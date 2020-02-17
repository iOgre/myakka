package recap

import scala.collection.mutable
import scala.util.Random

class SimpleContainer {
  private var value = 0
  def isEmpty = value == 0
  def get: Int = {
    val result = value
    value = 0
    result
  }
  def set(newVal: Int) = value = newVal
}

object ThreadCommunications extends App {
  //producer -  consumer problem
  // producer -> [x] -> consumer
  def naiveProducerConsumer(): Unit = {
    val container = new SimpleContainer
    val consumer = new Thread(() => {
      println("[consumer] waiting")
      while (container.isEmpty) {
        println("[consumer] actively waiting")
      }
      println(s"[consumer] I have consumed ${container.get}")
    })
    val producer = new Thread(() => {
      Thread.sleep(500)
      val value = 42
      println(s"[producer] - I have produced, after long work, the value = $value")
      container.set(value)
    })
    consumer.start()
    producer.start()
  }
  naiveProducerConsumer()
}

object WaitNotifyCommunications extends App {
  def smartProdCons(): Unit = {
    val container = new SimpleContainer
    val consumer = new Thread(() => {
      println("[consumer] waiting")
      container.synchronized {
        container.wait()
      }
      //container must have some value
      println(s"[consumer] I have consumed ${container.get}")
    })
    val producer = new Thread(() => {
      println("[consumer] Hard at work...")
      Thread.sleep(2000)
      val value = 42
      container.synchronized {
        println(s"[producer] I'm producing $value")
        container.set(value)
        container.notify() //wakes .wait()
      }
    })
    consumer.start()
    producer.start()
  }
  smartProdCons()
}

object ProdConsWithBuffer extends App {
  /*
  producer -> [ ? ? ? ] -> consumer
  [? ? ?] - buffer, has limited size
   */
  def prodConsLargeBuffer(): Unit = {
    val buffer = new mutable.Queue[Int]
    val capacity = 3
    val consumer = new Thread(() => {
      val random = new Random()
      while (true) {
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] Buffer empty, waiting")
            buffer.wait()
          }
          //there must be at least ONE value is buffer
          val x = buffer.dequeue()
          println(s"[consumer] consumed $x")
          buffer.notify()
        }
        Thread.sleep(random.nextInt(500))
      }
    })
    val producer = new Thread(() => {
      val random = new Random()
      var i = 0
      while (true) {
        buffer.synchronized {
          if (buffer.size == capacity) {
            println("[producer] buffer is full, waiting")
            buffer.wait()
          }
          //there must be at least ONE EMPTY SPACE in the buffer
          println("[producer]")
          buffer.enqueue(i)
          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(500))
      }
    })
    producer.start()
    consumer.start()
  }
  prodConsLargeBuffer()
}

object MultipleProdConsWithBuffer extends App {

  /*
    producer1 -> [? ? ?] -> consumer 1
    producer2 -----^  ^----- consumer 2
   */
  case class ProdConPair(producer: Thread, consumer: Thread)

  def generateProdCons(number: Int): Seq[ProdConPair] = {
    (1 to number).map { _ =>
      val buffer = new mutable.Queue[Int]
      val capacity = 3
      val consumer = new Thread(() => {
        val random = new Random()
        while (true) {
          buffer.synchronized {
            if (buffer.isEmpty) {
              println(s"[consumer][${Thread.currentThread().getId}] Buffer empty, waiting")
              buffer.wait()
            }
            //there must be at least ONE value is buffer
            val x: Int = buffer.dequeue()
            println(s"[consumer][${Thread.currentThread().getId}] consumed $x")
            buffer.notify()
          }
          Thread.sleep(random.nextInt(5000))
        }
      })
      val producer = new Thread(() => {
        val random = new Random()
        var i = 0
        while (true) {
          buffer.synchronized {
            if (buffer.size == capacity) {
              println(s"[producer][${Thread.currentThread().getId}] buffer is full, waiting")
              buffer.wait()
            }
            //there must be at least ONE EMPTY SPACE in the buffer
            println(s"[producer][${Thread.currentThread().getId}] puts $i to buffer")
            buffer.enqueue(i)
            buffer.notify()
            i += 1
          }
          Thread.sleep(random.nextInt(5000))
        }
      })
      ProdConPair(producer, consumer)
    }
  }
  generateProdCons(10).foreach { pc =>
    pc.consumer.start()
    pc.producer.start()
  }
}

object MultipleProdConsWithBuffer2 extends App {

  case class Producer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      var i = 0
      while (true) {
        buffer.synchronized {
          while (buffer.size == capacity) {
            println(s"[producer][$id] buffer is full, waiting")
            buffer.wait()
          }
          //there must be at least ONE EMPTY SPACE in the buffer
          println(s"[producer][$id] puts $i to buffer")
          buffer.enqueue(i)
          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(500))
      }
    }
  }

  case class Consumer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      while (true) {
        buffer.synchronized {
          while (buffer.isEmpty) {
            println(s"[consumer][$id] Buffer empty, waiting")
            buffer.wait()
          }
          //there must be at least ONE value is buffer
          val x = buffer.dequeue()
          println(s"[consumer][$id] consumed $x")
          buffer.notify()
        }
        Thread.sleep(random.nextInt(500))
      }
    }
  }

  def multiProdCons(nProducer: Int, nConsumer: Int): Unit = {
    val totalBuffer = new mutable.Queue[Int]
    val capacity = 4
    (1 to nProducer).foreach(i => Producer(i, totalBuffer, capacity).start())
    (1 to nConsumer).foreach(i => Consumer(i, totalBuffer).start())
  }
  multiProdCons(5, 0)
}

/*
Wait and notify
wait() - ing on an object's monitor suspends you (thread) indefinitely
notify() - signal one sleeping thread they may continue
notifyAll() - awakens all threads
wait and notify works only in synchronised expressions
*/
