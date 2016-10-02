package org.uqbar.plugins
import org.scalatest._
import matchers._
import scala.reflect.runtime.universe._
import org.uqbar.plugins.uqbarPlugins._

trait CoreTestMatchers {

  class comeOnlyFromTheseMethods(obj: Any)(expectedMethodNames: String*) extends Matcher[Iterable[ResourceHandler]] {

    def apply(consumers: Iterable[ResourceHandler]) = {
      val methods = for (expectedMethodName <- expectedMethodNames)
        yield runtimeMirror(getClass.getClassLoader).reflect(obj).symbol.toType.member(TermName(expectedMethodName)).asMethod
        
      MatchResult(consumers.map(_.method).equals(methods), "Consumers do not correspond to the methods", "Consumers had come only from the methods")
    }
  }
  def comeOnlyFromTheseMethods(consumerProvider: Any)(expectedMethodNames: String*) =
    new comeOnlyFromTheseMethods(consumerProvider)(expectedMethodNames:_*)
}

object CoreTestMatchers extends CoreTestMatchers