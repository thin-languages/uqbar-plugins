package org.uqbar.plugins

import scala.reflect.runtime.universe._

object uqbarPlugins {

  val mirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
  
  trait Resource
  
  trait PluggableApplication extends ConsumerProvider {
    def loadPlugin(plugin: Plugin): Unit =
      for{producer <- plugin.producers
          consumer <- consumers} { consumer consume producer.product }
  }
  
  trait Plugin extends ConsumerProvider with ProducerProvider 
  
  trait ConsumerProvider {
    val consumers = for{method <- getClass.methods if method.paramLists.exists(_.exists(_.info <:< typeOf[Resource]))}
    yield Consumer(this, method) 
  }
  
  trait ProducerProvider {
    val producers = for{method <- getClass.methods if method.returnType <:< typeOf[Resource]}
    yield Producer(this, method)
  }
  
  trait ResourceHandler {
    val method: MethodSymbol
    val methodOwner: Any
    def reflectedMethod = mirror.reflect(methodOwner).reflectMethod(method)
  }
  
  case class Producer[T <: Resource](methodOwner: Any, method: MethodSymbol) extends ResourceHandler {    
    def product = reflectedMethod.apply().asInstanceOf[T]
  }
  
  case class Consumer[T <: Resource](methodOwner: Any, method: MethodSymbol) extends ResourceHandler {
    def consume(parameter: T) = reflectedMethod.apply(parameter)
  }
  
  implicit class ReflectiveClass(aClass: Class[_]) {
    def classSymbol = mirror.classSymbol(aClass)
    def methods = classSymbol.toType.members.filter(_.isMethod).map(_.asMethod)
  }
}