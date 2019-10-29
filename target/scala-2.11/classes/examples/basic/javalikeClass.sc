
class javalikeClass (initName:String, initID: Integer) {
  val name:String = initName
  var id:Integer = initID

  // is a function
  def makeMessage = {
    "Hi, I'm " + name + " with id " + id
  }
}
// an instance of javalikeClass called x
val x = new javalikeClass("cat" , 3)
//calling the makeMessage function, x.makeMessage, returns
// Hi, I'm cat with id 3
x.makeMessage