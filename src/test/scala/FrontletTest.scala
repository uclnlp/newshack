import org.riedelcastro.frontlets.Frontlet

/**
 * User: rockt
 * Date: 10/19/13
 * Time: 2:45 PM
 */

object FrontletTest extends App {
  class BasicFrontlet extends Frontlet {
    val number = IntSlot("number")
  } 
  
  class CompositeFrontlet extends Frontlet {
    val basics = FrontletListSlot("basics", () => new BasicFrontlet)
  }

  val c1 = new BasicFrontlet; c1.number := 5
  val c2 = new BasicFrontlet; c2.number := 4
  val cs = new CompositeFrontlet; cs.basics := List(c1,c2)

  println(cs.basics().mkString(" ")) //fine
  cs.basics := cs.basics().map(c => c.number := c.number() + 1)
  println(cs.basics().mkString(" ")) //fine
  cs.basics := cs.basics().filter(c => c.number() > 5)
  println(cs.basics().mkString(" ")) //fine
}