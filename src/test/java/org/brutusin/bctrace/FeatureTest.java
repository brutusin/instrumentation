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

import java.lang.reflect.InvocationTargetException;
import org.brutusin.bctrace.TestClass.TestRuntimeException;
import org.brutusin.bctrace.runtime.FrameData;
import org.brutusin.bctrace.spi.Filter;
import org.brutusin.bctrace.spi.Hook;
import org.brutusin.bctrace.spi.Listener;
import org.brutusin.bctrace.spi.impl.AllFilter;
import org.brutusin.bctrace.spi.impl.VoidListener;
import org.junit.Test;
import static org.junit.Assert.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class FeatureTest extends BcTraceTest {

    public void testStart() throws Exception {
        final StringBuilder steps = new StringBuilder();
        Class clazz = getInstrumentClass(TestClass.class, new Hook[]{
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public Object onStart(FrameData fd) {
                            assertNotNull(fd);
                            steps.append("1");
                            return null;
                        }
                    };
                }
            },
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public Object onStart(FrameData fd) {
                            assertNotNull(fd);
                            steps.append("2");
                            return null;
                        }
                    };
                }
            }
        });
        clazz.getMethod("execVoid").invoke(null);
        assertEquals(steps.toString(), "12");
    }

    @Test
    public void testThrown() throws Exception {
        final StringBuilder steps = new StringBuilder();
        Class clazz = getInstrumentClass(TestClass.class, new Hook[]{
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public void onBeforeThrown(Throwable th, FrameData fd) {
                            assertTrue(th instanceof TestRuntimeException);
                            assertNotNull(fd);
                            steps.append("1");
                        }
                    };
                }
            },
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public void onBeforeThrown(Throwable th, FrameData fd) {
                            assertTrue(th instanceof TestRuntimeException);
                            assertNotNull(fd);
                            steps.append("2");
                        }
                    };
                }
            }
        });
        boolean captured = false;
        try {
            clazz.getMethod("throwRuntimeException").invoke(null);
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof TestRuntimeException) {
                captured = true;
                assertEquals(steps.toString(), "12");
            }
        }
        assertTrue("Expected exception", captured);
    }

    @Test
    public void testVoidReturn() throws Exception {
        final StringBuilder steps = new StringBuilder();
        Class clazz = getInstrumentClass(TestClass.class, new Hook[]{
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public void onFinishedReturn(Object ret, FrameData fd) {
                            assertNull(ret);
                            assertNotNull(fd);
                            steps.append("1");
                        }
                    };
                }
            },
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public void onFinishedReturn(Object ret, FrameData fd) {
                            assertNull(ret);
                            assertNotNull(fd);
                            steps.append("2");
                        }
                    };
                }
            }
        });
        clazz.getMethod("execVoid").invoke(null);
        assertEquals(steps.toString(), "21");
    }

    @Test
    public void testReturn() throws Exception {
        final StringBuilder steps = new StringBuilder();
        Class clazz = getInstrumentClass(TestClass.class, new Hook[]{
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public void onFinishedReturn(Object ret, FrameData fd) {
                            assertNotNull(ret);
                            assertNotNull(fd);
                            steps.append("1");
                        }
                    };
                }
            },
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public void onFinishedReturn(Object ret, FrameData fd) {
                            assertNotNull(ret);
                            assertNotNull(fd);
                            steps.append("2");
                        }
                    };
                }
            }
        });
        clazz.getMethod("getLong").invoke(null);
        clazz.getMethod("getInt").invoke(null);
        clazz.getMethod("getObject").invoke(null);
        assertEquals(steps.toString(), "212121");
    }

    @Test
    public void testUncaughtThrowable() throws Exception {
        final StringBuilder steps = new StringBuilder();
        Class clazz = getInstrumentClass(TestClass.class, new Hook[]{
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter();
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public void onFinishedThrowable(Throwable th, FrameData fd) {
                            assertNotNull(th);
                            assertNotNull(fd);
                            steps.append("1");
                        }
                    };
                }
            },
            new Hook() {
                @Override
                public Filter getFilter() {
                    return new AllFilter() {
                        @Override
                        public boolean instrumentMethod(ClassNode classNode, MethodNode mn) {
                            return mn.name.equals("getLongWithConditionalException");
                        }
                    };
                }

                @Override
                public Listener getListener() {
                    return new VoidListener() {
                        @Override
                        public void onFinishedThrowable(Throwable th, FrameData fd) {
                            assertNotNull(th);
                            assertNotNull(fd);
                            steps.append("2");
                        }
                    };
                }
            }
        });
        clazz.getMethod("getLongWithConditionalException", boolean.class).invoke(null, false);
        boolean captured = false;
        try {
            clazz.getMethod("getLongWithConditionalException", boolean.class).invoke(null, true);
            assertEquals(steps.toString(), "");
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof TestRuntimeException) {
                captured = true;
                assertEquals(steps.toString(), "121");
            }
        }
        assertTrue("Expected exception", captured);
    }

}
