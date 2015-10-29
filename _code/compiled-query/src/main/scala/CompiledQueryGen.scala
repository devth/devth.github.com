import org.objectweb.asm._, Opcodes._
import Database.FilterExpr

object CompiledQueryGen extends Opcodes {

  val generatedClassName = "CompiledQuery"

  def generate: Array[Byte] = {

    val cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
    var mv: MethodVisitor = null

    cw.visit(52, ACC_PUBLIC + ACC_SUPER, generatedClassName, null,
      "java/lang/Object", null)

    // Constructor
    {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
      mv.visitCode()
      mv.visitVarInsn(ALOAD, 0)
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
      mv.visitInsn(RETURN)
      mv.visitMaxs(1, 1)
      mv.visitEnd()
    }

    // Static query method
    {
      mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "query",
        "(Lscala/collection/Seq;)Lscala/collection/Seq;",
        "(Lscala/collection/Seq<Lscala/Tuple3<Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;>;>;)Lscala/collection/Seq<Lscala/Tuple2<Ljava/lang/String;Ljava/lang/String;>;>;",
        null)
      mv.visitCode()

      // Load the `db` argument onto the stack
      mv.visitVarInsn(ALOAD, 0)
      // Invoke the `iterator` method on db, putting the iterator on the stack
      mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Seq", "iterator",
        "()Lscala/collection/Iterator;", true)
      // Store the iterator object at index 1
      mv.visitVarInsn(ASTORE, 1)

      // Stack size = 0

      // Instantiate a new ArrayBuffer `acc` {
      mv.visitTypeInsn(NEW, "scala/collection/mutable/ArrayBuffer")
      // Duplicate the reference to it on the stack
      mv.visitInsn(DUP)
      // Initialize the `acc` ArrayBuffer
      mv.visitMethodInsn(INVOKESPECIAL, "scala/collection/mutable/ArrayBuffer",
        "<init>", "()V", false)
      // Store the ArrayBuffer at index 2
      mv.visitVarInsn(ASTORE, 2)

      // Stack size = 0

      // A label is a point we can jump to with GOTO-style instructions. l0
      // marks the start of the while loop. The point at which the label is
      // visited represents its position and l0 is visited immediately.
      // while (...) {
      val l0 = new Label
      mv.visitLabel(l0)

      // Check the while condition
      // Load the iterator onto the stack from index 1
      mv.visitVarInsn(ALOAD, 1)
      // Call `hasNext` on the iterator, storing the boolean result on the
      // stack. The JVM stores boolean as int: 0 is false, 1 is true.
      mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Iterator",
        "hasNext", "()Z", true)

      // Stack size = 1, hasNext boolean

      // Create another jump location for the end of the loop. l1 isn't visited
      // until later at the end of the loop body but we need to create the label
      // here in order to reference it in `IFEQ`.
      val l1 = new Label
      // A jump instruction with IFEQ ("if equals") checks the current value on
      // the stack. If it's 0 (false) it jumps to the label, thus ending our
      // while loop.
      mv.visitJumpInsn(IFEQ, l1)

      // Stack size = 0

      // Load iterator onto the stack again
      mv.visitVarInsn(ALOAD, 1)
      // Obtain the `row` value from the iterator
      mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Iterator",
        "next", "()Ljava/lang/Object;", true)
      // Ensure the value is of expected type, Tuple3. This instruction pops a
      // value off the stack, checks it, then puts it back on the stack.
      mv.visitTypeInsn(CHECKCAST, "scala/Tuple3")
      // Store the row Tuple3 at local variable index 3
      mv.visitVarInsn(ASTORE, 3)
      // Load it again
      mv.visitVarInsn(ALOAD, 3)

      // Stack size = 1, row Tuple3

      // Invoke the `_2` method on the row to get the birthYear
      mv.visitMethodInsn(INVOKEVIRTUAL, "scala/Tuple3", "_2", "()Ljava/lang/Object;", false)
      // Ensure the expected type, Integer
      mv.visitTypeInsn(CHECKCAST, "java/lang/Integer")
      // Store birthYear at local var 4
      mv.visitVarInsn(ASTORE, 4)
      // Load birthYear from local var 4
      mv.visitVarInsn(ALOAD, 4)
      // Invoke the `intValue` method on birthYear
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
      // Push a short constant on to the stack
      mv.visitIntInsn(SIPUSH, 1910)

      // Stack size = 2, birthYear int, 1910 short

      // Any time we need to branch in some way, we need labels and jump
      // instructions. l2 marks the end of the filtering if statement, allowing
      //
      // us to jump over the body.
      val l2 = new Label()

      // Jump instructions are always the inverse predicate because if it
      // evaluates to true then it jumps, skipping the body of the if block.
      // IF_ICMPGE is short for "if int compare greater than or equal", so:
      // If value1 >= value2 then jump to l2, where
      // value1 = birthYear
      // value2 = 1910
      mv.visitJumpInsn(IF_ICMPGE, l2)
      // That's the first half of the if predicate. Now we check the other half.

      // Load the row Tuple3
      mv.visitVarInsn(ALOAD, 3)
      // Invoke the `_1` method on the row to get the name String
      mv.visitMethodInsn(INVOKEVIRTUAL, "scala/Tuple3", "_1", "()Ljava/lang/Object;", false)
      // This condition is much simpler and the JVM even has an instruction to
      // check for null. If the name String is null, jump to l2.
      mv.visitJumpInsn(IFNULL, l2)

      // Body of the if block {

        // Load the `acc` ArrayBuffer
        mv.visitVarInsn(ALOAD, 2)
        // Create a new Tuple2 to append to `acc`
        mv.visitTypeInsn(NEW, "scala/Tuple2")
        // Duplicate the reference to the Tuple2
        mv.visitInsn(DUP)
        // Load the `row` Tuple3
        mv.visitVarInsn(ALOAD, 3)
        // Invoke the `_1` method on the Tuple3, obtaining the name String
        mv.visitMethodInsn(INVOKEVIRTUAL, "scala/Tuple3", "_1", "()Ljava/lang/Object;", false)
        // Load the `row` Tuple3 again
        mv.visitVarInsn(ALOAD, 3)
        // Invoke the `_3` method on the Tupl3, obtaining the dissertation String
        mv.visitMethodInsn(INVOKEVIRTUAL, "scala/Tuple3", "_3", "()Ljava/lang/Object;", false)
        // Initialize the Tuple2 with the name and dissertation objects, pulling
        // them off the stack
        mv.visitMethodInsn(INVOKESPECIAL, "scala/Tuple2", "<init>",
          "(Ljava/lang/Object;Ljava/lang/Object;)V",
          false)
        // Append the Tuple2 to `acc`
        mv.visitMethodInsn(INVOKEVIRTUAL, "scala/collection/mutable/ArrayBuffer",
        "$plus$eq",
        "(Ljava/lang/Object;)Lscala/collection/mutable/ArrayBuffer;", false)
        // Discard the last item on the stack since we no longer need it.
        mv.visitInsn(POP)

      // }

      // Mark the end of the if block
      mv.visitLabel(l2)

      // Jump back to the start of the while loop
      mv.visitJumpInsn(GOTO, l0)
      // Mark the end of the while loop
      mv.visitLabel(l1)
      // } // end while

      // Load the acc Tuple3
      mv.visitVarInsn(ALOAD, 2)
      // Return the object on the stack
      mv.visitInsn(ARETURN)
      // Compute the max stack size and number of local vars (computed
      // automatically for us via COMPUTE_FRAMES)
      mv.visitMaxs(0, 0)
      // End the method
      mv.visitEnd()
    }

    // End the class
    cw.visitEnd()

    // Return the bytes representing a generated classfile
    cw.toByteArray
  }

  def classFrom(bytes: Array[Byte]) = {
    import scala.collection.mutable
    class DynamicClassLoader extends ClassLoader {
      val bytecodes = mutable.Map.empty[String, Array[Byte]]
      def putClass(name: String, bytecode: Array[Byte]) =
        bytecodes.put(name, bytecode)
      override def findClass(name: String): Class[_] =
        bytecodes.get(name).map { bytes =>
          defineClass(name, bytes, 0, bytes.length)
        }.getOrElse(super.findClass(name))
    }

    // Use ASM to generate a query
    val loader = new DynamicClassLoader
    loader.putClass(generatedClassName, bytes)

    val query: BaseQuery[JavaResult] =
      loader.findClass(className).newInstance.asInstanceOf[Query]


  }

}

