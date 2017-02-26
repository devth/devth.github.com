
import org.objectweb.asm.*;
public class StaticJavaQueryDump implements Opcodes {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(0);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(52, ACC_PUBLIC + ACC_SUPER, "StaticJavaQuery", null, "java/lang/Object", null);

{
mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
mv.visitInsn(RETURN);
mv.visitMaxs(1, 1);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "query", "(Lscala/collection/Seq;)Lscala/collection/Seq;", "(Lscala/collection/Seq<Lscala/Tuple3<Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;>;>;)Lscala/collection/Seq<Lscala/Tuple3<Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;>;>;", null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Seq", "iterator", "()Lscala/collection/Iterator;", true);
mv.visitVarInsn(ASTORE, 1);
mv.visitTypeInsn(NEW, "scala/collection/mutable/ArrayBuffer");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "scala/collection/mutable/ArrayBuffer", "<init>", "()V", false);
mv.visitVarInsn(ASTORE, 2);
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"scala/collection/Iterator", "scala/collection/mutable/ArrayBuffer"}, 0, null);
mv.visitVarInsn(ALOAD, 1);
mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Iterator", "hasNext", "()Z", true);
Label l1 = new Label();
mv.visitJumpInsn(IFEQ, l1);
mv.visitVarInsn(ALOAD, 1);
mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Iterator", "next", "()Ljava/lang/Object;", true);
mv.visitTypeInsn(CHECKCAST, "scala/Tuple3");
mv.visitVarInsn(ASTORE, 3);
mv.visitVarInsn(ALOAD, 3);
mv.visitMethodInsn(INVOKEVIRTUAL, "scala/Tuple3", "_2", "()Ljava/lang/Object;", false);
mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
mv.visitVarInsn(ASTORE, 4);
mv.visitVarInsn(ALOAD, 4);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
mv.visitIntInsn(SIPUSH, 1910);
Label l2 = new Label();
mv.visitJumpInsn(IF_ICMPGE, l2);
mv.visitVarInsn(ALOAD, 3);
mv.visitMethodInsn(INVOKEVIRTUAL, "scala/Tuple3", "_1", "()Ljava/lang/Object;", false);
mv.visitJumpInsn(IFNULL, l2);
mv.visitVarInsn(ALOAD, 2);
mv.visitVarInsn(ALOAD, 3);
mv.visitMethodInsn(INVOKEVIRTUAL, "scala/collection/mutable/ArrayBuffer", "$plus$eq", "(Ljava/lang/Object;)Lscala/collection/mutable/ArrayBuffer;", false);
mv.visitInsn(POP);
mv.visitLabel(l2);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitJumpInsn(GOTO, l0);
mv.visitLabel(l1);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 2);
mv.visitInsn(ARETURN);
mv.visitMaxs(2, 5);
mv.visitEnd();
}
cw.visitEnd();

return cw.toByteArray();
}
}

