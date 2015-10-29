trait Query {
  import Database.FilterExpr
  def query(db: Seq[(String, Integer, String)], projections: Seq[Int], filter: FilterExpr): Product
}
