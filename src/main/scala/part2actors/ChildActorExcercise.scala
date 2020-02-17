package part2actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import part2actors.ChildActorExcercise.WordCounterMaster.{Initialize, WordCountReply, WordCountTask}

object ChildActorExcercise extends App {
  //Distributed word counting
  class WordCounterMaster extends Actor with ActorLogging {

    override def receive: Receive = {
      case Initialize(number) =>
        log.info(s"[master] initialize with $number")
        val children: Seq[ActorRef] = (1 to number).map(i => context.actorOf(Props[WordCounterWorker], s"wcw-$i"))
        context.become(withChildren(children, 0, 0, Map()))
    }
    def withChildren(childrenRefs: Seq[ActorRef], currentIndex: Int, currentTaskId: Int, requestMap:Map[Int, ActorRef]): Receive = {
      case text: String =>
        log.info(s"[master] I have received [$text] and will send it to child $currentIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentIndex)
        childRef ! task
        val newIndex = (currentIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs = childrenRefs, currentIndex = newIndex,
          currentTaskId = newTaskId, newRequestMap))
      case WordCountReply(id, count) =>
        log.info(s"[master] I have received a reply for task $id with $count ")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentIndex, currentTaskId, requestMap - id))
    }
  }
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }

  class WordCounterWorker extends Actor with ActorLogging {
    override def receive: Receive = {
      case WordCountTask(id, text) =>
        log.info(s"${self.path} I have received task $id with text $text")
        sender() ! WordCountReply(id, count = text.split(" ").length)
    }
  }

  class TestActor extends Actor with ActorLogging  {
    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("I love akka", "Scala is very cool", "yes", "me too")
        texts.foreach(text => master ! text)
      case count:Int =>
        log.info(s"[test actor] I have received a reply $count")
    }
  }

  val system = ActorSystem("roundRobbinWordCounter")
  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "go"


}
