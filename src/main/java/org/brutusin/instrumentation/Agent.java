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

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(final String agentArgs, Instrumentation instrumentation) throws InstantiationException {
        try {
            if (agentArgs == null) {
                throw new IllegalArgumentException("Agent argument is required of the form 'listener-class-name[:listener-custom-args];filter-class-name[:filter-custom-args]'");
            }
            String[] tokens = agentArgs.split(";", 2);
            final Listener listener = (Listener)loadImpl(tokens[0]);
            Callback.listener = listener;
            Transformer transformer = Transformer.getInstance();
            final Filter filter;
            if(tokens.length==2){
                filter = (Filter)loadImpl(tokens[1]);
                transformer.setFilter(filter);
            }
            instrumentation.addTransformer(transformer);
        } catch (Throwable th) {
            th.printStackTrace(System.err);
        }
    }
    
    private static Initializable loadImpl(String str) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String[] tokens = str.split(":", 2);
        Class<?> clazz = Agent.class.getClassLoader().loadClass(tokens[0]);
        final Initializable ret = (Initializable) clazz.newInstance();
        if (tokens.length == 2) {
            ret.init(tokens[1]);
        } else {
            ret.init(null);
        }
        return ret;
    }
}
