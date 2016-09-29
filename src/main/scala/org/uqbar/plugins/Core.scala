package org.uqbar.plugins

trait PluggableApplication {
  def loadPlugin(plugin: Plugin): Unit = Unit
}

trait Plugin