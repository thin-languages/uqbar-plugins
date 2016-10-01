package org.uqbar.plugins

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import uqbarPlugins._

case class Coffee(name: String)
class MyApp extends PluggableApplication {
  private var coffee: Coffee = Coffee("Coffee")

  def getCoffee = coffee

  def makeCoffee(coffee: Coffee) {
    this.coffee = coffee
  }
}

object blackCoffeeProvider extends Plugin {
  def blackCoffee: Coffee = Coffee("Black Coffee")
}

class CoreTest extends FreeSpec with Matchers with BeforeAndAfterAll {
  "PluggableAppication with a single parameter consumer" - {
    "Load a plugin that produces what its consumer needs" in {
      val app = new MyApp

      printf(s"Consumers:\n\tSize:${app.consumers.size}\n\tSon estos: ${app.consumers.map(_.method.name).mkString(" , ")}\n")
      printf(s"Producers:\n\tSize:${blackCoffeeProvider.producers.size}\n\tSon estos: ${blackCoffeeProvider.producers.map(_.method.name).mkString(" , ")}\n")

      app.getCoffee should be(Coffee("Coffee"))

      app loadPlugin blackCoffeeProvider

      app.getCoffee should be(Coffee("Black Coffee"))
    }
    "Load a plugin that doesn't produce anything" in {

      val app = new MyApp

      object uselessPlugin extends Plugin

      app.getCoffee should be(Coffee("Coffee"))

      app loadPlugin uselessPlugin

      app.getCoffee should be(Coffee("Coffee"))
    }
  }
}