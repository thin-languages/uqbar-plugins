package org.uqbar.plugins

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import uqbarPlugins._
import scala.reflect.runtime.universe._
import CoreTestMatchers._

case class Coffee(name: String)
case class Ingredient(amount: Int, name: String)
case class Sandwich(ingredients: List[Ingredient])
case class Combo(coffee: Coffee, sandwich: Sandwich)

class CoffeeShopApp extends PluggableApplication {
  private var coffee: Coffee = Coffee("Coffee")

  private var combo: Option[Combo] = None

  def getCoffee = coffee

  def makeCoffee(coffee: Coffee) {
    this.coffee = coffee
  }

  def prepareCombo(coffee: Coffee, sandwich: Sandwich) {
    this.combo = Some(Combo(coffee, sandwich))
  }
}

object blackCoffeeProvider extends Plugin {
  def blackCoffee: Coffee = Coffee("Black Coffee")
}

object bigComboProvider extends Plugin {
  private val bigCoffee = Coffee("Extra large coffee")
  val bigSandwich = Sandwich(Ingredient(10, "bread") :: Ingredient(20, "jam") :: Ingredient(30, "provolone") :: Nil)
  val bigCombo = Combo(bigCoffee, bigSandwich)
}

class CoreTest extends FreeSpec with Matchers with BeforeAndAfterAll with CoreTestMatchers {
  "Inspecting the app and plugins" - {
    "Get the consumers by type of resource" in {
      val app = new CoffeeShopApp

      app.consumersOf(typeOf[Coffee]) should comeOnlyFromTheseMethods(app)("makeCoffee", "prepareCombo")

      app.consumersOf(typeOf[Sandwich]) should comeOnlyFromTheseMethods(app)("prepareCombo")
    }
    "Get the producers by type of resource" in {
      bigComboProvider.producersOf(typeOf[Coffee]) should be('empty)

      bigComboProvider.producersOf(typeOf[Sandwich]) should comeOnlyFromTheseMethods(bigComboProvider)("bigSandwich")

      bigComboProvider.producersOf(typeOf[Combo]) should comeOnlyFromTheseMethods(bigComboProvider)("bigCombo")
    }
  }
  "Loading Plugins" - {
    "Load a plugin that produces what its consumer needs" in {
      val app = new CoffeeShopApp

      printf(s"Consumers:\n\tSize:${app.consumers.size}\n\tSon estos: ${app.consumers.map(_.method.name).mkString(" , ")}\n")
      printf(s"Producers:\n\tSize:${blackCoffeeProvider.producers.size}\n\tSon estos: ${blackCoffeeProvider.producers.map(_.method.name).mkString(" , ")}\n")

      app.getCoffee should be(Coffee("Coffee"))

      app loadPlugin blackCoffeeProvider

      app.getCoffee should be(Coffee("Black Coffee"))
    }
    "Load a plugin that doesn't produce anything" in {

      val app = new CoffeeShopApp

      object uselessPlugin extends Plugin

      app.getCoffee should be(Coffee("Coffee"))

      app loadPlugin uselessPlugin

      app.getCoffee should be(Coffee("Coffee"))
    }
  }
}