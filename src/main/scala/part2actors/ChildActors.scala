package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.Parent.{CreateChild, Reset, TellChild}

object ChildActors extends App {

  class Parent extends Actor {

    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} FROM ${sender().path} Creating child $name")
        //creating new actor:
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }
    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
      case Reset => context.unbecome()
    }
  }

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
    case object Reset
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"[${self.path}] FROM ${sender().path} I got: $message")
    }
  }

  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("child")
  parent ! Reset
  parent ! CreateChild("Another")
  parent ! TellChild("Hey kijd")

  /**
   * Actor Selection
   */

  val actorSelection = system.actorSelection("user/parent/child")
  actorSelection ! "I found you"

  /**
   * NEVER PASS MUTABLE ACTOR STATE OR this to child actors
   *
   */

  object BadPractice extends App {
    import NaiveBankAccount._
    import CreditCard._

    class NaiveBankAccount extends Actor {
      var amount = 0

      import CreditCard._
      import NaiveBankAccount._

      override def receive: Receive = {
        case InitializeAccount =>
          val creditCardRef = context.actorOf(Props[CreditCard], "card")
          creditCardRef ! AttachToAccount(this)
        case Deposit(funds) => {
          println(s"${self.path} depositing $funds on top of $amount")
          deposit(funds)
        }
        case Withdraw(funds) => {
          println(s"${self.path} withdrawing $funds from top of $amount")
          withdraw(funds)
        }
      }

      def deposit(funds: Int) = amount += funds
      def withdraw(funds: Int) = amount -= funds
    }
    object NaiveBankAccount {
      case class Deposit(amount: Int)
      case class Withdraw(amount: Int)
      case object InitializeAccount
    }

    class CreditCard extends Actor {
      import CreditCard._

      override def receive: Receive = {
        case AttachToAccount(account) => context.become(attachedTo(account))
      }
      def attachedTo(account:NaiveBankAccount):Receive = {
        case CheckStatus =>
          println(s"${self.path} you message has been processed")
          account.withdraw(1) //because I can - access to actor internal state!!!

      }

    }
    object CreditCard {
      case class AttachToAccount(bankAccount: NaiveBankAccount) //!!
      case object CheckStatus
    }

    val bankAccountRef = system.actorOf(Props[NaiveBankAccount], "account")
    bankAccountRef ! InitializeAccount
    bankAccountRef ! Deposit(100)
    Thread.sleep(500)
    val ccSelection = system.actorSelection("/user/account/card")
    ccSelection ! CheckStatus
  }

}

