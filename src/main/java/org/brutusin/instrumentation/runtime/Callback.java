/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation.runtime;

import org.brutusin.instrumentation.spi.Plugin;

/**
 *
 * @author ignacio
 */
public final class Callback {

    public static Plugin[] plugins;

    public static Object onStart(FrameData fd, int i) {
        return plugins[i].getListener().onStart(fd);
    }

    public static void onFinishedReturn(Object ret, FrameData fd, int i) {
        plugins[i].getListener().onFinishedReturn(ret, fd);
    }

    public static void onFinishedThrowable(Throwable th, FrameData fd, int i) {
        plugins[i].getListener().onFinishedThrowable(th, fd);
    }

    public static void onBeforeThrown(Throwable th, FrameData fd, int i) {
        plugins[i].getListener().onBeforeThrown(th, fd);
    }
}
