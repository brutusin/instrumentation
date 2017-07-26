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

import java.lang.reflect.InvocationTargetException;
import org.brutusin.instrumentation.spi.Instrumentation;
import org.brutusin.instrumentation.spi.Plugin;
import org.brutusin.instrumentation.spi.impl.InstrumentationImpl;

public class Agent {

    public static void premain(final String agentArgs, java.lang.instrument.Instrumentation javaInstrumentation) throws Exception {
        if (agentArgs == null) {
            throw new IllegalArgumentException("Agent argument is required of the form 'plugin-class-name[:plugin-custom-args];...'");
        }
        String[] tokens = agentArgs.split(";", 2);
        Plugin[] plugins = new Plugin[tokens.length];
        InstrumentationImpl[] instrumentations = new InstrumentationImpl[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            instrumentations[i] = new InstrumentationImpl(javaInstrumentation);
            plugins[i] = createPlugin(tokens[i], instrumentations[i]);
        }
        Transformer transformer = new Transformer(plugins, instrumentations);
        javaInstrumentation.addTransformer(transformer, javaInstrumentation.isRetransformClassesSupported());
    }

    private static Plugin createPlugin(String str, InstrumentationImpl ins) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        String[] tokens = str.split(":", 2);
        Class<?> clazz = Agent.class.getClassLoader().loadClass(tokens[0]);
        Plugin ret = (Plugin) clazz.newInstance();
        if (tokens.length > 1) {
            ret.init(tokens[1], ins);
        } else {
            ret.init(null, ins);
        }
        return ret;
    }
}
