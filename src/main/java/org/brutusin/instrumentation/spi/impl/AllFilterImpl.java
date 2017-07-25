/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation.spi.impl;

import org.brutusin.instrumentation.spi.Filter;
import java.security.ProtectionDomain;
import org.brutusin.instrumentation.Agent;
import org.brutusin.instrumentation.utils.Helper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * @author ignacio
 */
public class AllFilterImpl implements Filter {

    public void init(String s) {
    }

    public boolean instrumentClass(String className, ProtectionDomain protectionDomain, ClassLoader cl) {
        if (!isAncestor(Agent.class.getClassLoader(), cl)) {
            return false;
        }
        if (className.startsWith("org/brutusin")) {
            return false;
        }
        return true;
    }

    public boolean instrumentMethod(ClassNode classNode, MethodNode mn) {
        if(Helper.isAbstract(mn)){
            return false;
        }
        if(Helper.isNative(mn)){
            return false;
        }
        return true;
    }

    private static boolean isAncestor(ClassLoader ancestor, ClassLoader cl) {
        if (ancestor == null || cl == null) {
            return false;
        }
        if (ancestor.equals(cl)) {
            return true;
        }
        return isAncestor(ancestor, cl.getParent());
    }

}
