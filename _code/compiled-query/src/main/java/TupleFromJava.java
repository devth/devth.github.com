import scala.Tuple2;

public class TupleFromJava {

  Tuple2<String, Integer> tup = new Tuple2<String, Integer>("foo", 2);

  public TupleFromJava() {
  }

  public Tuple2<String, Integer> getTup() {
    return tup;
  }

}
