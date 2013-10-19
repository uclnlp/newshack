import org.riedelcastro.frontlets.Frontlet

/**
 * User: rockt
 * Date: 10/19/13
 * Time: 2:45 PM
 */

object FrontletTest extends App {
  class BasicFrontlet extends Frontlet {
    val number = IntSlot("number")
    override def toString() = number().toString
  } 
  
  class CompositeFrontlet extends Frontlet {
    val basics = FrontletListSlot("basics", () => new BasicFrontlet)
    override def toString() = if (basics().isEmpty) "Nil" else basics().mkString(", ")
  }

  class SuperFrontlet extends Frontlet {
    val composites = FrontletListSlot("composites", () => new CompositeFrontlet)
    override def toString() = composites().mkString(" | ")
  }
  
  val b1 = new BasicFrontlet; b1.number := 5
  val b2 = new BasicFrontlet; b2.number := 4
  val b3 = new BasicFrontlet; b3.number := 7
  val comp1 = new CompositeFrontlet; comp1.basics := List(b1,b2,b3)
  val b4 = new BasicFrontlet; b4.number := 2
  val b5 = new BasicFrontlet; b5.number := 5
  val comp2 = new CompositeFrontlet; comp2.basics := List(b4,b5)
  val frontception = new SuperFrontlet; frontception.composites := List(comp1,comp2)

  println(s"comp1: $comp1") //fine
  comp1.basics := comp1.basics().map(b => b.number := b.number() + 1)
  println(s"comp1 after map: $comp1") //fine
  comp1.basics := comp1.basics().filter(_.number() > 5)
  println(s"comp1 after filter: $comp1") //fine
  comp1.basics().foreach(c => c.number := 5)
  println(s"comp1 after foreach: $comp1") //not working

  println(s"comp2: $comp2") //fine
  println(s"frontception: $frontception") //fine

  //tedious
  frontception.composites := frontception.composites().map(composite => {
    composite.basics := composite.basics().map(b => b.number := b.number() + 1)
  })
  println(s"frontception after map: $frontception") //fine

  frontception.composites := frontception.composites().map(composite => {
    composite.basics := composite.basics().filter(_.number() > 6)
  })
  println(s"frontception after filter: $frontception") //fine

  frontception.composites := frontception.composites().filterNot(_.basics().isEmpty)
  println(s"frontception after non-empty filter: $frontception") //fine
}