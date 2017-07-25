/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation.spi;

import org.brutusin.instrumentation.runtime.FrameData;

/**
 *
 * @author ignacio
 */
public interface Listener {

    public void init(String param, Instrumentation bi);

    public Object onStart(FrameData fd);

    public void onFinishedReturn(Object ret, FrameData fd);

    public void onFinishedThrowable(Throwable th, FrameData fd);

    public void onBeforeThrown(Throwable th, FrameData fd);

}
