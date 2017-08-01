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
package org.brutusin.instrumentation;

import org.brutusin.instrumentation.runtime.InstrumentationImpl;
import org.brutusin.instrumentation.spi.BctraceAgent;
import org.brutusin.instrumentation.spi.Hook;

/**
 * Framework entry point.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class Agent {

    public static void premain(final String arg, java.lang.instrument.Instrumentation javaInstrumentation) throws Exception {
        if (arg == null) {
            throw new Error("An agent implementation class has to be specified: {className}[:{initArgs}]");
        }
        String[] tokens = arg.split(":", 2);
        String agentClass = tokens[0];
        String agentArgs;
        if (tokens.length == 2) {
            agentArgs = tokens[1];
        } else {
            agentArgs = null;
        }
        BctraceAgent ba = (BctraceAgent) Class.forName(agentClass).newInstance();
        ba.init(agentArgs);

        Transformer transformer = new Transformer();

        Hook[] plugins = ba.getHooks();
        InstrumentationImpl[] instrumentations = new InstrumentationImpl[plugins.length];
        for (int i = 0; i < plugins.length; i++) {
            instrumentations[i] = new InstrumentationImpl(transformer, javaInstrumentation);
            plugins[i].init(instrumentations[i]);
        }
        transformer.init(plugins, instrumentations);
        javaInstrumentation.addTransformer(transformer, javaInstrumentation.isRetransformClassesSupported());
    }
}
