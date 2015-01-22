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

import static org.junit.Assert.*;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.io.IOUtils;
import org.brutusin.commons.Bean;
import org.junit.Test;

public class InstrumentatorTest {

    private static final String callbackId = "1";

    private static Class<?> clazz = SimpleClass.class;

    private static Class instrumentClass(Interceptor interceptor) throws Exception {
        if (Callback.getInstance(callbackId) != null) {
            Callback.removeCallback(callbackId);
        }
        Callback.registerCallback(callbackId, interceptor);
        ByteClassLoader cl = new ByteClassLoader();
        String className = clazz.getCanonicalName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName);
        byte[] bytes = IOUtils.toByteArray(is);
        Instrumentator instrumentator = new Instrumentator(className, bytes, interceptor, callbackId);
        byte[] newbytes = instrumentator.modifyClass();
        return cl.loadClass(className, newbytes);
    }

    public void compareStaticMethodResult(String methodName, Class[] argClasses, Object[] args) throws Exception {
        Method method = clazz.getMethod(methodName, argClasses);
        Object result = method.invoke(null, args);
        Class instrumentedClass = instrumentClass(new VoidInterceptor());
        Method method2 = instrumentedClass.getMethod(methodName, argClasses);
        Object result2 = method2.invoke(null, args);
        assertEquals(result2, result);
    }

    @Test
    public void testEqualResult() throws Exception {
        compareStaticMethodResult("sayHello", new Class[]{String.class}, new Object[]{"world"});
    }

    @Test
    public void testException() throws Exception {
        final Bean<String> executionIdBean = new Bean<String>();
        final Bean<Integer> counterBean = new Bean<Integer>();
        final Bean<Throwable> thBean = new Bean<Throwable>();
        final Bean<AssertionError> assertionBean = new Bean<AssertionError>();

        Interceptor interceptor = new VoidInterceptor() {
            @Override
            public void doOnStart(Object source, Object[] arg, String executionId) {
                try {
                    assertNull(executionIdBean.getValue());
                    assertNull(counterBean.getValue());
                    assertNull(thBean.getValue());
                    executionIdBean.setValue(executionId);
                    counterBean.setValue(1);
                } catch (AssertionError e) {
                    if (assertionBean.getValue() == null) {
                        assertionBean.setValue(e);
                    } else {
                        //assertionBean.getValue().addSuppressed(e);
                        assertionBean.setValue(e); // JDK 6 comp
                    }
                }
            }

            @Override
            public void doOnThrowableThrown(Object source, Throwable th, String executionId) {
                try {
                    assertNotNull(executionIdBean.getValue());
                    assertEquals(executionId, executionIdBean.getValue());
                    assertEquals(new Integer(1), counterBean.getValue());
                    assertNull(thBean.getValue());
                    counterBean.setValue(counterBean.getValue() + 1);
                    thBean.setValue(th);
                } catch (AssertionError e) {
                    if (assertionBean.getValue() == null) {
                        assertionBean.setValue(e);
                    } else {
                        //assertionBean.getValue().addSuppressed(e);
                        assertionBean.setValue(e); // JDK 6 comp
                    }
                }
            }

            @Override
            public void doOnThrowableUncatched(Object source, Throwable th, String executionId) {
                try {
                    assertNotNull(executionIdBean.getValue());
                    assertNotNull(thBean.getValue());
                    assertEquals(executionId, executionIdBean.getValue());
                    assertEquals(th, thBean.getValue());
                    assertEquals(new Integer(2), counterBean.getValue());
                } catch (AssertionError e) {
                    if (assertionBean.getValue() == null) {
                        assertionBean.setValue(e);
                    } else {
                        //assertionBean.getValue().addSuppressed(e);
                        assertionBean.setValue(e); // JDK 6 comp
                    }
                }
            }

            @Override
            public void doOnFinish(Object source, Object o, String executionId) {
                AssertionError e = new AssertionError();
                if (assertionBean.getValue() == null) {
                    assertionBean.setValue(e);
                } else {
                    //assertionBean.getValue().addSuppressed(e);
                    assertionBean.setValue(e); // JDK 6 comp
                }
            }
        };

        try {
            Class instrumentedClass = instrumentClass(interceptor);
            instrumentedClass.getMethod("throwHello").invoke(null);
        } catch (InvocationTargetException ite) {
            assertEquals(ite.getTargetException(), thBean.getValue());
        }
        if (assertionBean.getValue() != null) {
            throw assertionBean.getValue();
        }
        assertNotNull(thBean.getValue());
        assertTrue(thBean.getValue() instanceof RuntimeException && thBean.getValue().getMessage().equals(SimpleClass.GREETING));
    }

    @Test
    public void testSuccess() throws Exception {
        final Bean<String> executionIdBean = new Bean<String>();
        final Bean<AssertionError> assertionBean = new Bean<AssertionError>();

        Interceptor interceptor = new VoidInterceptor() {

            @Override
            public void doOnStart(Object source, Object[] arg, String executionId) {
                try {
                    assertNull(executionIdBean.getValue());
                    executionIdBean.setValue(executionId);
                } catch (AssertionError e) {
                    if (assertionBean.getValue() == null) {
                        assertionBean.setValue(e);
                    } else {
                        //assertionBean.getValue().addSuppressed(e);
                        assertionBean.setValue(e); // JDK 6 comp
                    }
                }
            }

            @Override
            public void doOnFinish(Object source, Object o, String executionId) {
                try {
                    assertNotNull(executionIdBean.getValue());
                    assertEquals(executionId, executionIdBean.getValue());
                } catch (AssertionError e) {
                    if (assertionBean.getValue() == null) {
                        assertionBean.setValue(e);
                    } else {
                        //assertionBean.getValue().addSuppressed(e);
                        assertionBean.setValue(e); // JDK 6 comp
                    }
                }
            }

            @Override
            public void doOnThrowableThrown(Object source, Throwable th, String executionId) {
                AssertionError e = new AssertionError();
                if (assertionBean.getValue() == null) {
                    assertionBean.setValue(e);
                } else {
                    //assertionBean.getValue().addSuppressed(e);
                    assertionBean.setValue(e); // JDK 6 comp
                }
            }

            @Override
            public void doOnThrowableUncatched(Object source, Throwable th, String executionId) {
                AssertionError e = new AssertionError();
                if (assertionBean.getValue() == null) {
                    assertionBean.setValue(e);
                } else {
                    //assertionBean.getValue().addSuppressed(e);
                    assertionBean.setValue(e); // JDK 6 comp
                }
            }
        };
        Class instrumentedClass = instrumentClass(interceptor);
        instrumentedClass.getMethod("sayHello", String.class).invoke(null, "world");
        if (assertionBean.getValue() != null) {
            throw assertionBean.getValue();
        }
    }

    @Test
    public void testNested() throws Exception {
        final Bean<Integer> startCounterBean = new Bean<Integer>();
        startCounterBean.setValue(0);
        final Bean<Integer> finishCounterBean = new Bean<Integer>();
        finishCounterBean.setValue(0);
        final Bean<AssertionError> assertionBean = new Bean<AssertionError>();

        Interceptor interceptor = new VoidInterceptor() {

            @Override
            public void doOnStart(Object source, Object[] arg, String executionId) {
                startCounterBean.setValue(startCounterBean.getValue() + 1);
            }

            @Override
            public void doOnFinish(Object source, Object o, String executionId) {
                finishCounterBean.setValue(finishCounterBean.getValue() + 1);
            }

            @Override
            public void doOnThrowableThrown(Object source, Throwable th, String executionId) {
                AssertionError e = new AssertionError();
                if (assertionBean.getValue() == null) {
                    assertionBean.setValue(e);
                } else {
                    //assertionBean.getValue().addSuppressed(e);
                    assertionBean.setValue(e); // JDK 6 comp
                }
            }

            @Override
            public void doOnThrowableUncatched(Object source, Throwable th, String executionId) {
                AssertionError e = new AssertionError();
                if (assertionBean.getValue() == null) {
                    assertionBean.setValue(e);
                } else {
                    //assertionBean.getValue().addSuppressed(e);
                    assertionBean.setValue(e); // JDK 6 comp
                }
            }
        };
        Class instrumentedClass = instrumentClass(interceptor);
        instrumentedClass.getMethod("sayHelloDate", String.class).invoke(null, "world");
        if (assertionBean.getValue() != null) {
            throw assertionBean.getValue();
        }
        assertEquals(startCounterBean.getValue(), new Integer(2));
        assertEquals(finishCounterBean.getValue(), new Integer(2));
    }

    static class ByteClassLoader extends ClassLoader {

        public Class<?> loadClass(String name, byte[] byteCode) {
            return super.defineClass(name, byteCode, 0, byteCode.length);
        }
    }
}
