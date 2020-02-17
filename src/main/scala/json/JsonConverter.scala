package json

import json.JsonSerialization.{Feed, Post, User}
//type class
/*
1. type class
2. type class instances (implicits)
3. pimp library to use type class instances
 */
//1.
trait JsonConverter[T] {
  def convert(value: T): JsonValue
}

object JsonConverter {

  //2.
  implicit object StringConverter extends JsonConverter[String] {
    override def convert(value: String): JsonValue = JsonString(value)
  }

  implicit object NumberConverter extends JsonConverter[Int] {
    override def convert(value: Int): JsonValue = JsonNumber(value)
  }

  implicit object UserConverter extends JsonConverter[User] {
    override def convert(user: User): JsonValue = JsonObject(Map("name" -> JsonString(user.name),
      "age" -> JsonNumber(user.age), "email" -> JsonString(user.email)))
  }

  implicit object PostConverter extends JsonConverter[Post] {
    override def convert(post: Post): JsonValue = JsonObject(Map("content" -> JsonString(post.content),
      "created" -> JsonString(post.createdAt.toString)))
  }

  implicit object FeedConverter extends JsonConverter[Feed] {
    override def convert(feed: Feed): JsonValue = JsonObject(Map(
      "user" -> feed.user.toJson,
      "posts" -> JsonArray(feed.posts.map(_.toJson))
    ))
  }

  implicit class JsonOps[T](value: T) {
    def toJson(implicit converter: JsonConverter[T]): JsonValue = converter.convert(value)
  }

}



