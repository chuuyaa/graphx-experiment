
// shorthand = concise and easier to understand
class shorthandClass(name:String, id:Integer=0) {
  def makeMessage = "Hi, I'm " + name + " with id " +id
}

val y1 = new shorthandClass("cat", 3)
y1.makeMessage
val y2 = new shorthandClass("baby")
y2.makeMessage