package json

import json.JsonConverter.JsonOps

object JsonSerialization extends App {
  type Date = java.util.Date

  case class User(name: String, age: Int, email: String)

  case class Post(content: String, createdAt: Date)

  case class Feed(user: User, posts: List[Post])

  /*
  1 - create intermediate data types for Int, String, List, Date
  2 - typeclasses for conversion to intermediate data type
   */
  val data = JsonObject(Map(
    "user" -> JsonString("Itsmee"),
    "posts" -> JsonArray(List(JsonString("Woof-Woof"), JsonNumber(52)))
  ))
  println(data.stringify)
  val now = new Date(System.currentTimeMillis())
  val john = User("john", 36, "me@ogt.com")
  val feed = Feed(john, List(Post("hello", now), Post("look here!", now)))
  println(feed.toJson.stringify)
}
