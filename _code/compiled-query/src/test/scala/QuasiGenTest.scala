import org.scalatest.FunSuite

import reflect.runtime._, universe._, tools.reflect.ToolBox

package p {
  trait A { def a: String }
}

class QuasiGenTest extends FunSuite {

  // val universe: scala.reflect.runtime.universe.type = scala.reflect.runtime.universe
  // import universe._

  import p._

  val toolbox = currentMirror.mkToolBox()
  val tb = currentMirror.mkToolBox()

  test("code gen Query") {
  }

  test("pretty print") {
    val q = q"package mypackage { class MyClass }"
    println {
      showCode(q)
    }
  }

  test("2nd try") {


    // tb.compile(tb.parse("""case class Child(j: Int) extends Parent(42);"""))()
    // val tb = runtimeMirror(getClass.getClassLoader).mkToolBox()

    println("generated:")
    println {
      val d: A = tb.eval(q"""class C extends p.A { def a = "foo" }; classOf[C]""")
        .asInstanceOf[Class[_]].newInstance.asInstanceOf[A]
      d.a
    }

    // val tb = currentMirror.mkToolBox()
    // tb: scala.tools.reflect.ToolBox[reflect.runtime.universe.type] = scala.tools.reflect.ToolBoxFactory$ToolBoxImpl@59a67c3a
    // tb.compile(tb.parse("""case class Child(j: Int) extends Parent(42) ; val c = Child(17) ; c.j"""))()
    // res1: Any = 17

  }

  test("REPL") {
    // val tree = q"i am { a quasiquote }"
    // println(tree)

    // println(q"what is {}")

    // val t = q"one { $tree }"
    // println(s"t: $t")

    // val tmatch = t match { case q"one { $tree }" => true }
    // println(tmatch)

    // println(q"foo + bar" equalsStructure q"foo.+(bar)")

    // val q"i am $what" = q"i am { a quasiquote }"
    // println(what)

    // val x = q"""
    //      val x: List[Int] = List(1, 2) match {
    //        case List(a, b) => List(a + b)
    //      }
    //    """
    // println(x)

    // println(x.getClass)

    // val code = q"""println("compiled and run at runtime!")"""
    // val compiledCode = toolbox.compile(code)
    // println(compiledCode)
    // val result = compiledCode()

    // class A { def a = 2 }

    // val c = toolbox.compile(q"""class C(x: Int) { def r = x }""")
    // println(c())

  }

}

