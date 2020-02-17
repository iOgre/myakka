package excercises

import part4implicits.Typeclasses.User

trait Equal[T] {
  def apply(one: T, other: T): Boolean
}

object Equal {
  def apply[T](a: T, b: T)(implicit instance: Equal[T]): Boolean = instance(a, b)

  implicit class Enrich[T](value: T)(implicit instance: Equal[T]) {
    def ===(another: T): Boolean = Equal(value, another)
    def !==(another: T): Boolean = !Equal(value, another)
  }

}

object EqualImplicits {

  implicit object NameEqual extends Equal[User] {
    override def apply(one: User, other: User): Boolean = one.name == other.name
  }

  implicit object FullEquality extends Equal[User] {
    override def apply(one: User, other: User): Boolean = one.name == other.name && one.email == other.email
  }

}

