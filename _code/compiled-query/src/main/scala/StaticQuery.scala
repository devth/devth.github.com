object StaticQuery extends Query {
  import Database.FilterExpr

  def query(db: Seq[Product],
            projections: Seq[Int]=Seq.empty,
            filter: FilterExpr=null): Seq[Row] =
    db.flatMap { case (name: String, birthYear: Int, dissertation: String) =>
      if (birthYear < 1910 && name != null)
        Some(DissertationRow(name=Some(name), dissertation=Some(dissertation)))
      else
        None
    }

}
