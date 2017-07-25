/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation.spi;

import java.lang.instrument.UnmodifiableClassException;

/**
 *
 * @author ignacio
 */
public interface Instrumentation {

    boolean isRetransformClassesSupported();
    
    boolean isModifiableClass(Class<?> clazz);

    String[] getTransformedClasses();

    void retransformClasses(Class<?>... classes) throws UnmodifiableClassException;
}
