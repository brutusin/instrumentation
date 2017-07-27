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
package org.brutusin.instrumentation.spi;

/**
 * An <b>instrumentation plugin</b> is the main abstraction to be provided by
 * agent implementations.
 * <br>
 * Plugins determine what methods to instrument (to inject notification hooks
 * in) and what actions to perform at runtime under the events triggered by the
 * hooks.
 * <br><br>
 * Plugins should be registered in a file called
 * <code>'.brutusin-intrumentation'</code> located in the root package of the
 * agent jar, with one plugin class name per line. Plugin listener notication is
 * performed according the order they appear in the previous file.
 * <br><br>
 * If retransformation of classes is supported in the current JVM, plugins are
 * registered as retransformation-capable, meaning that if a retransformation of
 * a class is triggered, its filter instance will be invoked again for all the
 * methods of that particular class, determining if a listener hook will be
 * injected or not into the new class definition.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public interface Plugin {

    /**
     * Initializes the plugin. Called once at startup before initial
     * instrumentation is performed.
     *
     * @param s Initialization string passed from the command line
     * @param ins Intrumentation callback, allowing triggering retransformations
     */
    void init(String s, Instrumentation ins);

    /**
     * Returns the filter, deciding what methods to instrument.
     *
     * @return
     */
    Filter getFilter();

    /**
     * Returns the listener invoked by the instrumented method hooks.
     *
     * @return
     */
    Listener getListener();
}
