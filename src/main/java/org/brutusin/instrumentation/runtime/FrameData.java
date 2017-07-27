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

/**
 * Holds the information of a stack frame.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
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

    public FrameData copy() {
        FrameData fd = new FrameData();
        fd.args = args;
        fd.className = className;
        fd.instance = instance;
        fd.methodDescriptor = methodDescriptor;
        return fd;
    }

    private void dispose() {
        // have in mind copies instances no in the pool
    }
}
