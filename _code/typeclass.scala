// Running this code:
// scala -Dscala.color -feature -classpath ~/.m2/raptor2/org/scalaz/scalaz-core_2.11/7.1.2/scalaz-core_2.11-7.1.2.jar typeclass.scala
import scalaz._, Scalaz._
import scala.language.implicitConversions
import scala.language.postfixOps

object Typeclass {

  class C(val name: String)

  /// instances for C

  implicit val equalC: Equal[C] = new Equal[C] {
    override def equal(c1: C, c2: C) = c1.name == c2.name
  }

  implicit val showC: Show[C] = new Show[C] {
    override def show(c: C) = s"C[name=${c.name}]"
  }

  ///

  final class SimpleOps[F](val self: F)(implicit val F: Simple[F]) {
    final def /^ = F.simplify(self)
    final def ⬈ = F.simplify(self)
  }

  trait ToSimpleOps {
    implicit def ToSimpleOps[F](v: F)(implicit F0: Simple[F]) =
      new SimpleOps[F](v)
  }

  object simple extends ToSimpleOps

  trait SimpleSyntax[F] {
    def F: Simple[F]
    implicit def ToSimpleOps(v: F): SimpleOps[F] =
      new SimpleOps[F](v)(SimpleSyntax.this.F)
  }

  trait Simple[F] { self =>
    def simplify(f: F): String
    val simpleSyntax = new SimpleSyntax[F] { def F = Simple.this }
  }

  ///

  def manySimple[A](simples: Seq[A])(implicit s: Simple[A]): String =
    "Many simples:\n\t" + simples.map(s.simplify).mkString("\n\t")

  /// instances

  implicit def simpleC: Simple[C] = new Simple[C] {
    override def simplify(c: C) = s"Simplified: ${c.show}"
  }

  implicit val simpleInt: Simple[Int] = new Simple[Int] {
    override def simplify(i: Int) = s"Simplified Int with value of $i"
  }

  implicit def simpleList[A: Simple]: Simple[List[A]] = new Simple[List[A]] {
   override def simplify(l: List[A]) = {
     val simplifyA = implicitly[Simple[A]].simplify _
     s"Simplified list:\n${l.map(simplifyA).mkString("\n")}"
   }
  }

  /// usage

  def main(args: Array[String]) {
    // Bring implicit `ToSimpleOps` method in scope
    import simple._
    List(
      1 ⬈,
      1 ⬈,
      new C("I am C") ⬈,
      List(1,2,3) ⬈,
      new C("bar") /^,
      List(new C("foo"), new C("bar"), new C("baz")) /^
    ).map(println)
  }

}
