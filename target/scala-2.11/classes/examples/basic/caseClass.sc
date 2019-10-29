
// case classes originally intended for a specific purpose

case class caseClass(name:String, id:Integer = 0) {
  def makeMessage = "Hi i'm " +name+ " with id " + id
}

// with case classes, there's no need for the new keyword
val z = caseClass("cat", 3)
z.makeMessage