import scalaz._, Scalaz._
import org.scalatest.FunSuite

class InterpretedQueryTest extends FunSuite {
  import Database.FilterExpr, Operators._
  StaticQuery.query(Database.db).map(println)

  test("interpreted query") {

    val projections = Seq(1)

    // WHERE name != null AND birthYear < 1910
    val filterExpr: FilterExpr = And.node(
      IsNotNull.node(
        Item.node("name".leaf)),
      LessThan.node(
        Item.node("birthYear".leaf),
        Literal.node("1910".leaf)))

    val result = InterpretedQuery.query(Database.db, projections, filterExpr)
    println("Interpreted query: $result")

  }
}

