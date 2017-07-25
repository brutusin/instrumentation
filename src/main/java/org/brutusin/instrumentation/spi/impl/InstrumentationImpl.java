/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation.spi.impl;

import org.brutusin.instrumentation.spi.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ignacio
 */
public final class InstrumentationImpl implements Instrumentation {

    private final java.lang.instrument.Instrumentation javaInstrumentation;
    private final Set<String> transformedClassNames = new HashSet<>();

    private boolean stale = true;
    private String[] transformedClassNamesArray;

    public InstrumentationImpl(java.lang.instrument.Instrumentation javaInstrumentation) {
        this.javaInstrumentation = javaInstrumentation;
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
        javaInstrumentation.retransformClasses(classes);
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
