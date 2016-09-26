#﻿ Uqbar-Plugins

A Scala based framework to create plugin-based easy to extend applications.

## Setup

To include this module in your *SBT* project, just add the following line to your `.sbt` project definition:

```scala
libraryDependencies += "org.uqbar" %% "uqbar-plugins" % "latest.integration"
```

## Usage

This framework provides an interface to turn any Scala program into an application that can be extended by defining and
importing plugins. Just declare what resources you want to allow plugins to provide, set up a life-cycle and you are
ready to go.

To start configuring your application, define an object extending the `PluggableApplication` mixin. This will give it all
you need to handle resources and execute as a Scala program.

If you are looking to make a plugin  to provide resources for an existing application, just create an object and make
it extend the `Plugin` mixin instead.
  
### Resources

Think of resources as any content you expect plugins to provide to your application. Anything can be a resource, from
configuration files to class definitions. `PluggableApplication` instances will automatically check for plugins on start
and keep any handled resource up to date.

You don't need to explicitly declare resources. Instead, any type used on a *resource handler* will automatically become 
a tracked resource.

#### Resource Handlers

Any public method defined on an instance of `Plugin` or `PluggableApplication` will be considered a *resource handler*.
This methods will be used by the framework to control it's life cycle and let it know when a resource has changed.

There are two main types of resource handlers: *producers* and *consumers*.

##### Producers

Any public method with a return type other than `Unit` will be considered a producer of that type. That way, a plugin
that defines a method returning `T`, declares that it knows how to generate the resource *T* for anyone that may need it.
This method will be called by the framework any time a *T consumer* needs an instance of that resource.

Most non-primitive types may be used as produceable resources, yet some return types denote a special meaning:

	- `T`: The method can always provide a single instance of a `T` resource each time it's invoked.
	- `Option[T]`: The method may or may not be able to provide a `T`.
	- `List[T]`: The method may or may not be able to provide many `T` resources. 
	- `Future[T]`: The method can always provide an asynchronous process that, when resolved, may result in a single `T`.
	- `Future[List[T]]`: The method can always provide an asynchronous process that, when resolved, may result in many `T`.

##### Consumers

Any public method with a parameter of any type will be considered a consumer of that type. That way, a plugin
that defines a method expecting a parameter of type `T`, declares that is interested in updates of the resource `T`.
This method will be called by the framework any time `T` resources change, or a new *T provider* is detected.

A single method can consume many resource types at once by declaring multiple parameters; that way the method will
execute only when all the required resources are available and any time one of those resources change. 

Most non-primitive types may be used as consumable resources, yet some types denote a special meaning:
	- `T`: The method requires an instance of a `T` resource to perform. If more than one instance is available, the 
	method will be executed once for each instance.
	- `Option[T]`: The method uses the resource `T`, but does not need it to perform. If the method is invoked with no
	resource, it may be invoked again once an instance is available at a later time.
	- `List[T]`: The method requires all available instances of `T` at once and may perform even if there are none. The
	method will be invoked at any time a new instance of `T` is made available or removed with the whole list obtained
	from all the producers. Consumers should not assume any order for the resource list. 
	
##### Producer-Consumers

Any public method that expects parameters and returns a non-unit type is booth a producer *and* a consumer and will be
treated accordingly by the framework. 

### Declarative Definitions

Plugin definition is intended to be as declarative and isolated as possible. This allows dynamic loading and hot swap.
Producers should have no need to worry about synchronizing or establishing an order with other producers; instead,
consumers should decide what resource is best to perform.


## Roadmap

**TODO**

	- Manage exceptions.
	- Plugins auto-download and compile from remote origins (such as github)
	- Dynamic install, uninstall and hot swap for plugins.
	- Manage cyclic requirements.
	- Support implicit arguments.
	- (?) handle things asynchronously with actors.
	- (?) Life cycle control
	- (?) Support other funky special types:
		- Streams as return
		- Logging
		- Single[T], Latest[T] & Earliest[T] as parameters
		- Resource[T] and Plugin[P] as parameter for reflective queries such as `isBeingProvided` and `providedResources`


## Contributions

Yes, please! Pull requests are always welcome, just try to keep it small and clean.


## License

This code is open source software licensed under the [LGPL v3 License](https://www.gnu.org/licenses/lgpl.html) by [The Uqbar Foundation](http://www.uqbar-project.org/). Feel free to use it accordingly.