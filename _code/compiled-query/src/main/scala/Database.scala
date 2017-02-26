import scalaz._, Scalaz._

/** Run with `scala db.scala` */
object Database {

  type FilterExpr = Tree[String]

  // Store a mapping of column name to ordinal for fast projection
  val schema: Map[String, Int] = Seq("name", "birthYear", "dissertation").zipWithIndex.toMap

  val db: Seq[(String, Integer, String)] = Seq(
    ("John McCarthy", 1927, "Projection Operators and Partial Differential Equations."),
    ("Haskell Curry", 1900, "Grundlagen der kombinatorischen Logik"),
    ("Philip Wadler", 1956, "Listlessness is Better than Laziness"),
    ("Alonzo Church", 1903, "Alternatives to Zermelo's Assumption"),
    ("Alan Turing", 1912, "Systems of Logic based on Ordinals")
  )

}
