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

import java.io.InputStream;
import java.security.ProtectionDomain;
import org.apache.commons.io.IOUtils;
import org.brutusin.bctrace.runtime.Callback;
import org.brutusin.bctrace.runtime.FrameData;
import org.brutusin.bctrace.runtime.InstrumentationImpl;
import org.brutusin.bctrace.spi.Filter;
import org.brutusin.bctrace.spi.Hook;
import org.brutusin.bctrace.spi.Listener;
import org.brutusin.bctrace.spi.impl.AllFilter;
import org.brutusin.bctrace.spi.impl.VoidListener;
import org.junit.Test;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class PerformanceTest {

    @Test
    public void testMinimimOverheadPrimitive() throws Exception {
        int stackDepth = 2000;
        int times = 10000;

        long nano = System.nanoTime();
        for (int i = 0; i < times; i++) {
            TestClass.fact(stackDepth);
        }
        long normalElapse = (System.nanoTime() - nano) / times;

        Class clazz = getInstrumentClass(TestClass.class);
        nano = System.nanoTime();
        for (int i = 0; i < times; i++) {
            clazz.getMethod("fact", long.class).invoke(null, stackDepth);
        }
        long instrumentedElapse = (System.nanoTime() - nano) / times;

        System.out.println("Normal (primitive): " + normalElapse / 1e6 + " ms");
        System.out.println("Instrumented (primitive): " + instrumentedElapse / 1e6 + " ms");
    }

    @Test
    public void testMinimimOverheadWrapper() throws Exception {
        long stackDepth = 2000;
        int times = 10000;

        long nano = System.nanoTime();
        for (int i = 0; i < times; i++) {
            TestClass.factWrapper(stackDepth);
        }
        long normalElapse = (System.nanoTime() - nano) / times;

        Class clazz = getInstrumentClass(TestClass.class);
        nano = System.nanoTime();
        for (int i = 0; i < times; i++) {
            clazz.getMethod("factWrapper", Long.class).invoke(null, stackDepth);
        }
        long instrumentedElapse = (System.nanoTime() - nano) / times;

        System.out.println("Normal (wrapper): " + normalElapse / 1e6 + " ms");
        System.out.println("Instrumented (wrapper): " + instrumentedElapse / 1e6 + " ms");

    }

    private static Class getInstrumentClass(Class clazz) throws Exception {
        String className = clazz.getCanonicalName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName);
        byte[] bytes = IOUtils.toByteArray(is);
        Hook hook = new Hook() {
            @Override
            public Filter getFilter() {
                return new AllFilter() {
                    @Override
                    public boolean instrumentClass(String className, ProtectionDomain protectionDomain, ClassLoader cl) {
                        return className.contains("TestClass");
                    }
                };
            }

            @Override
            public Listener getListener() {
                return new VoidListener() {
                    @Override
                    public Object onStart(FrameData fd) {
//                        if (((Long) fd.args[0]) % 2 == 0) {
//                            System.nanoTime();
//                        }
                        return null;
                    }
                };
            }
        };
        hook.init(new InstrumentationImpl(null));
        Callback.hooks = new Hook[]{hook};
        Transformer transformer = new Transformer();
        byte[] newBytes = transformer.transform(clazz.getClassLoader(), className, clazz, clazz.getProtectionDomain(), bytes);
        // Helper.viewByteCode(newBytes);
        ByteClassLoader cl = new ByteClassLoader();
        return cl.loadClass(className, newBytes);
    }

    static class ByteClassLoader extends ClassLoader {

        public Class<?> loadClass(String name, byte[] byteCode) {
            return super.defineClass(name, byteCode, 0, byteCode.length);
        }
    }
}
