/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.obfuscate;

import java.io.PrintWriter;
import java.util.Stack;

import proguard.classfile.Clazz;
import proguard.classfile.JavaTypeConstants;
import proguard.classfile.Method;
import proguard.classfile.ProgramClass;
import proguard.classfile.ProgramField;
import proguard.classfile.ProgramMethod;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.LineNumberInfo;
import proguard.classfile.attribute.LineNumberTableAttribute;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.peephole.LineNumberLinearizer;


/**
 * This ClassVisitor prints out the renamed method arguments with
 * their old names and new names.
 *
 * @see ClassRenamer
 *
 * @author chrispaulwu
 */
public class MappingExtraPrinter
implements   ClassVisitor,
             MemberVisitor,
             AttributeVisitor
{
    private final PrintWriter pw;

    // A field serving as a return value for the visitor methods.
    private boolean printed;


    /**
     * Creates a new MappingPrinter that prints to the given writer.
     * @param printWriter the writer to which to print.
     */
    public MappingExtraPrinter(PrintWriter printWriter)
    {
        this.pw = printWriter;
    }


    // Implementations for ClassVisitor.

    @Override
    public void visitAnyClass(Clazz clazz) { }


    @Override
    public void visitProgramClass(ProgramClass programClass)
    {
        String name    = programClass.getName();

        // Print out the class mapping.
        pw.println(ClassUtil.externalClassName(name) +
                   ":");

        // Print out the class members.
        programClass.methodsAccept(this);
    }


    // Implementations for MemberVisitor.


    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        String methodNameAndDescription  = oldMethodNameAndDescription(programMethod);
        String newMethodName             = programMethod.getName(programClass);

        if (methodNameAndDescription != null) {
            String[] methodNameAndDescArray = methodNameAndDescription.split(",");
            if (methodNameAndDescArray.length == 2) {
                pw.println("    " +
                        ClassUtil.externalMethodReturnType(methodNameAndDescArray[1]) + " " +
                        methodNameAndDescArray[0]                                     + JavaTypeConstants.METHOD_ARGUMENTS_OPEN  +
                        ClassUtil.externalMethodArguments(methodNameAndDescArray[1])  + JavaTypeConstants.METHOD_ARGUMENTS_CLOSE +
                        " -> " +
                        ClassUtil.externalMethodReturnType(programMethod.getDescriptor(programClass)) + " " +
                        newMethodName                                                                 + JavaTypeConstants.METHOD_ARGUMENTS_OPEN  +
                        ClassUtil.externalMethodArguments(programMethod.getDescriptor(programClass))  + JavaTypeConstants.METHOD_ARGUMENTS_CLOSE);
            }
        }
    }

    public static String oldMethodNameAndDescription(ProgramMethod programMethod)
    {
        Object processingExtraInfo = programMethod.getProcessingExtraInfo();

        return processingExtraInfo instanceof String ?
                (String)processingExtraInfo :
                null;
    }
}
