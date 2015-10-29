object StaticQuery {

  def query(db: Seq[(String, Integer, String)]) = {
    db.flatMap { case (name, birthYear, dissertation) =>
      if (birthYear < 1910 && name != null) Some((name, dissertation))
      else None
    }
  }

}
