package org.uqbar.plugins

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import scala.language.reflectiveCalls

class CoreTest extends FreeSpec with Matchers with BeforeAndAfterAll {
  "PluggableAppication" - {
    "Load a plugin" in {
      case class Coffee(name: String) extends Resource
      object blackCoffeeProvider extends Plugin {
        def blackCoffee: Coffee = Coffee("Black Coffee")
      }
      
      object app extends PluggableApplication {
        var coffee: Coffee = Coffee("Coffee")
        
        def makeCoffee(coffee: Coffee) {
          this.coffee = coffee
        }
      }
      
      app.coffee should be (Coffee("Coffee"))
      
      app loadPlugin blackCoffeeProvider
      //TODO app.makeCoffee should be call one time
      app.coffee should be (Coffee("Black Coffee"))
    }
  }
}