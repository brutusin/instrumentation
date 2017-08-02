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
package org.brutusin.bctrace.spi;

import org.brutusin.bctrace.runtime.FrameData;

/**
 * Listener instances define the actions to be performed when a instrumented
 * method is invoked.
 *
 * @see Plugin
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public interface Listener {

    /**
     * Invoked by instrumented methods before any of its original instructions
     * (if multiple plugins are registered, listener notification is performed
     * according to their respective plugin registration order).
     *
     * @param fd Current stack frame data
     * @return
     */
    public Object onStart(FrameData fd);

    /**
     * Invoked by instrumented methods just before return (if multiple plugins
     * are registered, listener notification is performed according to their
     * respective plugin <b>reverse</b> registration order).
     *
     * @param ret Object being returned by the method. Wrapper type if the
     * original return type is primitive. <code>null</code> if the method return
     * type is <code>void</code>
     * @param fd Current stack frame data
     */
    public void onFinishedReturn(Object ret, FrameData fd);

    /**
     * Invoked by instrumented methods just before rising a throwable to the
     * caller (if multiple plugins are registered, listener notification is
     * performed according to their respective plugin <b>reverse</b>
     * registration order).
     *
     * @param th thowable to be raised
     * @param fd Current stack frame data
     */
    public void onFinishedThrowable(Throwable th, FrameData fd);

    /**
     * Invoked by instrumented methods just before the actual method throws a
     * throwable.
     *
     * @param th thowable to be thrown
     * @param fd Current stack frame data
     */
    public void onBeforeThrown(Throwable th, FrameData fd);

}
