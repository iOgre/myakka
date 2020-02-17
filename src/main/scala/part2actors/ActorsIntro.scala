package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsIntro extends App {
  val actorSystem = ActorSystem("firstActorSystem")
  class WordCountActor extends Actor {
   //internal data
   var totalWords = 0
   //behavior
    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
      println(s"[word counter] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }

 val wordCounter: ActorRef = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter: ActorRef = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")
  wordCounter ! "I am learning akka and it is pretty damn cool"
  anotherWordCounter ! "A different message"

  class Person(name:String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

  object Person {
    def props(name:String) = Props(new Person(name))
  }

  val person = actorSystem.actorOf(Person.props("Steve"))
  person ! "hi"




}
