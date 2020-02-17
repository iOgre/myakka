package part4implicits

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Forcomp extends App {
  for {
    _ <- Future {
      println("One")
    }
    _ <- Future {
      println("Two")
    }
    _ <- Future {
      println("Three")
    }
    _ <- Future {
      println("Four")
    }
    _ <- Future {
      println("Five")
    }
  } yield ()
  Thread.sleep(1000)
}
