package part4implicits

object PimpMyLibrary extends App {

  //2.isPrime
  implicit class RichInt(value: Int) {
    def isEven: Boolean = value % 2 == 0
    def sqrt: Double = Math.sqrt(value)
    def times(f: () => Unit): Unit = {
      (1 to value).foreach(_ => f())
    }
    def *[T](list: List[T]): List[T] = {
      (1 to value).flatMap(_ => List.empty[T] ++ list).toList
    }
  }

  new RichInt(42).isEven
  42.sqrt

  //Enrich the String class
  // asInt string->int
  // encrypt : caesar cypher (2)
  // Enrich int with:
  // times(function) 3.times(() => ..)
  // * : 3*List(1,2) = List(1,2,1,2,1,2)
  implicit class RichString(value: String) {
    def asInt: Int = value.toInt
    def encrypt: String = value.map(c => (c + 2).toChar)
  }

  println("John".encrypt)
  3.times(() => println("hello"))
  println(1 * List(4, 5))
}
