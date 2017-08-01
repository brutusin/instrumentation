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

import org.brutusin.instrumentation.spi.Hook;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class Callback {

    public static Hook[] plugins;

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
