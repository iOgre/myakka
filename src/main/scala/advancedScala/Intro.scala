package advancedScala

import java.util.concurrent.Executors
import scala.runtime.AbstractFunction0

object Intro extends App {
  private def One: Unit = {
    /*
  interface Runnable {
  public void run()
  */
    //JVM thread
    val aThread = new Thread(new Runnable {
      override def run() = println(s"running in parallel")
    })
    aThread.start() //gives a signal to jvm to start jvm thread
    //Create JVM thread on top of OS thread
    val runnable = new Runnable {
      override def run(): Unit = println("this is runnable")
    }
    runnable.run() //No parallel execution here!!
    aThread.join() //blocks until aThread finishes running
  }
  private def differentRunsDifferentResults: Unit = {
    val threadHello = new Thread(() => (1 to 5).foreach(g => println(s"Hello $g")))
    val threadBye = new Thread(() => (1 to 5).foreach(g => println(s"Bye $g")))
    threadHello.start()
    threadBye.start()
  }
  private def executors(): Unit = {
    val pool = Executors.newFixedThreadPool(10)
    pool.execute(() => println(s"Something in the thread pool"))
    pool.execute(() => {
      Thread.sleep(5000)
      println("done after 5 seconds")
    })
    pool.execute(() => {
      Thread.sleep(5000)
      println("almost done")
      Thread.sleep(5000)
      println("done after 10 seconds")
    })
    //
    //    pool.shutdown()
    //    pool.execute(() => println("Should not appear")) //throws an exception in the calling thread
    //
    pool.shutdownNow()
    println(pool.isShutdown)
    //Shutdown means that pool WILL NOT RECEIVE ANY MORE ACTIONS
  }
  private def concurrencyProblems: Unit = {
    def runInParallel: Unit = {
      var x = 0
      val thread1 = new Thread(() => {
        x = 1
      })
      val thread2 = new Thread(() => {
        x = 2
      })
      thread1.start()
      thread2.start()
      println(x)
    }
    for (_ <- 1 to 1000) runInParallel //mostly prints 0 and rarely 1 or 2
  }
  private def bankAccountsEtc: Unit = {
    class BankAccount(var amount: Int) {
      override def toString = "" + amount
    }

    def buy(account: BankAccount, thing: String, price: Int): Unit = {
      account.amount -= price
    }
    def buySafe(account: BankAccount, thing: String, price: Int): Unit =
    //no threads can evaluate this at the same time
      account.synchronized {
        account.amount -= price
        println(s"I've bought $thing")
        println(s"My account now is $account")
      }
    for (_ <- 1 to 10000) {
      val account = new BankAccount(50000)
      val thread1 = new Thread(() => buy(account, "shoes", 3000))
      val thread2 = new Thread(() => buy(account, "iphone12", 4000))
      thread1.start()
      thread2.start()
      thread1.join()
      thread2.join()
      //  Thread.sleep(2)
      if (account.amount != 43000) println(s"aha! ${account.amount}")
      println()
    }
  }
  bankAccountsEtc
}

object Problems extends App {

  class BankAccount(var amount: Int) {
    override def toString = "" + amount
  }

  class VolatileBankAccount(@volatile var amount: Int) {
    override def toString = "" + amount
  }

  def buy(account: BankAccount, thing: String, price: Int): Unit = {
    account.amount -= price
  }
  def buySafe(account: BankAccount, thing: String, price: Int): Unit =
  //no threads can evaluate this at the same time
    account.synchronized {
      account.amount -= price
      println(s"I've bought $thing")
      println(s"My account now is $account")
    }
  for (_ <- 1 to 10000) {
    val account = new BankAccount(50000)
    val thread1 = new Thread(() => buySafe(account, "shoes", 3000))
    val thread2 = new Thread(() => buySafe(account, "iphone12", 4000))
    thread1.start()
    thread2.start()
    thread1.join()
    //thread2.join()
    //  Thread.sleep(2)
    if (account.amount != 43000) println(s"aha! ${account.amount}")
    println()
  }
}

object A extends App {
  def inception(max: Int, acc: Thread = null): Thread = new Thread(() => {
    if (max > 0) {
      println(s"Hello from $max")
      val incThread = inception(max - 1)
      incThread.start()
    }
  })
  val inceptionThread = inception(700, new Thread(() => "Hello from Z"))
  inceptionThread.start()
}

object Ex0 extends App {
  def inception(max: Int): Thread = new Thread(() => {
    if (max > 0) {
      println(s"Hello from $max")
      val incThread = inception(max - 1)
      incThread.start()
      incThread
    }
  })
  val inceptionThread = inception(700)
  inceptionThread.start()
}

object Excercise1 extends App {
  /*
  1. construct 50 inception threads
    thread1->thread2->thread3... ->thread 50
    println("hello from thread #)
    in reverse order
         */
  def inceptionThreads(maxThreads: Int, i: Int = 1): Thread = new Thread(() => {
    if (i < maxThreads) {
      val newThread = inceptionThreads(maxThreads, i + 1)
      newThread.start()
      newThread.join()
    }
    println(s"Hello from thread #$i")
  })
  inceptionThreads(50).start()
}

/*
1. biggest value possible for x? 100??
2. smallest value possible for x 1???
 */
object Excercise2 extends App {
  var x = 0
  val threads: Seq[Thread] = (1 to 100).map(_ => new Thread(() => x = x + 1))
  threads.foreach(_.start())
  println(x)
}

//sleep fallacy
/*
what is value for message? "Scala is awesome" Mostly!
is it guaranteed? NO!
why or why not?
  (main thread)
    message = "Scala sucks"
    awesomeThread.start()
    sleep() - relieves execution of main thread
   (awesomeThread)
      sleep - relieves execution of awesome thread
      (OS gives the cpu to more important thread - takes more than 2 seconds)
      (OS gives cpu back to MAIN thread)
        println "scala sucks"
      (OS gives cpu to awesome thread)
        message = "scala is awesome"

 */
object Excercise3 extends App {
  val priority: Thread = new Thread(() => {
    println(s"start - ${priority.getPriority}")
    Thread.sleep(10000)
    println("finish")
  })
  priority.setPriority(10)
  var message = ""
  var awesomeThread = new Thread(() => {
    Thread.sleep(1000)
    println(s"awesome priority ${Thread.currentThread().getPriority}")
    message = "Scala is awesome"
  })
  awesomeThread.setPriority(1)
  priority.start()
  message = "Scala sucks"
  awesomeThread.start()
  Thread.sleep(1001)
  awesomeThread.join()
  println(message)
}

