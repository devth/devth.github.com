/** Run with `scala db.scala` */
object Database {

  type FilterExpr = Tuple2[String, Seq[Product]]

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
      def eval(expr: Product): Boolean = {
        val (operator, operands: Seq[Product]) = expr
        operator match {
          case "AND" => operands.forall(eval)
          case "OR" => operands.exists(eval)
          case "IS_NOT_NULL" => valueFor(operands(0)) != null
          case "LESS_THAN" => {
            val (x :: y :: _) = operands.map(o => valueFor(o).asInstanceOf[Int])
            x < y
          }
        }
      }
      def valueFor(node: Product) = node match {
        case ("item", column: String) => row.productElement(schema(column))
        case ("literal", lit) => lit
      }
      eval(expr)
    }
  }

  // Query loop
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

    // i.e. WHERE name != null AND birthYear < 1910
    val filterExpr: FilterExpr = ("AND",
      Seq(
        ("IS_NOT_NULL", Seq(("item", "name"))),
        ("LESS_THAN", Seq(("item", "birthYear"), ("literal", 1910)))))

    val result = query(projections, filterExpr)
    result.map(println)
  }

}
