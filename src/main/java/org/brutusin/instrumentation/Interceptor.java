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

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * This class constitutes the main extension point in the framework.
 * Implementation class names are passed to the framework via the options
 * argument in the agent.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public abstract class Interceptor extends Callback {

    /**
     * Initializes the instance. This is a life-cycle method called by the agent
     * after instantiation.
     *
     * @param arg Second token (split by ";") in the agent options after the
     * Interceptor class name.
     * @throws Exception 
     */
    public abstract void init(String arg) throws Exception;

    /**
     * When to instrument a class based on its name.
     *
     * @param className the name of the class in the internal form of fully
     * qualified class and interface names as defined in
     * <i>The Java Virtual Machine Specification</i>. For example,
     * <code>"java/util/List"</code>.
     * @param byteCode the input byte buffer in class file format
     * @return
     */
    public abstract boolean interceptClass(String className, byte[] byteCode);

    /**
     * When to instrument a method of a class based on their ASM representation
     *
     * @param cn
     * @param mn
     * @return
     */
    public abstract boolean interceptMethod(ClassNode cn, MethodNode mn);
}
