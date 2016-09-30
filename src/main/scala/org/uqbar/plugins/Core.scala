package org.uqbar.plugins

import scala.reflect.runtime.universe._
import scala.reflect.runtime.universe

object uqbarPlugins {

  val mirror = universe.runtimeMirror(getClass.getClassLoader)

  trait PluggableApplication extends ConsumerProvider {

    var plugins: List[Plugin] = Nil

    def producers = plugins flatMap (_.producers)

    def loadPlugin(plugin: Plugin) {
      plugins :+= plugin
      for {
        producer <- plugin.producers
        consumer <- consumers if (consumer needsType (producer.getType)) && (consumer canBeFulfilledBy producers)
      } { consumer consume (producer.product) }
    }
  }

  trait Plugin extends ConsumerProvider with ProducerProvider

  trait ProducerProvider {
    def producers = getClass.publicMethods.collect { case IsProducer(method) => Producer(this, method) }
  }

  trait ConsumerProvider {
    def consumers = getClass.publicMethods.collect { case IsConsumer(method) => Consumer(this, method) }
  }

  object IsProducer {
    def unapply(method: MethodSymbol) = Option(method) filterNot (_.returnType <:< typeOf[Unit])
  }

  object IsConsumer {
    def unapply(method: MethodSymbol) = Option(method) filterNot (_.paramLists.flatten.isEmpty)
  }

  trait ResourceHandler {
    val method: MethodSymbol
    val methodOwner: Any
    def reflectedMethod = mirror.reflect(methodOwner).reflectMethod(method)
  }

  case class Producer(methodOwner: Any, method: MethodSymbol) extends ResourceHandler {
    def product = reflectedMethod()
    def getType = method.returnType
  }

  case class Consumer(methodOwner: Any, method: MethodSymbol) extends ResourceHandler {
    def consume(parameter: Any) = reflectedMethod(parameter)
    def getTypes = method.paramLists.flatten.map(_.info)
    def needsType(aType: Type) = getTypes contains aType
    def canBeFulfilledBy(producers: Iterable[Producer]) = getTypes forall (tpe => producers exists (tpe <:< _.getType))
  }

  implicit class ReflectiveClass(aClass: Class[_]) {
    def classSymbol = mirror classSymbol aClass
    def publicMethods = classSymbol.toType.decls.filter(_.isMethod).filter(_.isPublic).map(_.asMethod)
  }
}