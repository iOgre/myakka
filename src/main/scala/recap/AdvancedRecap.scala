package recap

import scala.concurrent.Future

object AdvancedRecap extends App {
  //partial functions - operates on subset of domain
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }
  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }
  val function: (Int => Int) = partialFunction
  val modlist = List(1, 2, 3).map {
    case 1 => 42
    case _ => 0
  }
  val lifted: Int => Option[Int] = partialFunction.lift //lifts Int => Int pf to Int => Option[Int] total function
  lifted(3) // Some(65)
  lifted(5000) //None
  val pfChain: PartialFunction[Int, Int] = partialFunction orElse {
    case 60 => 9000
  }
  pfChain(5) //999 => original fnc
  pfChain(60) //9000 => chained fnc
  pfChain(493) //throw a matcher
  //type aliases
  type ReceiveFunction = PartialFunction[Any, Unit]
  def receive: ReceiveFunction = {
    case 1 => println("hello")
    case "b" => println("good bye")
    case _ => println("confused")
  }
  //implicits
  implicit val timeout = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()
  setTimeout(() => println("timrout")) //extra parameter omitted
  //imlicit convertions
  // implicit defs
  case class Person(name: String) {
    def greet = s"H, mn is $name"
  }

  implicit def stringToPerson(string: String): Person = Person(string)
  "Peter".greet

  //implicit classes
  implicit class Dog(name: String) {
    def bark = println("bark")
  }

  implicit class Cat(name: String) {
    def mew = println("mew")
  }

  "Lassie".bark
  "Mimi".mew
  //organize
  //local scope
  implicit val inv: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1, 2, 3).sorted //List(3,2,1)
  //imported scope

  import scala.concurrent.ExecutionContext.Implicits.global

  val future = Future {
    println("helli fut")
  }
}
