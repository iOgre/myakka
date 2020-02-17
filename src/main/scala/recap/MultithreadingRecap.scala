package recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {
  //creating threads on the jvm
  val aThread = new Thread(() => println("running at parallel"))
  // aThread.start()
  //aThread.join()
  val threadHello = new Thread(() => (1 to 1000).foreach(t => println(s"$t hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(t => println(s"$t goodbye")))
  // threadHello.start()
  //threadGoodbye.start()
  //different runs produce different results
  //scala Futures

  import scala.concurrent.ExecutionContext.Implicits.global

  val future = Future {
    //long computation - on different thread
    42
  }
  val aProcessedFuture: Future[Int] = future.map(_ + 1)
  val aFlatFuture = future.flatMap {
    value => Future(value + 2)
  } //F with 44
  val filteredFuture = future.filter(_ % 2 == 0)
  val aNonsense = for {
    mol <- future
    fmol <- filteredFuture
  } yield mol + fmol
  val completer: Future[Int] => Unit = (f: Future[Int]) => f.onComplete {
    case Failure(_) => println("something happened")
    case Success(42) => println(":meaning of life")
  }
  completer(future)
  completer(aProcessedFuture)
  /*
  BA(10000)
  T1 -> withraw 1000
  T2 -> withdraw 2000

  T1 -> this.amount = this.amount - ... //PREEMPTED by the OS
  T2 -> this.amount = this.amount - 2000 = 8000
  T1 -> -1000 = 9000
  => result = 9000
   */
  //inter-thread communications on the JVM
  //wait - notify mechanism

  import scala.concurrent.ExecutionContext.Implicits.global

  class BankAccount(private var amount: Int) {
    override def toString = "" + amount
    def withdraw(money: Int) = this.amount -= money
    def safeWithdraw(money: Int) = this.synchronized {
      this.amount -= money
    }
  }

  class VolatileBankAccount(@volatile private var amount: Int) {
    override def toString = "" + amount
    def withdraw(money: Int) = this.amount -= money
  }

}
