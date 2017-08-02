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

import java.security.ProtectionDomain;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A filter determines which classes and methods are instrumented.
 * <br><br>
 * If the class is transformable, the framework performs an initial query to the
 * {@link #instrumentClass(String, ProtectionDomain, ClassLoader) instrumentClass}
 * method. If this return <code>true</code> the filter
 * {@link #instrumentMethod(ClassNode, MethodNode) instrumentMethod} method will
 * be invoked once per non abstract nor native method in the class. Invocations
 * returning <code>true</code> lead to a hook insertions into the bytecode of
 * the method.
 *
 * @see Plugin
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public interface Filter {

    /**
     * Whether or not instrument the methods of a class.
     *
     * @param className
     * @param protectionDomain
     * @param cl
     * @return
     */
    public boolean instrumentClass(String className, ProtectionDomain protectionDomain, ClassLoader cl);

    /**
     * Whether or not instrument the specified method.
     *
     * @param classNode
     * @param mn
     * @return
     */
    public boolean instrumentMethod(ClassNode classNode, MethodNode mn);
}
