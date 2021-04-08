# Scala.js example fullstack program

## TLDR
```
$ npm install -g yarn
$ git clone https://github.com/oyvindberg/scalajs-fullstack.git
$ cd scalajs-fullstack
$ sbt
sbt> dev
```

Application served via `webpack-dev-server` at [http://localhost:8081](http://localhost:8081).
The server will be running at [http://localhost:8080](http://localhost:8080).

Look at **Suggestions** at the bottom

## About
This project consists of a simple file browser that uses
 [Akka Http](https://doc.akka.io/docs/akka-http/current/)
 on the backend, and
 [Scala.js](https://www.scala-js.org/), 
 [Slinky](https://slinky.dev)
 [ScalablyTyped](https://scalablytyped.org)
 and [Antd](https://ant.design/) for the frontend code.

Furthermore, we use the [«Li Haoyi stack»](https://github.com/lihaoyi) for
 type-safe Ajax calls ([Autowire](https://github.com/lihaoyi/autowire)),
 json serialization ([uPickle](https://github.com/lihaoyi/upickle))
 and testing ([uTest](https://github.com/lihaoyi/utest)).

 These are all examples of good micro-libraries that are cross-compiled for Scala.js

### Compiling Scala.js

The Scala.js compiler has two modes:

- `fastOptJS` generates unoptimized javascript.
 Since it is by far the fastest mode, we will use this for development
- `fullOptJS` also pipes the resulting javascript through the
[Google Closure compiler](https://developers.google.com/closure/compiler/)
 which does heavy DCE (Dead Code Elimination), among other optimizations.
This is slower, but output file size drops from several megabytes to hundreds of kilobytes.

Usage is just running either of those commands:
```
sbt> fastOptJS
sbt> fullOptJS
```

### Rapid development
Since the project both has client and server code, we can reload one or both
on code changes.

For the backend we use [sbt-revolver](https://github.com/spray/sbt-revolver).
Usage is like this:
```
# start server
sbt> tutorialJVM/reStart

# restart server
sbt> tutorialJVM/reStart

# stop server
sbt> tutorialJVM/reStop

# status
sbt> reStatus

# continuously restart server on changed code
sbt> ~tutorialJVM/reStart

# alias
sbt> devBack
```

Frontend: 
```
# continuously compile and bundle code 
sbt> ~tutorialJS/fastOptJS::webpack

# alias
sbt> devFront
```

If you make changes both on client and server side, this snippet should do it:
```
# alias
sbt> dev

```
Note that there is no synchronization between the two restarts, so
 it's possible that the client will reload just as the server is restarting.
In that case, simply reload the browser, or use `devFront` or `devBack`


## Testing
Test code is transpiled and then executed on Node.js, which you need to install 
on your system if you want to run tests.

To run the frontend tests do this:
```
sbt> tutorialJS/test
```

## Production
You can build a fatjar which is executable and will serve frontend contents as well:
```
sbt>tutorialJVM/assembly
# [info] Packaging .../jvm/target/scala-2.13/tutorial-assembly-0.1.0-SNAPSHOT.jar ...

shell> java -jar .../jvm/target/scala-2.13/tutorial-assembly-0.1.0-SNAPSHOT.jar 
```

## Ideas

This repo was originally created for a workshop, and the idea was that people can play around.
These are some suggestions for things that could be fun to play with:

- Try to break it!
 The compiler generally has your back, and a *lot*
 of the pain points from traditional web development are gone,
 though some remain. By refactoring the application you
 can get a feeling for what is still brittle

- Extend the application to show metadata.
 Last changed? file size? Right now it's pretty bare bones

- Add support for showing content of files.
 Such basic functionality missing!. Can you make it happen?

- Add support for several file browsers in tabs on the same page.
 Bootstrap has [tabs](http://getbootstrap.com/components/#nav),
 and the file browser just needs a DOM element to render to)

- Add support for remembering state.
 The Local Storage API is defined in `dom.localStorage`.
 You probably want to use uPickle for serialization

- Breadcrumbs for the parent folders instead of the back button.

## Resources

- [Scala.js Gitter room](https://gitter.im/scala-js/scala-js)
Probably the best place for support

### Talks
- Li Haoyi - «Scala.js - Safety & Sanity in the wild west of the web»
[Video](https://vimeo.com/124702603)
[Slides](http://www.lihaoyi.com/post/slides/PhillyETE-Scala.js.pdf)

- Otto Chrons - «Scala.js: Next generation front end development in Scala»
[Video](https://www.youtube.com/watch?v=n1GgVWOThhY)
[Slides](http://www.slideshare.net/OttoChrons/scalajs-next-generation-front-end-development-in-scala)

### Further reading

- [www.scala-js.org/](http://www.scala-js.org/)
Tutorial, community, list of libraries, etc

- [Basic tutorial](http://www.scala-js.org/tutorial/basic/)
Tutorial on which this workshop was partly based

- [Hands-on Scala.js](www.lihaoyi.com/hands-on-scala-js/)
Comprehensive introductory material

- [Scala.js SPA-tutorial](https://github.com/ochrons/scalajs-spa-tutorial)
A more comprehensive starter project that includes
[Scalajs-React](https://github.com/japgolly/scalajs-react)
and [Diode](https://github.com/ochrons/diode)
support, as well how to package a project for production.

- [TodoMVC example](http://todomvc.com/examples/scalajs-react/)
Frontend-only todo application with Scalajs-React

- [Scala.js semantics compared to Scala](http://www.scala-js.org/doc/semantics.html)
