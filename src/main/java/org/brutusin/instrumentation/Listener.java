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
public interface Listener extends Initializable{
    
    public Object onStart(FrameData fd);
    public void onFinishedReturn(Object ret, FrameData fd);
    public void onFinishedThrowable(Throwable th, FrameData fd);
    public void onBeforeThrown(Throwable th, FrameData fd);
    
}
