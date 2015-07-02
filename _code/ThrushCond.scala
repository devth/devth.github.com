/***
scalaVersion := "2.11.6"

libraryDependencies ++= Seq("org.scalaz" %% "scalaz-core" % "7.1.0")
*/

// Code listing for http://devth.com/2015/thrush-cond-is-not-a-monad/
object Main extends App {

  import scalaz._, Scalaz._

  type Step[A] = (A => Boolean, A => A)

  case class Request(
    target: String,
    params: Map[String, String] = Map.empty,
    headers: Map[String, String] = Map.empty) {
    // validation and helper functions
    val acceptMap = Map("html" -> "text/html", "json" -> "application/json")
    val isValidAccept: (String => Boolean) = acceptMap.isDefinedAt _
    def addParam(k: String, v: String) = this.copy(params=params.updated(k, v))
    def addHeader(k: String, v: String) = this.copy(headers=headers.updated(k, v))
  }

  case class ThrushCond[A](steps: Step[A]*) {
    /** Perform a pipeline step only if the value meets a predicate */
    def guard[A](pred: (A => Boolean), fn: (A => A)): (A => A) =
      (a: A) => if (pred(a)) fn(a) else a
    /** Compose the steps into a single function */
    def comp = Function.chain(steps.map { step => guard(step._1, step._2) })
    /** Run a value through the pipeline */
    def run(a: A) = comp(a)
  }

  case object ThrushCond {
    /** Evidence of a PlusEmpty */
    implicit def thrushCondPlusEmpty: PlusEmpty[ThrushCond] =
      new PlusEmpty[ThrushCond] {
        def plus[A](a: ThrushCond[A], b: => ThrushCond[A]): ThrushCond[A] =
          ThrushCond[A]((Function.const(true), b.comp compose a.comp))

        def empty[A]: ThrushCond[A] = ThrushCond[A]()
      }
    /** Use PlusEmpty to provide evidence of a Monoid[Request] */
    implicit def requestMonoid: Monoid[ThrushCond[Request]] =
      thrushCondPlusEmpty.monoid[Request]
    /** Evidence of a Semigroup */
    implicit def thrushCondSemigroup[A]: Semigroup[ThrushCond[A]] =
      new Semigroup[ThrushCond[A]] {
        def append(t1: ThrushCond[A], t2: => ThrushCond[A]): ThrushCond[A] =
          ThrushCond[A]((Function.const(true), t2.comp compose t1.comp))
      }
  }

  import ThrushCond._ // evidence
  import scala.language.postfixOps

  //
  // Examples
  //
  // sample values from user input
  val userId: Int = 1
  val userName: Option[String] = Some("devth")
  val address: Option[String] = None
  val accept = "json"
  val shouldCache = false
  val req = Request("/users")

  val userPipeline = ThrushCond[Request](
    ({_ => userName.isDefined}, {_.addParam("userName", userName.get)}),
    ({_ => address.isDefined}, {_.addParam("address", address.get)}))

  val headerPipeline = ThrushCond[Request](
    ({_.isValidAccept(accept)}, {req =>
      req.addHeader("accept", req.acceptMap(accept))}))

  //
  // Semigroup
  //
  println("Semigroup:")
  println(userPipeline |+| headerPipeline run req)

  //
  // PlusEmpty
  //
  println("PlusEmpty:")
  println(userPipeline <+> headerPipeline run req)

  //
  // Monoid
  //
  val cachePipeline = ThrushCond[Request](
    ({_ => !shouldCache}, {_.addHeader("cache-control", "no-cache")}))

  val pipeline = List(userPipeline, headerPipeline, cachePipeline) suml

  println("Monoid:")
  println(pipeline run req)

  //
  // Output
  //
  // Semigroup:
  // Request(/users,Map(userName -> devth),Map(accept -> application/json))
  // PlusEmpty:
  // Request(/users,Map(userName -> devth),Map(accept -> application/json))
  // Monoid:
  // Request(/users,Map(userName -> devth),Map(accept -> application/json,
  // cache-control -> no-cache))

}
