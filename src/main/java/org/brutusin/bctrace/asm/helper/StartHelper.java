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
package org.brutusin.bctrace.asm.helper;

import java.util.LinkedList;
import org.brutusin.bctrace.runtime.MethodRegistry;
import org.brutusin.bctrace.asm.utils.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class StartHelper {

    public static int addTraceStart(ClassNode cn, MethodNode mn, LinkedList<Integer> hooksToUse) {
        int methodId = MethodRegistry.getInstance().getMethodId(cn.name, mn.name + mn.desc);
        InsnList il = new InsnList();
        if (ASMUtils.isStatic(mn) || mn.name.equals("<init>")) {
            il.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }
        il.add(ASMUtils.getPushInstruction(methodId));
        addMethodParametersVariable(il, mn);
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "org/brutusin/bctrace/runtime/FrameData", "getInstance",
                "(Ljava/lang/Object;I[Ljava/lang/Object;)Lorg/brutusin/bctrace/runtime/FrameData;", false));

        il.add(new InsnNode(Opcodes.DUP));
        il.add(new VarInsnNode(Opcodes.ASTORE, mn.maxLocals));
        mn.maxLocals++;
        for (int i = 0; i < hooksToUse.size(); i++) {
            Integer index = hooksToUse.get(i);
            if (i < hooksToUse.size() - 1) {
                il.add(new InsnNode(Opcodes.DUP));
            }
            il.add(ASMUtils.getPushInstruction(index));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "org/brutusin/bctrace/runtime/Callback", "onStart",
                    "(Lorg/brutusin/bctrace/runtime/FrameData;I)Ljava/lang/Object;", false));

            il.add(new InsnNode(Opcodes.POP));
        }
        mn.instructions.insert(il);
        return mn.maxLocals - 1;
    }

    /**
     * Creates a the parameter object array reference on top of the operand
     * stack
     *
     * @param il
     * @param mn
     */
    private static void addMethodParametersVariable(InsnList il, MethodNode mn) {
        Type[] methodArguments = Type.getArgumentTypes(mn.desc);
        if (methodArguments.length == 0) {
            il.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            il.add(ASMUtils.getPushInstruction(methodArguments.length));
            il.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
            int index = ASMUtils.isStatic(mn) ? 0 : 1;
            for (int i = 0; i < methodArguments.length; i++) {
                il.add(new InsnNode(Opcodes.DUP));
                il.add(ASMUtils.getPushInstruction(i));
                il.add(ASMUtils.getLoadInst(methodArguments[i], index));
                MethodInsnNode mNode = ASMUtils.getWrapperContructionInst(methodArguments[i]);
                if (mNode != null) {
                    il.add(mNode);
                }
                il.add(new InsnNode(Opcodes.AASTORE));
                index += methodArguments[i].getSize();
            }
        }
    }
}
