import org.scalameter.api._

object QueryBenchmark extends PerformanceTest.Quickbenchmark {

  val birthYears = Gen.range("birthYears")(1900, 1920, 1)

  val records: Gen[Seq[(String, Integer, String)]] = for {
    birthYear <- birthYears
  } yield (0 until birthYear).map { i: Int => ("name", i: Integer, "diss") }

  performance of "Querying" in {

    measure method "StaticQuery" in {
      using (records) in { StaticQuery.query(_) }
    }

    measure method "InterpretedQuery" in {
      using (records) in {
        InterpretedQuery.query(_, Main.projections, Main.filterExpr)
      }
    }

    measure method "StaticJavaQuery" in {
      using (records) in { r =>
        val res = StaticJavaQuery.query(r)
        println(s"StaticJavaQuery size: ${res.size}")
      }
    }

  }

}
