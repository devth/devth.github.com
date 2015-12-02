
abstract class Query {
  import Database.FilterExpr
  def query(db: Seq[Product], projections: Seq[Int], filter: FilterExpr): Seq[Row]
}
