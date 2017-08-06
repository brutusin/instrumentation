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
package org.brutusin.bctrace.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class MethodRegistry {

    private static final MethodRegistry INSTANCE = new MethodRegistry();

    private final ArrayList<MethodInfo> methodArray = new ArrayList<MethodInfo>();
    private final Map<MethodInfo, Integer> methodMap = new HashMap<MethodInfo, Integer>();

    public static MethodRegistry getInstance() {
        return INSTANCE;
    }

    private MethodRegistry() {
    }

    public synchronized MethodInfo getMethod(int id) {
        return methodArray.get(id);
    }

    public synchronized int getMethodId(String className, String signature) {
        MethodInfo mi = new MethodInfo(className, signature);
        Integer id = methodMap.get(mi);
        if (id == null) {
            methodArray.add(mi);
            id = methodArray.size() - 1;
            methodMap.put(mi, id);
        }
        return id;
    }

    public synchronized int size() {
        return methodArray.size();
    }
}
