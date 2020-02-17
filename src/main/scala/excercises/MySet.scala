package excercises

import scala.annotation.tailrec

trait MySet[A] extends (A => Boolean) {
  def contains(elem: A): Boolean

  def +(elem: A): MySet[A]

  def ++(anotherSet: MySet[A]): MySet[A] //union

  def map[B](f: A => B): MySet[B]

  def flatMap[B](f: A => MySet[B]): MySet[B]

  def filter(predicate: A => Boolean): MySet[A]

  def foreach(f: A => Unit): Unit

  def apply(v1: A): Boolean = contains(v1)

  /*
  Ex: removing an element
  intersection with another set *
  difference \
   */
  def -(elem: A): MySet[A]

  def &(anotherSet: MySet[A]): MySet[A] //intersection

  def --(anotherSet: MySet[A]): MySet[A] //difference

  def unary_! : MySet[A]
}

object MySet {
  def apply[A](values: A*): MySet[A] = {
    @tailrec
    def buildSet(valSeq: Seq[A], acc: MySet[A]): MySet[A] = if (valSeq.isEmpty) acc
    else buildSet(valSeq.tail, acc + valSeq.head)

    buildSet(values.toSeq, new EmptySet[A])
  }

}

class EmptySet[A] extends MySet[A] {
  override def contains(elem: A): Boolean = false

  override def +(elem: A): MySet[A] = new NonEmptySet[A](elem, this)

  override def ++(anotherSet: MySet[A]): MySet[A] = anotherSet

  override def map[B](f: A => B): MySet[B] = new EmptySet[B]

  override def flatMap[B](f: A => MySet[B]): MySet[B] = new EmptySet[B]

  override def filter(predicate: A => Boolean): MySet[A] = this

  override def foreach(f: A => Unit): Unit = ()

  override def -(elem: A): MySet[A] = this

  override def &(anotherSet: MySet[A]): MySet[A] = this

  override def --(anotherSet: MySet[A]): MySet[A] = this

  override def unary_! : MySet[A] = ???
}

class NonEmptySet[A](head: A, tail: MySet[A]) extends MySet[A] {
  override def contains(elem: A): Boolean = elem == head || tail.contains(elem)

  override def +(elem: A): MySet[A] = if (this contains elem) this else new NonEmptySet[A](elem, this)

  /*
  [1 2 3] ++ [4 5] =
  [2 3] ++ [4 5] + 1 =
  [3] ++ [4 5] + 1 + 2 =
  [] ++ [4 5] + 1 + 2 + 3 =
  [4 5] + 1 + 2 + 3
  [4 5 1 2 3]
   */
  override def ++(anotherSet: MySet[A]): MySet[A] = tail ++ anotherSet + head

  override def map[B](f: A => B): MySet[B] = tail.map(f) + f(head)

  override def flatMap[B](f: A => MySet[B]): MySet[B] = tail.flatMap(f) ++ f(head)

  override def filter(predicate: A => Boolean): MySet[A] = {
    val filteredTail = tail filter predicate
    if (predicate(head)) filteredTail + head
    else filteredTail
  }

  override def foreach(f: A => Unit): Unit = {
    f(head)
    tail foreach f
  }

  override def -(elem: A): MySet[A] = if (head == elem) tail
  else tail - elem + head

  override def &(anotherSet: MySet[A]): MySet[A] = filter(anotherSet) //intersect == filter

  override def --(anotherSet: MySet[A]): MySet[A] = filter(!anotherSet(_))

  override def unary_! : MySet[A] = ???
}

object MyTestPlayground extends App {
  val s = MySet(1, 2, 3, 4)
  s + 5 ++ MySet(-1, -2) + 3 flatMap (x => MySet(x, 10 * x)) filter (_ % 2 != 0) foreach println

}
