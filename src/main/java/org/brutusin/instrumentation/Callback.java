/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation;

/**
 *
 * @author ignacio
 */
public final class Callback {

    public static Listener listener = new VoidListener();

    public static Object onStart(FrameData fd) {
        return listener.onStart(fd);
    }

    public static void onFinishedReturn(Object ret, FrameData fd) {
        listener.onFinishedReturn(ret, fd);
    }

    public static void onFinishedThrowable(Throwable th, FrameData fd) {
        listener.onFinishedThrowable(th, fd);
    }

    public static void onBeforeThrown(Throwable th, FrameData fd) {
        listener.onBeforeThrown(th, fd);
    }
}
