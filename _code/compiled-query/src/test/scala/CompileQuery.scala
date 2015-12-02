import org.scalatest.FunSuite

class CompiledQueryGenTest extends FunSuite {

  test("It can compile") {

    val bytes = CompiledQueryGen.generate
    // val q = CompiledQueryGen.classFrom(bytes)

  }

}
