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
public class ThrowHelper {

    public static void addTraceThrow(MethodNode mn, int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = mn.instructions;
        Iterator<AbstractInsnNode> it = il.iterator();
        while (it.hasNext()) {
            AbstractInsnNode abstractInsnNode = it.next();

            switch (abstractInsnNode.getOpcode()) {
                case Opcodes.ATHROW:
                    il.insertBefore(abstractInsnNode, getThrowTraceInstructions(frameDataVarIndex, hooksToUse));
                    break;
            }
        }
    }

    private static InsnList getThrowTraceInstructions(int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = new InsnList();
        for (Integer index : hooksToUse) {
            il.add(new InsnNode(Opcodes.DUP));
            il.add(new VarInsnNode(Opcodes.ALOAD, frameDataVarIndex));
            il.add(ASMUtils.getPushInstruction(index));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "org/brutusin/bctrace/runtime/Callback", "onBeforeThrown",
                    "(Ljava/lang/Throwable;Lorg/brutusin/bctrace/runtime/FrameData;I)V", false));
        }
        return il;
    }
}
