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
package org.brutusin.bctrace;

import org.brutusin.bctrace.runtime.InstrumentationImpl;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.brutusin.bctrace.runtime.Callback;
import org.brutusin.bctrace.runtime.MethodRegistry;
import org.brutusin.bctrace.spi.Hook;
import org.brutusin.bctrace.utils.Helper;
import org.brutusin.bctrace.utils.TreeInstructions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
class Transformer implements ClassFileTransformer {

    Transformer() {
    }

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
            if (className.startsWith("sun/") || className.startsWith("com/sun/") || className.startsWith("javafx/")|| className.startsWith("org/springframework/boot/")) {
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
            if (Helper.isAbstract(mn) || Helper.isNative(mn)) {
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
        int frameDataVarIndex = addTraceStart(cn, mn, hooksToUse);
        addTraceReturn(mn, frameDataVarIndex, hooksToUse);
//        addTraceThrow();
//        addTraceThrowablePassed();
        return true;
    }

    private int addTraceStart(ClassNode cn, MethodNode mn, LinkedList<Integer> hooksToUse) {
        int methodId = MethodRegistry.getInstance().getMethodId(cn.name, mn.name + mn.desc);
        InsnList il = new InsnList();
        if (Helper.isStatic(mn) || mn.name.equals("<init>")) {
            il.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }
        il.add(TreeInstructions.getPushInstruction(methodId));
        addMethodParametersVariable(il, mn);
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "org/brutusin/bctrace/runtime/FrameData", "getInstance",
                "(Ljava/lang/Object;I[Ljava/lang/Object;)Lorg/brutusin/bctrace/runtime/FrameData;", false));

        il.add(new InsnNode(Opcodes.DUP));
        il.add(new VarInsnNode(Opcodes.ASTORE, mn.maxLocals));
        mn.maxLocals++;
        for (int i = 0; i < hooksToUse.size(); i++) {
            Integer index = hooksToUse.get(i);
            if (i < hooksToUse.size() - 1) {
                il.add(new InsnNode(Opcodes.DUP));
            }
            il.add(TreeInstructions.getPushInstruction(index));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "org/brutusin/bctrace/runtime/Callback", "onStart",
                    "(Lorg/brutusin/bctrace/runtime/FrameData;I)Ljava/lang/Object;", false));

            il.add(new InsnNode(Opcodes.POP));

        }

        mn.instructions.insert(il);
        return mn.maxLocals - 1;
    }

    /**
     * Creates a the parameter object array reference on top of the operand
     * stack
     *
     * @param il
     * @param mn
     */
    private void addMethodParametersVariable(InsnList il, MethodNode mn) {
        Type[] methodArguments = Type.getArgumentTypes(mn.desc);
        if (methodArguments.length == 0) {
            il.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            il.add(TreeInstructions.getPushInstruction(methodArguments.length));
            il.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
            int index = Helper.isStatic(mn) ? 0 : 1;
            for (int i = 0; i < methodArguments.length; i++) {
                il.add(new InsnNode(Opcodes.DUP));
                il.add(TreeInstructions.getPushInstruction(i));
                il.add(TreeInstructions.getLoadInst(methodArguments[i], index));
                MethodInsnNode mNode = TreeInstructions.getWrapperContructionInst(methodArguments[i]);
                if (mNode != null) {
                    il.add(mNode);
                }
                il.add(new InsnNode(Opcodes.AASTORE));
                index += methodArguments[i].getSize();
            }
        }
    }

    private void addTraceReturn(MethodNode mn, int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = mn.instructions;
        Iterator<AbstractInsnNode> it = il.iterator();
        Type returnType = Type.getReturnType(mn.desc);

        while (it.hasNext()) {
            AbstractInsnNode abstractInsnNode = it.next();

            switch (abstractInsnNode.getOpcode()) {
                case Opcodes.RETURN:
                    il.insertBefore(abstractInsnNode, getVoidReturnTraceInstructions(frameDataVarIndex, hooksToUse));
                    break;
                case Opcodes.IRETURN:
                case Opcodes.LRETURN:
                case Opcodes.FRETURN:
                case Opcodes.ARETURN:
                case Opcodes.DRETURN:
                    il.insertBefore(abstractInsnNode, getReturnTraceInstructions(returnType, frameDataVarIndex, hooksToUse));
            }
        }
    }

    private InsnList getVoidReturnTraceInstructions(int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = new InsnList();
        Iterator<Integer> descendingIterator = hooksToUse.descendingIterator();
        while (descendingIterator.hasNext()) {
            Integer index = descendingIterator.next();
            il.add(new InsnNode(Opcodes.ACONST_NULL));
            il.add(new VarInsnNode(Opcodes.ALOAD, frameDataVarIndex));
            il.add(TreeInstructions.getPushInstruction(index));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "org/brutusin/bctrace/runtime/Callback", "onFinishedReturn",
                    "(Ljava/lang/Object;Lorg/brutusin/bctrace/runtime/FrameData;I)V", false));
        }
        return il;
    }

    private InsnList getReturnTraceInstructions(Type returnType, int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = new InsnList();
        Iterator<Integer> descendingIterator = hooksToUse.descendingIterator();
        while (descendingIterator.hasNext()) {
            Integer index = descendingIterator.next();
            if (returnType.getSize() == 1) {
                il.add(new InsnNode(Opcodes.DUP));
            } else {
                il.add(new InsnNode(Opcodes.DUP2));
            }
            MethodInsnNode mNode = TreeInstructions.getWrapperContructionInst(returnType);
            if (mNode != null) {
                il.add(mNode);
            }
            il.add(new VarInsnNode(Opcodes.ALOAD, frameDataVarIndex));
            il.add(TreeInstructions.getPushInstruction(index));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "org/brutusin/bctrace/runtime/Callback", "onFinishedReturn",
                    "(Ljava/lang/Object;Lorg/brutusin/bctrace/runtime/FrameData;I)V", false));
        }
        return il;
    }
}
