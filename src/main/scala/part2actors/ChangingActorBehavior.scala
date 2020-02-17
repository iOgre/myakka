package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.Mom.MomStart

object ChangingActorBehavior extends App {

  class FussyKid extends Actor {
    import part2actors.ChangingActorBehavior.FussyKid._
    import part2actors.ChangingActorBehavior.Mom._
    //internal state of the kid
    var state = HAPPY

    override def receive: Receive = {
      case Food(CHOCOLATE) => state = HAPPY
      case Food(VEGETABLE) => state = SAD
      case Ask(_) => if (state == HAPPY) sender() ! KidAccept else sender() ! KidReject
    }
  }

  object FussyKid {

    case object KidAccept

    case object KidReject

    val HAPPY = "happy"
    val SAD = "sad"
  }

  class Mom extends Actor {
    import part2actors.ChangingActorBehavior.FussyKid._
    import part2actors.ChangingActorBehavior.Mom._
    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("do you want to play")
      case KidAccept =>
        println("My kid is happy")
      case KidReject =>
        println("My kid is sad but healthy")
    }
  }

  object Mom {

    case class MomStart(kidRef: ActorRef)

    case class Food(food: String)

    case class Ask(message: String)

    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  val system = ActorSystem("changingActorBehaviorDemo")
  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])
  mom ! MomStart(statelessFussyKid)

  class StatelessFussyKid extends Actor {
    import part2actors.ChangingActorBehavior.FussyKid._
    import part2actors.ChangingActorBehavior.Mom._
    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false) //change receive handler to sadReceive
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }
  }

}
