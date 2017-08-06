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

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import org.brutusin.bctrace.spi.Hook;

/**
 * Framework entry point.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class Init {

    private static final String DESCRIPTOR_NAME = ".bctrace";

    public static void premain(final String arg, Instrumentation inst) throws Exception {
        bootstrap(arg, inst);
    }

    public static void agentmain(String arg, Instrumentation inst) throws Exception {
        bootstrap(arg, inst);
    }

    private static void bootstrap(String agentArgs, Instrumentation inst) throws Exception {

        String[] hookClassNames = readHookClassNamesFromDescriptors();
        Hook[] hooks = new Hook[hookClassNames.length];
        for (int i = 0; i < hooks.length; i++) {
            hooks[i] = (Hook) Class.forName(hookClassNames[i]).newInstance();
        }
        Bctrace.instance = new Bctrace(inst, hooks);
    }

    private static String[] readHookClassNamesFromDescriptors() throws IOException {
        ClassLoader cl = Init.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader().getParent();
        }
        Enumeration<URL> resources = cl.getResources(DESCRIPTOR_NAME);
        ArrayList<String> list = new ArrayList<String>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    list.add(line);
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }
}
