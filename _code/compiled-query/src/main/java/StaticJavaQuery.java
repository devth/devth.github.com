import scala.Tuple2;
import scala.Tuple3;
import scala.collection.Seq;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.Iterator;

public class StaticJavaQuery {

  public static Seq<Tuple2<String, String>> query(Seq<Tuple3<String, Integer, String>> db) {
    Iterator<Tuple3<String, Integer, String>> iter = db.iterator();
    ArrayBuffer<Tuple2<String, String>> acc =
      new ArrayBuffer<Tuple2<String, String>>();
    while (iter.hasNext()) {
      Tuple3<String, Integer, String> row = iter.next();
      Integer birthYear = row._2();
      if (birthYear.intValue() < 1910 && row._1() != null) {
        acc.$plus$eq(new Tuple2<String, String>(
          row._1(), row._3()));
      }
    }
    return acc;
  }

}
