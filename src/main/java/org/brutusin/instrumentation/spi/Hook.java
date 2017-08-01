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
 * An <b>instrumentation hook</b> determines what methods to instrument and what
 * actions to perform at runtime under the events triggered by the instrumented
 * methods.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 * @see BctraceAgent
 */
public interface Hook {

    /**
     * Initializes the plugin. Called once at startup before initial
     * instrumentation is performed.
     *
     * @param ins Intrumentation callback, allowing triggering retransformations
     */
    void init(Instrumentation ins);

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
