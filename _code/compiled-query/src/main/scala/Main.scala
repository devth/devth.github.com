import scalaz._, Scalaz._

object Main {
  import Database.FilterExpr, Operators._

  // SELECT name, dissertation
  val projections = Seq(0, 2)

  // WHERE name != null AND birthYear < 1910
  val filterExpr: FilterExpr = And.node(
    IsNotNull.node(
      Item.node("name".leaf)),
    LessThan.node(
      Item.node("birthYear".leaf),
      Literal.node("1910".leaf)))


  // Demo
  def main(args: Array[String]) {
    val result = InterpretedQuery.query(Database.db, projections, filterExpr)
    println("Interpreted results:")
    result.map(println)

    println("Static results:")
    StaticQuery.query(Database.db).map(println)

    StaticJavaQuery.query(Database.db)

  }
}
