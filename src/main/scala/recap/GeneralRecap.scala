package recap

object GeneralRecap extends App {
  val aCondition: Boolean = false
  var aVariable: Int = 42
  aVariable += 1
  val aCodeBlock = {
    if (aCondition) 74
    else 56
  }
  //val aUnit: Unit = print("hello")
  def aFunct(x: Int): Int = x + 1
  val af: Int => Int = (x: Int) => x + 1
  def fct(n: Int, acc: Int): Int =
    if (n <= 0) acc
    else fct(n - 1, acc * n)
  val nothing = try {
    throw new RuntimeException("fff")
  } catch {
    case exception: Exception =>
      println("aaa!")
  } finally {
    println("nooo")
  }
  print(nothing)
  val cvt = new Function[Int, String] {
    override def apply(v1: Int): String = "22"
  }
  val cvtr: Int => String = (x: Int) => s"333 $x"
  cvt(4)
  List(1, 3, 4, 5).map(cvtr)
  val pairs = for {
    num <- List(1, 2, 3, 4)
    char <- List('q', 'w', 'e', 'r')
  } yield num + "-" + char
  println(pairs.mkString("#"))
}
