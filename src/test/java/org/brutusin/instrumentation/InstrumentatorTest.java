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

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.brutusin.instrumentation.utils.Helper;
import org.junit.Test;

public class InstrumentatorTest {

    private static Class getInstrumentClass(Class clazz) throws Exception {

        String className = clazz.getCanonicalName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName);
        byte[] bytes = IOUtils.toByteArray(is);
        Transformer transformer = Transformer.getInstance();
        Callback.listener = new Listener() {
            public Object onStart(FrameData fd) {
                System.out.println(fd.getClassName() + "." + fd.getMethodDescriptor());
                return null;
            }

            public void onFinishedReturn(Object ret, FrameData fd) {
            }

            public void onFinishedThrowable(Throwable th, FrameData fd) {
            }

            public void onBeforeThrown(Throwable th, FrameData fd) {
            }

            public void init(String s) {
            }

        };
        byte[] newBytes = transformer.transform(clazz.getClassLoader(), className, clazz, clazz.getProtectionDomain(), bytes);
        Helper.viewByteCode(newBytes);
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
