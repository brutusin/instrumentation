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
package org.brutusin.instrumentation.runtime;

import org.brutusin.instrumentation.spi.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.brutusin.instrumentation.Transformer;

/**
 * Single implementation of the {@link Instrumentation Instrumentation}
 * interface.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class InstrumentationImpl implements Instrumentation {

    private final java.lang.instrument.Instrumentation javaInstrumentation;
    private final Set<String> transformedClassNames = new HashSet<String>();
    private final Transformer transformer;

    private boolean stale = true;
    private String[] transformedClassNamesArray;

    public InstrumentationImpl(Transformer transformer, java.lang.instrument.Instrumentation javaInstrumentation) {
        this.javaInstrumentation = javaInstrumentation;
        this.transformer = transformer;
    }

    @Override
    public boolean isRetransformClassesSupported() {
        return javaInstrumentation.isRetransformClassesSupported();
    }

    @Override
    public boolean isModifiableClass(Class<?> clazz) {
        return javaInstrumentation.isModifiableClass(clazz);
    }

    @Override
    public String[] getTransformedClasses() {
        if (stale) {
            transformedClassNamesArray = transformedClassNames.toArray(new String[transformedClassNames.size()]);
            stale = false;
        }
        return transformedClassNamesArray;
    }

    @Override
    public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
        if (classes != null) {
            javaInstrumentation.retransformClasses(classes);
        }
    }

    @Override
    public Class[] getAllLoadedClasses() {
        return javaInstrumentation.getAllLoadedClasses();
    }

    @Override
    public Class[] getRetransformableClasses() {
        List<Class> list = new ArrayList<Class>();
        Class[] allLoadedClasses = getAllLoadedClasses();
        for (Class clazz : allLoadedClasses) {
            if (isModifiableClass(clazz) && transformer.isRetransformable(clazz.getName())) {
                list.add(clazz);
            }
        }
        return list.toArray(new Class[list.size()]);
    }

    public void removeTransformedClass(String className) {
        transformedClassNames.remove(className);
        stale = true;
    }

    public void addTransformedClass(String className) {
        transformedClassNames.add(className);
        stale = true;
    }
}
