package org.uqbar.plugins

import scala.reflect.runtime.universe._

object uqbarPlugins {

  val mirror = runtimeMirror(getClass.getClassLoader)

  class PluggableApplication extends ConsumerProvider {

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

  trait ResourceHandlerProvider {
    val isResourceHandler: MethodSymbol => Boolean = method => method.isPublic && !method.isConstructor
    
    def methods = mirror.reflect(this).symbol.toType.decls.collect { case method: MethodSymbol => method }
  }

  trait ProducerProvider extends ResourceHandlerProvider {
    def isProducer(method: MethodSymbol) = isResourceHandler(method) && returnsSomething(method)

    def returnsSomething(method: MethodSymbol) = !(method.returnType =:= typeOf[Unit])

    def producers = methods.collect { case method if isProducer(method) => Producer(this, method) }
  }

  trait ConsumerProvider extends ResourceHandlerProvider {
    def isConsumer(method: MethodSymbol) = isResourceHandler(method) && hasParameters(method)

    def hasParameters(method: MethodSymbol) = method.paramLists.headOption.fold(false)(_.nonEmpty)

    def consumers = methods.collect { case method if isConsumer(method) => Consumer(this, method) }
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
}