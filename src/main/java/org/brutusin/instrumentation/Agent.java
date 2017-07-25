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

import org.brutusin.instrumentation.spi.impl.InstrumentationImpl;
import org.brutusin.instrumentation.spi.Instrumentation;
import org.brutusin.instrumentation.spi.Plugin;

public class Agent {

    public static void premain(final String agentArgs, java.lang.instrument.Instrumentation javaInstrumentation) throws InstantiationException {
        try {
            if (agentArgs == null) {
                throw new IllegalArgumentException("Agent argument is required of the form 'plugin-class-name[:plugin-custom-args];...'");
            }
            String[] tokens = agentArgs.split(";", 2);
            Plugin[] plugins = new Plugin[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                plugins[i] = createPlugin(tokens[i], javaInstrumentation);
            }
            Transformer transformer = new Transformer(plugins, javaInstrumentation);
            javaInstrumentation.addTransformer(transformer, javaInstrumentation.isRetransformClassesSupported());
        } catch (Throwable th) {
            th.printStackTrace(System.err);
        }
    }

    private static Plugin createPlugin(String str, java.lang.instrument.Instrumentation javaInstrumentation) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String[] tokens = str.split(":", 2);
        Class<?> clazz = Agent.class.getClassLoader().loadClass(tokens[0]);
        Plugin ret = (Plugin) clazz.newInstance();
        Instrumentation ins = new InstrumentationImpl(javaInstrumentation);
        if (tokens.length == 2) {
            ret.init(tokens[1], ins);
        } else {
            ret.init(null, ins);
        }
        return ret;
    }
}
