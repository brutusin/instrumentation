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

import java.util.Iterator;
import java.util.LinkedList;
import org.brutusin.bctrace.asm.utils.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class ReturnHelper {

    public static void addTraceReturn(MethodNode mn, int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = mn.instructions;
        Iterator<AbstractInsnNode> it = il.iterator();
        Type returnType = Type.getReturnType(mn.desc);

        while (it.hasNext()) {
            AbstractInsnNode abstractInsnNode = it.next();

            switch (abstractInsnNode.getOpcode()) {
                case Opcodes.RETURN:
                    il.insertBefore(abstractInsnNode, getVoidReturnTraceInstructions(frameDataVarIndex, hooksToUse));
                    break;
                case Opcodes.IRETURN:
                case Opcodes.LRETURN:
                case Opcodes.FRETURN:
                case Opcodes.ARETURN:
                case Opcodes.DRETURN:
                    il.insertBefore(abstractInsnNode, getReturnTraceInstructions(returnType, frameDataVarIndex, hooksToUse));
            }
        }
    }

    private static InsnList getVoidReturnTraceInstructions(int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = new InsnList();
        Iterator<Integer> descendingIterator = hooksToUse.descendingIterator();
        while (descendingIterator.hasNext()) {
            Integer index = descendingIterator.next();
            il.add(new InsnNode(Opcodes.ACONST_NULL));
            il.add(new VarInsnNode(Opcodes.ALOAD, frameDataVarIndex));
            il.add(ASMUtils.getPushInstruction(index));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "org/brutusin/bctrace/runtime/Callback", "onFinishedReturn",
                    "(Ljava/lang/Object;Lorg/brutusin/bctrace/runtime/FrameData;I)V", false));
        }
        return il;
    }

    private static InsnList getReturnTraceInstructions(Type returnType, int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = new InsnList();
        Iterator<Integer> descendingIterator = hooksToUse.descendingIterator();
        while (descendingIterator.hasNext()) {
            Integer index = descendingIterator.next();
            if (returnType.getSize() == 1) {
                il.add(new InsnNode(Opcodes.DUP));
            } else {
                il.add(new InsnNode(Opcodes.DUP2));
            }
            MethodInsnNode mNode = ASMUtils.getWrapperContructionInst(returnType);
            if (mNode != null) {
                il.add(mNode);
            }
            il.add(new VarInsnNode(Opcodes.ALOAD, frameDataVarIndex));
            il.add(ASMUtils.getPushInstruction(index));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "org/brutusin/bctrace/runtime/Callback", "onFinishedReturn",
                    "(Ljava/lang/Object;Lorg/brutusin/bctrace/runtime/FrameData;I)V", false));
        }
        return il;
    }
}
