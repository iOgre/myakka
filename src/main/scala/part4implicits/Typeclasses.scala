package part4implicits

import java.util.Date

object Typeclasses extends App {

  trait HtmlWriteble {
    def toHtml: String
  }

  case class User(name: String, age: Int, email: String) extends HtmlWriteble {
    override def toHtml: String = s"<div>$name ($age yo) <a href=$email/></div>"
  }

  val john = User("John", 32, "some@email")
  println(john.toHtml)

  /*
  1 - for types we write
  2 - one implementation out of quite a number
   */
  // Option 2 - matter patching ))
  object serializeToHtmlPM {
    def serializeToHtmlPM(value: Any) = value match {
      case User(n, a, e) =>
      //case Date =>
      case Int =>
      case _ =>
    }
  }

  /*
  1. lost type safety
  2. need to modify code every time
  3. one implementation
  */
  implicit object UserSerializer extends HTMLSerializer[User] {
    override def serialize(user: User): String = s"<div>${user.name} (${user.age} yo) <a href=${user.email}/></div>"
  }

  // 1 - we can define serializers for other types
  object DateSerializer extends HTMLSerializer[Date] {
    override def serialize(value: Date): String = s"<div>${value.toString}</div>"
  }

  // we can define multiple serializers
  object PartialUserSerializer extends HTMLSerializer[User] {
    override def serialize(user: User): String = s"<div>${user.name}</div>"
  }

  // HTMLSerializer = Type class
  val joe = User("Joe", 32, "boo@email")
  println(PartialUserSerializer.serialize(joe))

  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<div color:bred>$value</div>"
  }

  implicit class HtmlWrapper[T](item: T) {
    def toHTML(implicit serializer: HTMLSerializer[T]): String = HTMLSerializer.serialize(item)
  }

  println(HTMLSerializer.serialize(42))
  println(HTMLSerializer.serialize(john)(PartialUserSerializer))
  println(2.toHTML)
  //access to the entire typeclass interface
  println(HTMLSerializer[User].serialize(joe))
  //context bounds
  def htmlBoilerplate[T](content: T)(implicit serializer: HTMLSerializer[T]): String =
    s"<html><body>${content.toHTML(serializer)}</body></html>"
  //context bound here!! no (serializer) parameter needed
  //but we can't use serializer by name
  def htmlSugar[T: HTMLSerializer](content: T): String = s"<html><body>${content.toHTML}</body></html>"

  //implicitly
  case class Permissions(mask: String)

  implicit val defaultPermissions: Permissions = Permissions("0744")
  def myImp[T](implicit default: T): T = default
  //in other part of the code
  val standartPerms: Permissions = implicitly[Permissions]
  println(standartPerms)
  println(myImp[Permissions])
}
