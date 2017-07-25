/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation.spi;

/**
 *
 * @author ignacio
 */
public abstract class Plugin {

    private final Instrumentation instrumentation;

    public Plugin(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public abstract Filter getFilter();

    public abstract Listener getListener();

    public abstract void init(String s);

    public final Instrumentation getInstrumentation() {
        return instrumentation;
    }
}
