import org.scalatest.FunSuite

class StaticQueryTest extends FunSuite {
  import Database.FilterExpr, Operators._
  StaticQuery.query(Database.db).map(println)

  test("static query") {
    println {
      StaticQuery.query(Database.db)
    }
  }
}
