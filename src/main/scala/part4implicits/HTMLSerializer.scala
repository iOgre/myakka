package part4implicits

trait HTMLSerializer[T] {
  def serialize(value: T): String
}

object HTMLSerializer {
  def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  def apply[T](implicit serializer: HTMLSerializer[T]): HTMLSerializer[T] = serializer
}