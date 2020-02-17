package part4implicits

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object MagnetPattern extends App {

  //method overloading
  class P2PRequest

  class P2PResponse

  class Serializer[T]

  trait Actor {
    def receive(statusCode: Int): Int

    def receive(request: P2PRequest): Int

    def receive(response: P2PResponse): Int

    def receive[T: Serializer](message: T): Int

    def receive[T: Serializer](message: T, statusCode: Int): Int

    def receive(future: Future[P2PRequest]): Int

    //type erasure ((
    //def receive(future: Future[P2PResponse]): Int
    //lots of overloads
  }

  /*
  1. type erasure
  2. lifting does not work for all overloads
  val receiveFV = receive _ //?!
  3. code duplication
  4. type inference and default args
    actor.receive(?!)
   */

  //magnet pattern
  trait MessageMagnet[Result] {
    def apply(): Result
  }

  def receive[R](magnet: MessageMagnet[R]): R = magnet()

  implicit class FromP2PRequest(request: P2PRequest) extends MessageMagnet[Int] {
    override def apply(): Int = {
      //handling p2prequest
      println("handling p2p request")
      42
    }
  }

  implicit class FromP2PResponse(request: P2PResponse) extends MessageMagnet[Int] {
    override def apply(): Int = {
      //handling p2prequest
      println("handling p2p response")
      24
    }
  }

  //benefits -
  // 1. no more type erasures
  implicit class FromResponseFuture(future: Future[P2PResponse]) extends MessageMagnet[Int] {
    override def apply(): Int = 1
  }

  implicit class FromRequestFuture(future: Future[P2PRequest]) extends MessageMagnet[Int] {
    override def apply(): Int = 2
  }

  println(receive(Future(new P2PResponse)))
  println(receive(Future(new P2PRequest)))
  receive(new P2PRequest)
  receive(new P2PResponse)

  //lifting
  trait MathLib {
    def add1(x: Int): Int = x + 1

    def add1(s: String): Int = s.toInt + 1

    //other add1 overloads
  }

  trait AddMagnet {
    def apply(): Int
  }

  def add1(magnet: AddMagnet): Int = magnet()

  implicit class AddInt(x: Int) extends AddMagnet {
    override def apply(): Int = x + 1
  }

  implicit class AddString(s: String) extends AddMagnet {
    override def apply(): Int = s.toInt + 1
  }

  val add1FV = add1 _
  println(add1FV(77))
  println(add1FV("12"))
  //but this will not work for
  val receiveFV = receive _

  //because
  // val receiveFV: MessageMagnet[Nothing] => Nothing = receive _

  /*  other drawbacks
    1. verbose
    2. harder to read
    3. can't name or place default arguments
    4. call by name doesn't work correctly
   */

  //call-by-name example does not work correctly

  class Handler {
    def handle(s: => String): Unit = {
      println(s)
      println(s)
    }

    //other overloads
  }

  trait HandleMagnet {
    def apply(): Unit
  }

  def handle(magnet: HandleMagnet): Unit = magnet()

  implicit class StringHandle(s: =>String) extends HandleMagnet {
    override def apply(): Unit = {
      println(s)
      println(s)
    }
  }

  def sideEffectMethod():String = {
    println("Side effect")
    "hahaha"
  }

  handle(sideEffectMethod())
  println("-------------")

  handle {
    println("Side effect")
      println("Another Side effect")
    "hahaha"
  }

}
