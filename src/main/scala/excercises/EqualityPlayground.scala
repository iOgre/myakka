package excercises

import part4implicits.Typeclasses.User
import EqualImplicits.FullEquality
import Equal.Enrich

object EqualityPlayground extends App {
  val joe = User("John", 32, "boo@email")
  val john = User("John", 32, "some@email")
  println(joe === john)
  /*
   Implement TC pattern for equality
    */
  //Equal typeclass
  //AD-HOC polymorphism
}

//Extend equal TC with implicit conversion class
// ===(anotherValue: T)
// !==(anotherValue: T)