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
package org.brutusin.bctrace.spi;

import java.lang.instrument.UnmodifiableClassException;

/**
 * Offers retransformation capabilities to the hooks. The framework passes a
 * unique instance of this class to the hook though their initialization method.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public interface Instrumentation {

    /**
     * Whether or not this JVM supports class retransformation.
     *
     * @see
     * <a href="https://docs.oracle.com/javase/6/docs/api/java/lang/instrument/Instrumentation.html#isRetransformClassesSupported()">Instrumentation.isRetransformClassesSupported()</a>
     * @return
     */
    boolean isRetransformClassesSupported();

    /**
     * Whether or not this class can be retransformed.
     *
     * @param clazz
     * @return
     */
    boolean isModifiableClass(Class<?> clazz);

    /**
     * Returns an array of all classes currently loaded by the JVM.
     *
     * @return
     */
    Class[] getAllLoadedClasses();

    /**
     * Returns the names of the classes instrumented with the current hook.
     *
     * @return
     */
    Class[] getTransformedClasses();

    /**
     * Retransforms the classes.
     *
     * @see
     * <a href="https://docs.oracle.com/javase/6/docs/api/java/lang/instrument/Instrumentation.html#retransformClasses(java.lang.Class...)">Instrumentation.retransformClasses(java.lang.Class...)</a>
     * @param classes
     * @throws UnmodifiableClassException
     */
    void retransformClasses(Class<?>... classes) throws UnmodifiableClassException;

}
