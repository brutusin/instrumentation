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

import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import org.brutusin.instrumentation.utils.Helper;
import org.brutusin.instrumentation.utils.TreeInstructions;
import org.objectweb.asm.tree.LabelNode;

public class Instrumentator {

    protected final String callbackId;
    protected final Interceptor interceptor;
    protected final String className;
    protected final byte[] originalClassFileBuffer;

    protected ClassNode cn;
    protected Type classType;

    protected MethodNode mn;
    protected Type[] methodArguments;
    protected Type methodReturnType;
    protected int methodOffset;

    /*
     Callback arguments: Method scope variables
     */
    protected int methodVarIndex;
    protected int executionIdIndex;

    protected LabelNode startNode;

    public Instrumentator(String className, byte[] classfileBuffer, Interceptor interceptor, String callbackId) {

        this.className = className;
        this.originalClassFileBuffer = classfileBuffer;
        this.callbackId = callbackId;
        this.interceptor = interceptor;
    }

    public byte[] modifyClass() {
        if (!this.interceptor.interceptClass(className, originalClassFileBuffer)) {
            return originalClassFileBuffer;
        }
        ClassReader cr = new ClassReader(originalClassFileBuffer);
        this.cn = new ClassNode();
        cr.accept(cn, 0);
        this.classType = Type.getType("L" + cn.name + ";");

        List<MethodNode> methods = cn.methods;
        boolean transformed = false;
        for (MethodNode node : methods) {
            if (modifyMethod(node) == true) {
                transformed = true;
            }
        }
        if (!transformed) {
            return originalClassFileBuffer;
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cn.accept(cw);

        return cw.toByteArray();

    }

    private boolean modifyMethod(MethodNode mn) {

        if (!this.interceptor.interceptMethod(this.cn, mn) || Helper.isAbstract(mn)) {
            return false;
        }
        this.mn = mn;
        this.methodArguments = Type.getArgumentTypes(this.mn.desc);
        this.methodReturnType = Type.getReturnType(this.mn.desc);
        this.methodOffset = isStatic() ? 0 : 1;

        addTraceStart();
        addTraceReturn();
        addTraceThrow();
        addTraceThrowablePassed();

        return true;
    }

    private int addMethodParametersVariable(InsnList il) {
        il.add(TreeInstructions.getPushInstruction(this.methodArguments.length));
        il.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
        int methodParametersIndex = getFistAvailablePosition();
        il.add(new VarInsnNode(Opcodes.ASTORE, methodParametersIndex));
        this.mn.maxLocals++;
        for (int i = 0; i < this.methodArguments.length; i++) {
            il.add(new VarInsnNode(Opcodes.ALOAD, methodParametersIndex));
            il.add(TreeInstructions.getPushInstruction(i));
            il.add(TreeInstructions.getLoadInst(methodArguments[i],
                    getArgumentPosition(i)));
            MethodInsnNode mNode = TreeInstructions
                    .getWrapperContructionInst(methodArguments[i]);
            if (mNode != null) {
                il.add(mNode);
            }
            il.add(new InsnNode(Opcodes.AASTORE));
        }
        return methodParametersIndex;
    }

    private void addGetMethodInvocation(InsnList il) {
        il.add(TreeInstructions.getPushInstruction(this.methodArguments.length));
        il.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Class"));
        int parameterClassesIndex = getFistAvailablePosition();
        il.add(new VarInsnNode(Opcodes.ASTORE, parameterClassesIndex));
        this.mn.maxLocals++;
        for (int i = 0; i < this.methodArguments.length; i++) {
            il.add(new VarInsnNode(Opcodes.ALOAD, parameterClassesIndex));
            il.add(TreeInstructions.getPushInstruction(i));
            il.add(TreeInstructions.getClassReferenceInstruction(methodArguments[i], cn.version & 0xFFFF));
            il.add(new InsnNode(Opcodes.AASTORE));
        }
        il.add(TreeInstructions.getClassConstantReference(this.classType, cn.version & 0xFFFF));
        il.add(new LdcInsnNode(this.mn.name));
        il.add(new VarInsnNode(Opcodes.ALOAD, parameterClassesIndex));
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "org/brutusin/instrumentation/utils/Helper", "getSource",
                "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/Object;", false));
    }

    private void addStoreMethod(InsnList il) {
        this.methodVarIndex = getFistAvailablePosition();
        il.add(new VarInsnNode(Opcodes.ASTORE, this.methodVarIndex));
        this.mn.maxLocals++;
    }

    private void addGetCallback(InsnList il) {
        il.add(new LdcInsnNode(this.callbackId));
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "org/brutusin/instrumentation/Callback", "getInstance",
                "(Ljava/lang/String;)Lorg/brutusin/instrumentation/Callback;", false));
    }

    /*
     * 
     */
    private void addTraceStart() {
        InsnList il = new InsnList();
        int methodParametersIndex = addMethodParametersVariable(il);
        addGetMethodInvocation(il);
        addStoreMethod(il);
        addGetCallback(il);

        il.add(new VarInsnNode(Opcodes.ALOAD, this.methodVarIndex));
        il.add(new VarInsnNode(Opcodes.ALOAD, methodParametersIndex));
        il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "org/brutusin/instrumentation/Callback", "onStart",
                "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/String;", false));

        this.executionIdIndex = getFistAvailablePosition();
        il.add(new VarInsnNode(Opcodes.ASTORE, this.executionIdIndex));
        this.mn.maxLocals++;
        this.startNode = new LabelNode();
        this.mn.instructions.insert(startNode);
        this.mn.instructions.insert(il);
    }

    /*
     * 
     */
    private void addTraceReturn() {

        InsnList il = this.mn.instructions;

        Iterator<AbstractInsnNode> it = il.iterator();
        while (it.hasNext()) {
            AbstractInsnNode abstractInsnNode = it.next();

            switch (abstractInsnNode.getOpcode()) {
                case Opcodes.RETURN:
                    il.insertBefore(abstractInsnNode, getVoidReturnTraceInstructions());
                    break;
                case Opcodes.IRETURN:
                case Opcodes.LRETURN:
                case Opcodes.FRETURN:
                case Opcodes.ARETURN:
                case Opcodes.DRETURN:
                    il.insertBefore(abstractInsnNode, getReturnTraceInstructions());
            }
        }
    }

    /*
     * 
     */
    private void addTraceThrow() {

        InsnList il = this.mn.instructions;

        Iterator<AbstractInsnNode> it = il.iterator();
        while (it.hasNext()) {
            AbstractInsnNode abstractInsnNode = it.next();

            switch (abstractInsnNode.getOpcode()) {
                case Opcodes.ATHROW:
                    il.insertBefore(abstractInsnNode, getThrowTraceInstructions());
                    break;
            }
        }

    }

    /*
     * 
     */
    private void addTraceThrowablePassed() {

        InsnList il = this.mn.instructions;

        LabelNode endNode = new LabelNode();
        il.add(endNode);

        addCatchBlock(this.startNode, endNode);

    }

    private void addCatchBlock(LabelNode startNode, LabelNode endNode) {

        InsnList il = new InsnList();
        LabelNode handlerNode = new LabelNode();
        il.add(handlerNode);

        int exceptionVariablePosition = getFistAvailablePosition();
        il.add(new VarInsnNode(Opcodes.ASTORE, exceptionVariablePosition));
        this.methodOffset++; // Actualizamos el offset
        addGetCallback(il);
        il.add(new VarInsnNode(Opcodes.ALOAD, this.methodVarIndex));
        il.add(new VarInsnNode(Opcodes.ALOAD, exceptionVariablePosition));
        il.add(new VarInsnNode(Opcodes.ALOAD, this.executionIdIndex));
        il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "org/brutusin/instrumentation/Callback", "onThrowableUncatched",
                "(Ljava/lang/Object;Ljava/lang/Throwable;Ljava/lang/String;)V", false));

        il.add(new VarInsnNode(Opcodes.ALOAD, exceptionVariablePosition));
        il.add(new InsnNode(Opcodes.ATHROW));

        TryCatchBlockNode blockNode = new TryCatchBlockNode(startNode, endNode, handlerNode, null);

        this.mn.tryCatchBlocks.add(blockNode);
        this.mn.instructions.add(il);
    }

    /*
     * 
     */
    private InsnList getVoidReturnTraceInstructions() {
        InsnList il = new InsnList();
        addGetCallback(il);
        il.add(new VarInsnNode(Opcodes.ALOAD, this.methodVarIndex));
        il.add(new VarInsnNode(Opcodes.ALOAD, this.executionIdIndex));
        il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "org/brutusin/instrumentation/Callback", "onVoidFinish",
                "(Ljava/lang/Object;Ljava/lang/String;)V", false));

        return il;
    }

    /*
     * 
     */
    private InsnList getThrowTraceInstructions() {
        InsnList il = new InsnList();

        int exceptionVariablePosition = getFistAvailablePosition();
        il.add(new VarInsnNode(Opcodes.ASTORE, exceptionVariablePosition));

        this.methodOffset++; // Actualizamos el offset
        addGetCallback(il);
        il.add(new VarInsnNode(Opcodes.ALOAD, this.methodVarIndex));
        il.add(new VarInsnNode(Opcodes.ALOAD, exceptionVariablePosition));
        il.add(new VarInsnNode(Opcodes.ALOAD, this.executionIdIndex));
        il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "org/brutusin/instrumentation/Callback", "onThrowableThrown",
                "(Ljava/lang/Object;Ljava/lang/Throwable;Ljava/lang/String;)V", false));

        il.add(new VarInsnNode(Opcodes.ALOAD, exceptionVariablePosition));

        return il;
    }

    /*
     * 
     */
    private InsnList getReturnTraceInstructions() {

        InsnList il = new InsnList();

        int retunedVariablePosition = getFistAvailablePosition();
        il.add(TreeInstructions.getStoreInst(this.methodReturnType, retunedVariablePosition));

        this.variableCreated(this.methodReturnType); // Actualizamos el offset
        addGetCallback(il);
        il.add(new VarInsnNode(Opcodes.ALOAD, this.methodVarIndex));
        il.add(TreeInstructions.getLoadInst(this.methodReturnType, retunedVariablePosition));
        MethodInsnNode mNode = TreeInstructions.getWrapperContructionInst(this.methodReturnType);
        if (mNode != null) {
            il.add(mNode);
        }
        il.add(new VarInsnNode(Opcodes.ALOAD, this.executionIdIndex));
        il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "org/brutusin/instrumentation/Callback", "onFinish",
                "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;)V", false));

        il.add(TreeInstructions.getLoadInst(this.methodReturnType, retunedVariablePosition));

        return il;

    }

    /*
     * 
     */
    private int getFistAvailablePosition() {

        return this.mn.maxLocals + this.methodOffset;
    }

    /*
     * 
     */
    protected void variableCreated(Type type) {
        char charType = type.getDescriptor().charAt(0);
        if (charType == 'J' || charType == 'D') {
            this.methodOffset += 2;
        } else {
            this.methodOffset += 1;
        }
    }

    public int getArgumentPosition(int argNo) {
        return Helper.getArgumentPosition(this.methodOffset,
                this.methodArguments, argNo);
    }

    public boolean isAbstract() {
        return Helper.isAbstract(this.mn);
    }

    public boolean isStatic() {
        return Helper.isStatic(this.mn);
    }

    public boolean isPublic() {
        return Helper.isPublic(this.mn);
    }
}
