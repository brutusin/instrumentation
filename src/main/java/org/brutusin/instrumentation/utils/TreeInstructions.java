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
package org.brutusin.instrumentation.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TreeInstructions {

    public static VarInsnNode getLoadInst(Type type, int position) {
        int opCode = -1;
        switch (type.getDescriptor().charAt(0)) {
            case 'B':
                opCode = Opcodes.ILOAD;
                break;
            case 'C':
                opCode = Opcodes.ILOAD;
                break;
            case 'D':
                opCode = Opcodes.DLOAD;
                break;
            case 'F':
                opCode = Opcodes.FLOAD;
                break;
            case 'I':
                opCode = Opcodes.ILOAD;
                break;
            case 'J':
                opCode = Opcodes.LLOAD;
                break;
            case 'L':
                opCode = Opcodes.ALOAD;
                break;
            case '[':
                opCode = Opcodes.ALOAD;
                break;
            case 'Z':
                opCode = Opcodes.ILOAD;
                break;
            case 'S':
                opCode = Opcodes.ILOAD;
                break;
            default:
                throw new ClassFormatError("Invalid method signature: "
                        + type.getDescriptor());
        }
        return new VarInsnNode(opCode, position);
    }

    public static InsnList getClassReferenceInstruction(Type type, int majorVersion) {

        InsnList list = new InsnList();
        char charType = type.getDescriptor().charAt(0);
        String wrapper;
        switch (charType) {
            case 'B':
                wrapper = "java/lang/Byte";
                break;
            case 'C':
                wrapper = "java/lang/Character";
                break;
            case 'D':
                wrapper = "java/lang/Double";
                break;
            case 'F':
                wrapper = "java/lang/Float";
                break;
            case 'I':
                wrapper = "java/lang/Integer";
                break;
            case 'J':
                wrapper = "java/lang/Long";
                break;
            case 'L':
            case '[':
                return getClassConstantReference(type, majorVersion);
            case 'Z':
                wrapper = "java/lang/Boolean";
                break;
            case 'S':
                wrapper = "java/lang/Short";
                break;
            default:
                throw new ClassFormatError("Invalid method signature: "
                        + type.getDescriptor());
        }

        list.add(new FieldInsnNode(Opcodes.GETSTATIC, wrapper, "TYPE", "Ljava/lang/Class;"));
        return list;

    }

    public static MethodInsnNode getWrapperContructionInst(Type type) {

        char charType = type.getDescriptor().charAt(0);
        String wrapper;
        switch (charType) {
            case 'B':
                wrapper = "java/lang/Byte";
                break;
            case 'C':
                wrapper = "java/lang/Character";
                break;
            case 'D':
                wrapper = "java/lang/Double";
                break;
            case 'F':
                wrapper = "java/lang/Float";
                break;
            case 'I':
                wrapper = "java/lang/Integer";
                break;
            case 'J':
                wrapper = "java/lang/Long";
                break;
            case 'L':
                return null;
            case '[':
                return null;
            case 'Z':
                wrapper = "java/lang/Boolean";
                break;
            case 'S':
                wrapper = "java/lang/Short";
                break;
            default:
                throw new ClassFormatError("Invalid method signature: "
                        + type.getDescriptor());
        }

        return new MethodInsnNode(Opcodes.INVOKESTATIC, wrapper, "valueOf",
                "(" + charType + ")L" + wrapper + ";", false);

    }

    public static VarInsnNode getStoreInst(Type type, int position) {
        int opCode = -1;
        switch (type.getDescriptor().charAt(0)) {
            case 'B':
                opCode = Opcodes.ISTORE;
                break;
            case 'C':
                opCode = Opcodes.ISTORE;
                break;
            case 'D':
                opCode = Opcodes.DSTORE;
                break;
            case 'F':
                opCode = Opcodes.FSTORE;
                break;
            case 'I':
                opCode = Opcodes.ISTORE;
                break;
            case 'J':
                opCode = Opcodes.LSTORE;
                break;
            case 'L':
                opCode = Opcodes.ASTORE;
                break;
            case '[':
                opCode = Opcodes.ASTORE;
                break;
            case 'Z':
                opCode = Opcodes.ISTORE;
                break;
            case 'S':
                opCode = Opcodes.ISTORE;
                break;
            default:
                throw new ClassFormatError("Invalid method signature: "
                        + type.getDescriptor());
        }
        return new VarInsnNode(opCode, position);
    }

    public static AbstractInsnNode getPushInstruction(int value) {

        if (value == -1) {
            return new InsnNode(Opcodes.ICONST_M1);
        } else if (value == 0) {
            return new InsnNode(Opcodes.ICONST_0);
        } else if (value == 1) {
            return new InsnNode(Opcodes.ICONST_1);
        } else if (value == 2) {
            return new InsnNode(Opcodes.ICONST_2);
        } else if (value == 3) {
            return new InsnNode(Opcodes.ICONST_3);
        } else if (value == 4) {
            return new InsnNode(Opcodes.ICONST_4);
        } else if (value == 5) {
            return new InsnNode(Opcodes.ICONST_5);
        } else if ((value >= -128) && (value <= 127)) {
            return new IntInsnNode(Opcodes.BIPUSH, value);
        } else if ((value >= -32768) && (value <= 32767)) {
            return new IntInsnNode(Opcodes.SIPUSH, value);
        } else {
            return new LdcInsnNode(value);
        }
    }

    public static InsnList getClassConstantReference(Type type, int majorVersion) {
        InsnList il = new InsnList();

        if (majorVersion >= Opcodes.V1_5) {
            il.add(new LdcInsnNode(type));

        } else {
            String fullyQualifiedName = type.getInternalName().replaceAll("/", ".");
            il.add(new LdcInsnNode(fullyQualifiedName));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "java/lang/Class", "forName",
                    "(Ljava/lang/String;)Ljava/lang/Class;", false));
        }
        return il;
    }

}
