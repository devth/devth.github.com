import scalaz._, Scalaz._

object InterpretedQuery extends Query {

  import Database.schema, Database.FilterExpr, Operators._

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
  def query(db: Seq[Product],
            projections: Seq[Int],
            filter: FilterExpr): Seq[Row] = {
    val filterer = new FilterInterpreter(filter, schema)
    db.flatMap { row =>
      // Filter
      if (filterer.isFiltered(row)) None
      // Project
      else Some {
        projections.foldLeft(DissertationRow()) { case (acc, i) =>
          acc.setValueForOrdinal(i, row.productElement(i))
        }
      }
    }
  }

}
