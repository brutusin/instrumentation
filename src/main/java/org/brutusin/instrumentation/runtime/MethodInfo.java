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
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class MethodInfo {

    private final String className;
    private final String signature;

    private final String representation;

    public MethodInfo(String className, String signature) {
        this.className = className;
        this.signature = signature;
        this.representation = className + "." + signature;
    }

    public String getClassName() {
        return className;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return representation;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.className != null ? this.className.hashCode() : 0);
        hash = 83 * hash + (this.signature != null ? this.signature.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MethodInfo other = (MethodInfo) obj;
        if ((this.className == null) ? (other.className != null) : !this.className.equals(other.className)) {
            return false;
        }
        return !((this.signature == null) ? (other.signature != null) : !this.signature.equals(other.signature));
    }
}
