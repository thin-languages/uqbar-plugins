package org.uqbar.plugins

import scala.reflect.runtime.universe._

trait Resource

trait PluggableApplication {
  val mirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
  
  def loadPlugin(plugin: Plugin): Unit =
    for{producer <- plugin.producers} yield consumers.foreach(consumer => consumer(producer(plugin), this))
  
  val consumers = ConsumerFinder findIn getClass
}

trait Plugin {
  val producers = ProducerFinder findIn getClass
  
  val consumers = ConsumerFinder findIn getClass
}

trait ResourceHandler

class Producer[T <: Resource](method: MethodSymbol) extends ResourceHandler {
  val mirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
  
  def apply(caller: Any) = mirror.reflect(caller).reflectMethod(method).apply().asInstanceOf[T]
}

class Consumer[T <: Resource](method: MethodSymbol) extends ResourceHandler {
  val mirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
  
  def apply(parameter: T, caller: Any) = mirror.reflect(caller).reflectMethod(method).apply(parameter)
}

object ConsumerFinder {
  def mirrorFor(aClass: Class[_]) = scala.reflect.runtime.universe.runtimeMirror(aClass.getClassLoader)
  
  def findIn(aClass: Class[_]) =
    for{member <- mirrorFor(aClass).classSymbol(aClass).toType.members if member.isMethod
        method = member.asMethod if method.paramLists.exists(_.exists(_.info <:< typeOf[Resource]))}
  yield new Consumer(method)
        
}

object ProducerFinder {
  def mirrorFor(aClass: Class[_]) = scala.reflect.runtime.universe.runtimeMirror(aClass.getClassLoader)
  
  def findIn(aClass: Class[_]) =
    for{member <- mirrorFor(aClass).classSymbol(aClass).toType.members if member.isMethod
        method = member.asMethod if method.returnType <:< typeOf[Resource]}
  yield new Producer(method)
        
}