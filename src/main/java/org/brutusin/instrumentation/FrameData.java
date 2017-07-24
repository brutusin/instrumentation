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
public final class FrameData {

    private String className;
    private Object instance;
    private String methodDescriptor;
    private Object[] args;

    private FrameData() {

    }

    public static FrameData getInstance(Object instance, String className, String methodDescriptor, Object[] args) {
        FrameData ret = new FrameData();
        ret.className = className;
        ret.instance = instance;
        ret.methodDescriptor = methodDescriptor;
        ret.args = args;
        return ret;
    }

    public String getClassName() {
        return className;
    }

    public Object getInstance() {
        return instance;
    }

    public String getMethodDescriptor() {
        return methodDescriptor;
    }

    public Object[] getArgs() {
        return args;
    }
    
    private void dispose() {

    }
}
