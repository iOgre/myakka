package json

sealed trait JsonValue {
  def stringify: String
}

final case class JsonString(value: String) extends JsonValue {
  override def stringify: String = s""""$value""""
}

final case class JsonNumber(value: Int) extends JsonValue {
  override def stringify: String = value.toString
}

final case class JsonArray(values: List[JsonValue]) extends JsonValue {
  override def stringify: String = values.map(_.stringify).mkString("[", ",", "]")
}

final case class JsonObject(values: Map[String, JsonValue]) extends JsonValue {
  /*
  {
    name: "John"
    age: 22
    friends: [...]
    latestPost: {
      content: "Wooof-Woof"
      date: ....
      }
    }
   */
  override def stringify: String = values.map {
    case (key, value) => s""""$key": ${value.stringify}"""
  }.mkString("{", ",", "}")
}


