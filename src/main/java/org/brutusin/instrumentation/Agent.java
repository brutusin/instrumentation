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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.brutusin.instrumentation.spi.Plugin;
import org.brutusin.instrumentation.runtime.InstrumentationImpl;

/**
 * Framework entry point (should be referenced 'Premain-Class' in the manifest
 * of the agent jar).
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class Agent {

    private static final String PLUGIN_DESCRIPTOR_RESOURCE_NAME = ".brutusin-instrumentation";
    private static final Pattern JAR_FILE_NAME_FROM_URL_PATTERN = Pattern.compile("(?:zip:|jar:file:/|file:/)([^˛!]*)(?:!/.*)?", Pattern.CASE_INSENSITIVE);

    public static void premain(final String agentArg, java.lang.instrument.Instrumentation javaInstrumentation) throws Exception {
        String[] pluginClassNames = readPluginClassNamesFromDescriptor();
        if (pluginClassNames.length == 0) {
            throw new Error("Instrumentation descriptor '" + PLUGIN_DESCRIPTOR_RESOURCE_NAME + "' does not contain any plugin class name");
        }
        String[] args;
        if (agentArg != null) {
            args = agentArg.split(":", -1);
            if (args.length != pluginClassNames.length) {
                throw new Error("Invalid number of arguments supplied to the agent. One argument per plugin has to be informed 'arg1:arg2:...argn'. Plugins: " + Arrays.toString(pluginClassNames));
            }
        } else {
            args = new String[pluginClassNames.length];
        }
        Plugin[] plugins = new Plugin[pluginClassNames.length];
        InstrumentationImpl[] instrumentations = new InstrumentationImpl[pluginClassNames.length];
        for (int i = 0; i < pluginClassNames.length; i++) {
            instrumentations[i] = new InstrumentationImpl(javaInstrumentation);
            plugins[i] = createPlugin(pluginClassNames[i], args[i], instrumentations[i]);
        }
        Transformer transformer = new Transformer(plugins, instrumentations);
        javaInstrumentation.addTransformer(transformer, javaInstrumentation.isRetransformClassesSupported());
    }

    private static Plugin createPlugin(String className, String arg, InstrumentationImpl ins) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Class<?> clazz = Agent.class.getClassLoader().loadClass(className);
        Plugin ret = (Plugin) clazz.newInstance();
        ret.init(arg, ins);
        return ret;
    }

    private static String[] readPluginClassNamesFromDescriptor() throws IOException {
        Enumeration<URL> resources = Agent.class.getClassLoader().getResources(PLUGIN_DESCRIPTOR_RESOURCE_NAME);
        String agentJar = getJarName(Agent.class.getProtectionDomain().getCodeSource().getLocation());
        if (agentJar == null) {
            throw new Error("Could not extract agent jar file name");
        }
        URL descriptorUrl = null;
        // Ignores resources with the same name out the agent jar
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            String jarFilename = getJarName(url);
            if (agentJar.equals(jarFilename)) {
                descriptorUrl = url;
                break;
            }
        }
        if (descriptorUrl == null) {
            throw new Error("Instrumentation descriptor '" + PLUGIN_DESCRIPTOR_RESOURCE_NAME + "' not found in the agent jar");
        }
        ArrayList<String> list = new ArrayList<String>();
        Scanner scanner = new Scanner(descriptorUrl.openStream());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                list.add(line);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private static String getJarName(URL url) {
        Matcher m = JAR_FILE_NAME_FROM_URL_PATTERN.matcher(url.toExternalForm());
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static void main(String[] args) {
        String str = "file:/Users/ignacio/Documents/GitHub/runtime-agent/target/runtime-agent-0.0.0-SNAPSHOT-jar-with-dependencies.jar!/fsdfsd";
        Matcher m = JAR_FILE_NAME_FROM_URL_PATTERN.matcher(str);
        if (m.find()) {
            System.out.println(m.group(1));
        }
    }
}
