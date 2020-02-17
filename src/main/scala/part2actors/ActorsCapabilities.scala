package part2actors

import akka.actor.Status.Success
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorExcercises.BankAccountActor.Withdraw
import part2actors.ActorExcercises.Person.Live

import scala.util.{Failure, Try}


object ActorsCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" => context.sender() ! "Hello there"
      case message: String => println(s"[${self}] I have received: $message")
      case number: Int => println(s"[simple actor] I have received a number: $number")
      case SpecialMessage(special) => println(s"[simple actor] I have received a SPECIAL message: $special")
      case SendMessageToYourself(content) =>
        self ! content
      case SayHiTo(ref) => ref ! "Hi"
      case WirelessPhoneMessage(content, ref) => ref forward content + "s"
    }
  }

  case class SpecialMessage(contents: String)

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  //Messages can be of any type, but
  //a) messages must be IMMUTABLE
  //b) messages must be SERIALIZABLE
  //In practice - use case classes and case objects
  /* simpleActor ! "hello actor!"
   simpleActor ! 42
   simpleActor ! SpecialMessage("This is special")*/

  //2 Actors have information about their context and themselves
  //context.self === this
  //context.self === self

  case class SendMessageToYourself(content: String)

  simpleActor ! SendMessageToYourself("I am actor and I am proud")

  //3 actors can reply to messages

  val alice = system.actorOf(Props[SimpleActor], "Alice")
  val bob = system.actorOf(Props[SimpleActor], "Bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  //4 dead letters

  alice ! "Hi"

  //5 forwarding

  case class WirelessPhoneMessage(content: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("Hi", bob)


}

object Looping extends App {
  val system = ActorSystem("actorLoops")

  case class Parrot(name: String, number: Int = 1) extends Actor {
    override def receive: Receive = {
      case message: String =>
        println(s"[$name # $number] I heard a voice: $message")
        val child = system.actorOf(Parrot.props("Gosha", number + 1))
        child ! "another message"
        self ! message + "!"
    }
  }

  object Parrot {
    def props(name: String, num: Int = 0) = Props(Parrot(name, num))
  }

  val parrot = system.actorOf(Parrot.props("Gosha"))

  parrot ! "."
}


object ActorExcercises extends App {

  import part2actors.ActorExcercises.CounterActor.{Increment, Print, Decrement}

  val system = ActorSystem("actorExcercises")

  val counterActor = system.actorOf(Props[CounterActor], "counter-1")
  (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 3).foreach(_ => counterActor ! Decrement)
  counterActor ! Print

  val account = system.actorOf(BankAccountActor.props(0))
  val person = system.actorOf(Props[Person], "itsme")
  person ! Live(account)

  /**
   * 1. counter actor
   * - Increment
   * - Decrement
   * - Print
   * 2. Bank account as an actor
   * receives
   *  - Deposit amount
   *  - Withdraw amount
   *  - Statement
   * reply with Success/Failure
   *
   */
  class CounterActor extends Actor {
    var internal: Int = 0

    override def receive: Receive = {
      case Print => println(internal)
      case Increment => internal += 1
      case Decrement => internal -= 1
    }
  }

  object CounterActor {

    case object Increment

    case object Decrement

    case object Print

  }

  class BankAccountActor(val initial: Int) extends Actor {

    import part2actors.ActorExcercises.BankAccountActor._

    var funds: Int = initial

    override def receive: Receive = {
      case Deposit(money) =>
        money match {
          case _ if money < 0 => sender() ! OperationFailure("Unable to deposit negative amount")
          case _ =>
            Try {
              Math.addExact(funds, money)
            } match {
              case Failure(exception: ArithmeticException) => sender() ! OperationFailure(exception.getMessage)
              case util.Success(value) =>
                funds = value
                sender() ! OperationSuccess
            }
        }

      case Withdraw(money) =>

        money match {
          case _ if money < 0 => sender() ! OperationFailure("Unable to withdraw negative amount")
          case _ if money > funds => sender() ! OperationFailure("Withdraw amount > current amount")
          case _ =>
            Try {
              Math.subtractExact(funds, money)
            } match {
              case Failure(exception: ArithmeticException) => sender() ! OperationFailure(exception.getMessage)
              case util.Success(value) =>
                funds = value
                sender() ! OperationSuccess
            }
        }
      case Statement =>
        println(s"Current account state: $funds")
    }
  }

  object BankAccountActor {

    def props(initial: Int) = Props(new BankAccountActor(initial))

    case object OperationSuccess

    case class OperationFailure(reason: String)

    sealed trait Commands

    case class Deposit(amount: Int)

    case class Withdraw(amount: Int)

    case object Statement

  }

  object Person {

    case class Live(account: ActorRef)

  }

  class Person extends Actor {

    import part2actors.ActorExcercises.BankAccountActor._
    import part2actors.ActorExcercises.Person.Live

    override def receive: Receive = {
      case Live(account) =>
        account ! Deposit(100000)
        account ! Withdraw(500000)
        account ! Withdraw(7000)
        account ! Statement
      case message => println(message.toString)
    }
  }


}
