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

import org.brutusin.bctrace.spi.Filter;
import java.security.ProtectionDomain;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A filter that accepts all classes non loaded by the bootstrap class loader
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class NonBootstrapFilter implements Filter {

    @Override
    public boolean instrumentClass(String className, ProtectionDomain protectionDomain, ClassLoader cl) {
        return cl != Object.class.getClassLoader();
    }

    @Override
    public boolean instrumentMethod(ClassNode classNode, MethodNode mn) {
        return true;
    }
}
