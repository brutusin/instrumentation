/*
 * Copyright 2017 brutusin.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.bctrace.asm;

import org.brutusin.bctrace.runtime.InstrumentationImpl;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.LinkedList;
import java.util.List;
import org.brutusin.bctrace.asm.helper.ReturnHelper;
import org.brutusin.bctrace.asm.helper.StartHelper;
import org.brutusin.bctrace.asm.helper.ThrowHelper;
import org.brutusin.bctrace.asm.utils.ASMUtils;
import org.brutusin.bctrace.runtime.Callback;
import org.brutusin.bctrace.spi.Hook;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class Transformer implements ClassFileTransformer {

    @Override
    public byte[] transform(final ClassLoader loader,
            final String className, final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classfileBuffer)
            throws IllegalClassFormatException {

        try {
            // Do not instrument agent classes
            if (protectionDomain != null && protectionDomain.equals(getClass().getProtectionDomain())) {
                return null;
            }
            if (className == null || classfileBuffer == null) {
                return null;
            }
            if (className.startsWith("sun/") || className.startsWith("com/sun/") || className.startsWith("javafx/") || className.startsWith("org/springframework/boot/")) {
                return null;
            }
            if (className.startsWith("java/lang/ThreadLocal")) {
                return null;
            }

            LinkedList<Integer> matchingHooks = getMatchingHooks(className, protectionDomain, loader);
            if (matchingHooks == null || matchingHooks.isEmpty()) {
                return null;
            }

            ClassReader cr = new ClassReader(classfileBuffer);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            boolean transformed = transformMethods(cn, matchingHooks);
            if (!transformed) {
                return null;
            } else {
                ClassWriter cw = new StaticClassWriter(cr, ClassWriter.COMPUTE_FRAMES, loader);
                cn.accept(cw);
                return cw.toByteArray();
            }
        } catch (Throwable th) {
            Hook[] hooks = Callback.hooks;
            if (hooks != null) {
                for (Hook hook : hooks) {
                    if (hook != null) {
                        hook.onError(th);
                    }
                }
            }
            return null;
        }
    }

    private LinkedList<Integer> getMatchingHooks(String className, ProtectionDomain protectionDomain, ClassLoader loader) {
        LinkedList<Integer> ret = new LinkedList<Integer>();
        Hook[] hooks = Callback.hooks;
        if (hooks != null) {
            for (int i = 0; i < hooks.length; i++) {
                if (hooks[i].getFilter().instrumentClass(className, protectionDomain, loader)) {
                    ret.add(i);
                }
                ((InstrumentationImpl) hooks[i].getInstrumentation()).removeTransformedClass(className);
            }
        }
        return ret;
    }

    private boolean transformMethods(ClassNode cn, LinkedList<Integer> matchingHooks) {
        List<MethodNode> methods = cn.methods;
        boolean transformed = false;
        for (MethodNode mn : methods) {
            if (ASMUtils.isAbstract(mn) || ASMUtils.isNative(mn)) {
                continue;
            }

            LinkedList<Integer> hooksToUse = new LinkedList<Integer>();
            Hook[] hooks = Callback.hooks;
            for (Integer i : matchingHooks) {
                if (hooks[i] != null && hooks[i].getFilter().instrumentMethod(cn, mn)) {
                    hooksToUse.add(i);
                    ((InstrumentationImpl) hooks[i].getInstrumentation()).addTransformedClass(cn.name.replace('/', '.'));
                }
            }
            if (!hooksToUse.isEmpty()) {
                modifyMethod(cn, mn, hooksToUse);
                transformed = true;
            }
        }
        return transformed;
    }

    private boolean modifyMethod(ClassNode cn, MethodNode mn, LinkedList<Integer> hooksToUse) {
        int frameDataVarIndex = StartHelper.addTraceStart(cn, mn, hooksToUse);
        ReturnHelper.addTraceReturn(mn, frameDataVarIndex, hooksToUse);
        ThrowHelper.addTraceThrow(mn, frameDataVarIndex, hooksToUse);
//        addTraceThrowablePassed();
        return true;
    }

}
