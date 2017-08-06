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
package org.brutusin.bctrace;

import org.brutusin.bctrace.asm.Transformer;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.brutusin.bctrace.runtime.Callback;
import org.brutusin.bctrace.runtime.InstrumentationImpl;
import org.brutusin.bctrace.spi.Hook;

/**
 * Framework entry point.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class Bctrace {
    
    static Bctrace instance;
    
    private final Transformer transformer;
    private final List<Hook> hooks;
    private final java.lang.instrument.Instrumentation javaInstrumentation;
    
    Bctrace(java.lang.instrument.Instrumentation javaInstrumentation, Hook[] initialHooks) {
        this.javaInstrumentation = javaInstrumentation;
        this.hooks = Collections.synchronizedList(new ArrayList<Hook>());
        this.transformer = new Transformer();
        if(initialHooks!=null){
            for (Hook initialHook : initialHooks) {
                addHook(initialHook);
            }
        }
        javaInstrumentation.addTransformer(transformer, javaInstrumentation.isRetransformClassesSupported());
    }
    
    public static Bctrace getInstance() {
        if (instance == null) {
            throw new Error("Instrumentation is not properly configured. Please verify agent manifest attributes");
        }
        return instance;
    }
    
    public void addHook(Hook hook) {
        this.hooks.add(hook);
        updateCallback();
        hook.init(new InstrumentationImpl(javaInstrumentation));
    }
    
    public void removeHook(Hook hook) {
        removeHook(hook, true);
    }
    
    public void removeHook(Hook hook, boolean retransform) {
        int index = this.hooks.indexOf(hook);
        this.hooks.set(index, null);
        if (retransform) {
            try {
                hook.getInstrumentation().retransformClasses(hook.getInstrumentation().getTransformedClasses());
            } catch (UnmodifiableClassException ex) {
                throw new AssertionError();
            }
        }
        updateCallback();
    }
    
    private void updateCallback() {
        Callback.hooks = hooks.toArray(new Hook[hooks.size()]);
    }
    
}
