package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}


object StatelessCounterExercise extends App {

  import part2actors.StatelessCounterExercise.Counter._

  class Counter extends Actor {


    def counterReceive(current: Int): Receive = {
      case Increase => context.become(counterReceive(current + 1))
      case Decrease => context.become(counterReceive(current - 1))
      case Print => println(s"[counter] - current = $current")
    }


    override def receive: Receive = counterReceive(0)
  }

  object Counter {

    case object Increase

    case object Decrease

    case object Print

  }

  val system = ActorSystem("counter")
  val counter = system.actorOf(Props[Counter])
  counter ! Increase
  counter ! Increase
  counter ! Increase
  counter ! Decrease
  counter ! Decrease
  counter ! Decrease
  counter ! Decrease
  counter ! Decrease
  counter ! Print
}

object ActorBehaviorExcercises extends App {

  /*
  1. counter actor without internal state
  2. simplified voting system
   */
  //1


  //2
  case class Vote(candidate: String)

  case class VoteStatusReply(candidate: Option[String])

  case object VoteStatusRequest

  class Citizen extends Actor {
    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }

    override def receive: Receive = {
      case Vote(candidate) => context.become(voted(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])

  case object Results

  class VoteAggregator extends Actor {

    def aggregated(voteResult: Map[String, Int]): Receive = {
      case VoteStatusReply(None) =>
        //  println(s"[${sender()}] Not voted")
        sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
       val updatedVr = voteResult + voteResult.get(candidate).map(k => candidate -> (k + 1)).getOrElse(candidate -> 1)
        context.become(aggregated(updatedVr))
      //  println(s"[${sender()}] Voted for $candidate")
      case AggregateVotes(citizens) =>
        citizens.foreach(ct => ct ! VoteStatusRequest)
        self ! Results
      case Results =>
        println(s"vote result: $voteResult")
    }

    override def receive: Receive = aggregated(Map())
  }

  val system = ActorSystem("voting")
  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val john = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val jade = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  john ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, john))
  /*
  prints status of all votes
    Martin -> 1
    Jonas -> 1
    Roland -> 2
   */


}
