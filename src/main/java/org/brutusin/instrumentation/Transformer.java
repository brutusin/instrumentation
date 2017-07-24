/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.List;
import org.brutusin.instrumentation.utils.Helper;
import org.brutusin.instrumentation.utils.TreeInstructions;
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
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 *
 * @author ignacio
 */
public class Transformer implements ClassFileTransformer {

    private static final Transformer INSTANCE = new Transformer();
    private Filter filter = new AllFilterImpl();

    public static Transformer getInstance() {
        return INSTANCE;
    }

    private Transformer() {
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public byte[] transform(final ClassLoader loader,
            final String className, final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classfileBuffer)
            throws IllegalClassFormatException {

        if (!filter.instrumentClass(className, protectionDomain, loader)) {
            return classfileBuffer;
        }

        ClassReader cr = new ClassReader(classfileBuffer);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        List<MethodNode> methods = cn.methods;
        boolean transformed = false;
        for (MethodNode node : methods) {
            if (modifyMethod(cn, node) == true) {
                transformed = true;
            }
        }
        if (!transformed) {
            return classfileBuffer;
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        cn.accept(cw);

        return cw.toByteArray();
    }

    private boolean modifyMethod(ClassNode cn, MethodNode mn) {

        if (!this.filter.instrumentMethod(cn, mn)) {
            return false;
        }
        int frameDataVarIndex = addTraceStart(cn, mn);
        addTraceReturn(mn, frameDataVarIndex);
//        addTraceStart();
//        addTraceReturn();
//        addTraceThrow();
//        addTraceThrowablePassed();
        return true;
    }

    private int addTraceStart(ClassNode cn, MethodNode mn) {
        InsnList il = new InsnList();
        if (Helper.isStatic(mn) || mn.name.equals("<init>")) {
            il.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }
        il.add(new LdcInsnNode(cn.name));
        il.add(new LdcInsnNode(mn.name + mn.desc));
        addMethodParametersVariable(il, mn);
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "org/brutusin/instrumentation/FrameData", "getInstance",
                "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Lorg/brutusin/instrumentation/FrameData;", false));
        il.add(new InsnNode(Opcodes.DUP));
        il.add(new VarInsnNode(Opcodes.ASTORE, mn.maxLocals));
        mn.maxLocals++;
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "org/brutusin/instrumentation/Callback", "onStart",
                "(Lorg/brutusin/instrumentation/FrameData;)Ljava/lang/Object;", false));
        il.add(new InsnNode(Opcodes.POP));
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
    private void addMethodParametersVariable(InsnList il, MethodNode mn) {
        Type[] methodArguments = Type.getArgumentTypes(mn.desc);
        if (methodArguments.length == 0) {
            il.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            il.add(TreeInstructions.getPushInstruction(methodArguments.length));
            il.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
            int index = Helper.isStatic(mn) ? 0 : 1;
            for (int i = 0; i < methodArguments.length; i++) {
                il.add(new InsnNode(Opcodes.DUP));
                il.add(TreeInstructions.getPushInstruction(i));
                il.add(TreeInstructions.getLoadInst(methodArguments[i], index));
                MethodInsnNode mNode = TreeInstructions.getWrapperContructionInst(methodArguments[i]);
                if (mNode != null) {
                    il.add(mNode);
                }
                il.add(new InsnNode(Opcodes.AASTORE));
                index += methodArguments[i].getSize();
            }
        }
    }

    private void addTraceReturn(MethodNode mn, int frameDataVarIndex) {

        InsnList il = mn.instructions;
        Iterator<AbstractInsnNode> it = il.iterator();
        Type returnType = Type.getReturnType(mn.desc);

        while (it.hasNext()) {
            AbstractInsnNode abstractInsnNode = it.next();

            switch (abstractInsnNode.getOpcode()) {
                case Opcodes.RETURN:
                    il.insertBefore(abstractInsnNode, getVoidReturnTraceInstructions(frameDataVarIndex));
                    break;
                case Opcodes.IRETURN:
                case Opcodes.LRETURN:
                case Opcodes.FRETURN:
                case Opcodes.ARETURN:
                case Opcodes.DRETURN:
                    il.insertBefore(abstractInsnNode, getReturnTraceInstructions(returnType, frameDataVarIndex));
            }
        }
    }

    private InsnList getVoidReturnTraceInstructions(int frameDataVarIndex) {
        InsnList il = new InsnList();
        il.add(new InsnNode(Opcodes.ACONST_NULL));
        il.add(new VarInsnNode(Opcodes.ALOAD, frameDataVarIndex));
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "org/brutusin/instrumentation/Callback", "onFinishedReturn",
                "(Ljava/lang/Object;Lorg/brutusin/instrumentation/FrameData;)V", false));

        return il;
    }

    private InsnList getReturnTraceInstructions(Type returnType, int frameDataVarIndex) {

        InsnList il = new InsnList();
        if (returnType.getSize() == 1) {
            il.add(new InsnNode(Opcodes.DUP));
        } else {
            il.add(new InsnNode(Opcodes.DUP2));
        }
        MethodInsnNode mNode = TreeInstructions.getWrapperContructionInst(returnType);
        if (mNode != null) {
            il.add(mNode);
        }
        il.add(new VarInsnNode(Opcodes.ALOAD, frameDataVarIndex));
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "org/brutusin/instrumentation/Callback", "onFinishedReturn",
                "(Ljava/lang/Object;Lorg/brutusin/instrumentation/FrameData;)V", false));

        return il;
    }
}
