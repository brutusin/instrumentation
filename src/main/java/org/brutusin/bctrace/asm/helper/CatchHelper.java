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
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class CatchHelper {

    public static LabelNode insertStartNode(MethodNode mn) {
        LabelNode ret = new LabelNode();
        mn.instructions.insert(ret);
        return ret;
    }

    public static void addTraceThrowableUncaught(MethodNode mn, LabelNode startNode, int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = mn.instructions;

        LabelNode endNode = new LabelNode();
        il.add(endNode);

        addCatchBlock(mn, startNode, endNode, frameDataVarIndex, hooksToUse);
    }

    private static void addCatchBlock(MethodNode mn, LabelNode startNode, LabelNode endNode, int frameDataVarIndex, LinkedList<Integer> hooksToUse) {

        InsnList il = new InsnList();
        LabelNode handlerNode = new LabelNode();
        il.add(handlerNode);
        il.add(getThrowableTraceInstructions(frameDataVarIndex, hooksToUse));
        il.add(new InsnNode(Opcodes.ATHROW));

        TryCatchBlockNode blockNode = new TryCatchBlockNode(startNode, endNode, handlerNode, null);

        mn.tryCatchBlocks.add(blockNode);
        mn.instructions.add(il);
    }

    private static InsnList getThrowableTraceInstructions(int frameDataVarIndex, LinkedList<Integer> hooksToUse) {
        InsnList il = new InsnList();
        Iterator<Integer> descendingIterator = hooksToUse.descendingIterator();
        while (descendingIterator.hasNext()) {
            Integer index = descendingIterator.next();
            il.add(new InsnNode(Opcodes.DUP));
            il.add(new VarInsnNode(Opcodes.ALOAD, frameDataVarIndex));
            il.add(ASMUtils.getPushInstruction(index));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "org/brutusin/bctrace/runtime/Callback", "onFinishedThrowable",
                    "(Ljava/lang/Throwable;Lorg/brutusin/bctrace/runtime/FrameData;I)V", false));
        }
        return il;
    }
}
