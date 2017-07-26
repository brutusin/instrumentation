/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation.spi.impl;

import org.brutusin.instrumentation.spi.Instrumentation;
import org.brutusin.instrumentation.spi.Listener;
import org.brutusin.instrumentation.runtime.FrameData;

/**
 *
 * @author ignacio
 */
public class VoidListener implements Listener {

    @Override
    public Object onStart(FrameData fd) {
        return null;
    }

    @Override
    public void onFinishedReturn(Object ret, FrameData fd) {
    }

    @Override
    public void onFinishedThrowable(Throwable th, FrameData fd) {
    }

    @Override
    public void onBeforeThrown(Throwable th, FrameData fd) {
    }
}
