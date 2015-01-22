/*
 * Copyright 2014 brutusin.org
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
package org.brutusin.instrumentation;

import java.util.HashMap;
import java.util.Map;

/**
 * Instances of this class are notified by the instrumented methods.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public abstract class Callback {

    // One instance per agent. Required to fulfill the agent contract that lets several agents to be running at same time
    private static final Map<String, Callback> INSTANCES = new HashMap();

    // To avoid recursevely notifications due to instrumented classes used by listeners
    private final ThreadLocal<Boolean> ALREADY_NOTIFIED_FLAG = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private final ThreadLocal<Integer> COUNTER = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 1;
        }
    };

    public static Callback getInstance(String id) {
        return INSTANCES.get(id);
    }

    public static void registerCallback(String id, Callback callback) {
        if (INSTANCES.containsKey(id)) {
            throw new IllegalArgumentException(id + " already registered");
        }
        INSTANCES.put(id, callback);
    }

    public static void removeCallback(String id) {
        if (!INSTANCES.containsKey(id)) {
            throw new IllegalArgumentException(id + " is not registered");
        }
        INSTANCES.remove(id);
    }

    public final String onStart(Object source, Object[] arg) {
        if (ALREADY_NOTIFIED_FLAG.get()) {
            return null;
        }
        ALREADY_NOTIFIED_FLAG.set(true);
        String executionId = getExecutionId();
        try {
            doOnStart(source, arg, executionId);
        } catch (Throwable th) {
            th.printStackTrace(System.err);
        } finally {
            ALREADY_NOTIFIED_FLAG.set(false);
        }
        return executionId;
    }

    public final void onThrowableThrown(Object source, Throwable throwable, String executionId) {
        if (ALREADY_NOTIFIED_FLAG.get()) {
            return;
        }
        ALREADY_NOTIFIED_FLAG.set(true);
        try {
            doOnThrowableThrown(source, throwable, executionId);
        } catch (Throwable th) {
            th.printStackTrace(System.err);
        }
    }

    public final void onThrowableUncatched(Object source, Throwable throwable, String executionId) {
        if (ALREADY_NOTIFIED_FLAG.get()) {
            return;
        }
        ALREADY_NOTIFIED_FLAG.set(true);
        try {
            doOnThrowableUncatched(source, throwable, executionId);
        } catch (Throwable th) {
            th.printStackTrace(System.err);
        } finally {
            ALREADY_NOTIFIED_FLAG.set(false);
        }
    }

    public final void onVoidFinish(Object source, String executionId) {
        if (ALREADY_NOTIFIED_FLAG.get()) {
            return;
        }
        ALREADY_NOTIFIED_FLAG.set(true);
        try {
            doOnFinish(source, null, executionId);
        } catch (Throwable th) {
            th.printStackTrace(System.err);
        } finally {
            ALREADY_NOTIFIED_FLAG.set(false);
        }
    }

    public final void onFinish(Object source, Object o, String executionId) {
        if (ALREADY_NOTIFIED_FLAG.get()) {
            return;
        }
        ALREADY_NOTIFIED_FLAG.set(true);
        try {
            doOnFinish(source, o, executionId);
        } catch (Throwable th) {
            th.printStackTrace(System.err);
        } finally {
            ALREADY_NOTIFIED_FLAG.set(false);
        }
    }

    protected abstract void doOnStart(Object source, Object[] arg, String executionId);

    protected abstract void doOnThrowableThrown(Object source, Throwable throwable, String executionId);

    protected abstract void doOnThrowableUncatched(Object source, Throwable throwable, String executionId);

    protected abstract void doOnFinish(Object source, Object result, String executionId);

    private synchronized final String getExecutionId() {
        int counter = COUNTER.get();
        COUNTER.set(counter + 1);
        return Thread.currentThread().getId() + ":" + counter;
    }

}
