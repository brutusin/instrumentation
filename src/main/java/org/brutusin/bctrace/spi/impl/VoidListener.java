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
package org.brutusin.bctrace.spi.impl;

import org.brutusin.bctrace.spi.Listener;
import org.brutusin.bctrace.runtime.FrameData;

/**
 * A listener that hears but not listens. :)
 * 
 * @author Ignacio del Valle Alles idelvall@brutusin.org
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
