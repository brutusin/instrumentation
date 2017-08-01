/*
 * Copyright 2014 brutusin.org
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
package org.brutusin.instrumentation;

import org.brutusin.instrumentation.spi.Listener;
import org.brutusin.instrumentation.runtime.FrameData;
import java.io.InputStream;
import java.security.ProtectionDomain;
import org.apache.commons.io.IOUtils;
import org.brutusin.instrumentation.spi.Filter;
import org.brutusin.instrumentation.spi.Instrumentation;
import org.brutusin.instrumentation.spi.impl.NonBootstrapFilter;
import org.brutusin.instrumentation.runtime.InstrumentationImpl;
import org.brutusin.instrumentation.runtime.MethodInfo;
import org.brutusin.instrumentation.runtime.MethodRegistry;
import org.brutusin.instrumentation.spi.impl.VoidListener;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.brutusin.instrumentation.spi.Hook;

public class InstrumentatorTest {

    private static Class getInstrumentClass(Class clazz) throws Exception {

        String className = clazz.getCanonicalName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName);
        byte[] bytes = IOUtils.toByteArray(is);
        InstrumentationImpl[] instrumentations = new InstrumentationImpl[]{
            new InstrumentationImpl(null, null),
            new InstrumentationImpl(null, null)
        };
        Hook[] plugins = new Hook[]{
            new Hook() {

                @Override
                public Filter getFilter() {
                    return new NonBootstrapFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public Object onStart(FrameData fd) {
                            System.out.println(MethodRegistry.getInstance().getMethod(fd.methodId));
                            return null;
                        }
                    };
                }

                @Override
                public void init(Instrumentation ins) {
                }

            },
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new Filter() {

                        @Override
                        public boolean instrumentClass(String className, ProtectionDomain protectionDomain, ClassLoader cl) {
                            return true;
                        }

                        @Override
                        public boolean instrumentMethod(ClassNode classNode, MethodNode mn) {
                            return mn.name.equals("doubleIsDifferent");
                        }
                    };
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public Object onStart(FrameData fd) {
                            System.out.println("ahahahahaaa" + MethodRegistry.getInstance().getMethod(fd.methodId));
                            return null;
                        }
                    };
                }

                @Override
                public void init(Instrumentation ins) {
                }

            }};
        Transformer transformer = new Transformer();
        transformer.init(
                plugins,
                instrumentations);

        for (int i = 0; i < instrumentations.length; i++) {
            InstrumentationImpl instrumentation = instrumentations[i];
            plugins[i].init(instrumentation);
        }
        byte[] newBytes = transformer.transform(clazz.getClassLoader(), className, clazz, clazz.getProtectionDomain(), bytes);
        // Helper.viewByteCode(newBytes);
        ByteClassLoader cl = new ByteClassLoader();
        return cl.loadClass(className, newBytes);
    }

    @Test

    public void test() throws Exception {
        Class clazz = getInstrumentClass(B.class);
        Object b = clazz.newInstance();
        System.out.println(clazz.getMethod("doubleIsDifferent", double.class, double.class, double.class).invoke(b, 3d, 3.1d, 0.5));
        System.out.println(clazz.getMethod("joinTheJoyRide").invoke(null));

    }

    static class ByteClassLoader extends ClassLoader {

        public Class<?> loadClass(String name, byte[] byteCode) {
            return super.defineClass(name, byteCode, 0, byteCode.length);
        }
    }
}
