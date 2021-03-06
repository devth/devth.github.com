import scalaz._, Scalaz._

/** Run with `scala db.scala` */
object Database {

  import Operators._

  type FilterExpr = Tree[String]

  // Store a mapping of column name to ordinal for fast projection
  val schema: Map[String, Int] = Seq("name", "birthYear", "dissertation").zipWithIndex.toMap

  val db = Seq(
    ("John McCarthy", 1927, "Projection Operators and Partial Differential Equations."),
    ("Haskell Curry", 1900, "Grundlagen der kombinatorischen Logik"),
    ("Philip Wadler", 1956, "Listlessness is Better than Laziness"),
    ("Alonzo Church", 1903, "Alternatives to Zermelo's Assumption"),
    ("Alan Turing", 1912, "Systems of Logic based on Ordinals")
  )

  class FilterInterpreter(expr: FilterExpr, schema: Map[String, Int]) {
    /** return true if row is filtered out by expr */
    def isFiltered(row: Product): Boolean = evalFilterOn(row)

    private def evalFilterOn(row: Product): Boolean = {
      def eval(expr: FilterExpr): Boolean = {
        val (operator, operands: Stream[FilterExpr]) = (expr.rootLabel, expr.subForest)
        operator match {
          case And => operands.forall(eval)
          case Or => operands.exists(eval)
          case IsNotNull => valueFor(operands(0)) != null
          case LessThan => {
            val (x :: y :: _) = operands.map(o => valueFor(o).toString.toInt).toList
            x < y
          }
        }
      }
      def valueFor(node: FilterExpr) = node.rootLabel match {
        // Item expects a single operand
        case Item => row.productElement(schema(node.subForest.head.rootLabel))
        // Literal expects a single operand
        case Literal => node.subForest.head.rootLabel
      }
      eval(expr)
    }
  }

  // Query loop. Scans the table, applying filtering and projection along the
  // way.
  def query(projections: Seq[Int], filter: FilterExpr): Seq[Seq[Any]] = {
    val filterer = new FilterInterpreter(filter, schema)
    db.flatMap { row =>
      // Filter
      if (filterer.isFiltered(row)) None
      // Project
      else Some(projections.map(row.productElement))
    }
  }

  // Demo
  def main(args: Array[String]) {
    // i.e. SELECT name, dissertation
    val projections = Seq(0, 2)

    val filterExpr: FilterExpr = And.node(
      IsNotNull.node(
        Item.node("name".leaf)),
      LessThan.node(
        Item.node("birthYear".leaf),
        Literal.node("1910".leaf)))

    // i.e. WHERE name != null AND birthYear < 1910

    // val filterExpr: FilterExpr = ("AND",
    //   Seq(
    //     ("IS_NOT_NULL", Seq(("item", "name"))),
    //     ("LESS_THAN", Seq(("item", "birthYear"), ("literal", 1910)))))

    val result = query(projections, filterExpr)
    result.map(println)
  }

}
