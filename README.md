# Scala.js workshop at flatMap(Oslo) 2016

[![Join the chat at https://gitter.im/oyvindberg/scalajs-workshop](https://badges.gitter.im/oyvindberg/scalajs-workshop.svg)](https://gitter.im/oyvindberg/scalajs-workshop?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

##TLDR
```
git clone https://github.com/oyvindberg/scalajs-workshop.git
cd scalajs-workshop
./sbt
~;tutorialJVM/re-start;tutorialJS/fastOptJS;tutorialJS/refreshBrowsers
```
Application served at [http://localhost:8080](http://localhost:8080)
Look at **Suggestions** at the bottom

## About
This project consists of a simple file browser that uses
 [Akka Http](http://doc.akka.io/docs/akka/2.4.4/scala/http/)
 on the backend, and
 [Scala.js](https://www.scala-js.org/)
 with [Bootstrap](https://getbootstrap.com) for the frontend code.

Furthermore, we use the [«Li Haoyi stack»](https://github.com/lihaoyi) for
 type-safe Ajax calls ([Autowire](https://github.com/lihaoyi/autowire)),
 html templating ([ScalaTags](https://github.com/lihaoyi/scalatags)),
 json serialization ([uPickle](https://github.com/lihaoyi/upickle-pprint))
 and testing ([uTest](https://github.com/lihaoyi/utest)).

 These are all examples of good micro-libraries that are cross-compiled for Scala.js

We also use typed wrappers for javascript APIs, notably
[Scala.js DOM](http://scala-js.github.io/scala-js-dom/)


## Development

We bundle the [sbt-extras](https://github.com/paulp/sbt-extras)
launcher for [sbt](https://github.com/sbt/sbt/) in the project,
so you can start it by simply running:

```
bash> ./sbt
```

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
sbt> tutorialJS/fastOptJS
sbt> tutorialJS/fullOptJS
```

#### Resulting files
```
bash> ls js/target/scala-2.11/
1.3M scala-js-workshop-fastopt.js     <-- Result of fastOptJS
436K scala-js-workshop-fastopt.js.map <-- Source map for fastOptJS
309K scala-js-workshop-jsdeps.js      <-- Javascript dependencies concatenated by sbt
278K scala-js-workshop-jsdeps.min.js  <-- Minified Javascript dependencies concatenated bt
126B scala-js-workshop-launcher.js    <-- Generated script that runs main()
277K scala-js-workshop-opt.js         <-- Result of fullOptJS
920K scala-js-workshop-opt.js.map     <-- Source map for fullOptJS
```

### Rapid development
Since the project both has client and server code, we use two sbt plugins to
automatically reload browser and restart server when necessary.

For the backend we use [sbt-revolver](https://github.com/spray/sbt-revolver).
Usage is very simple:
```
# start server
sbt> tutorialJVM/re-start

# restart server
sbt> tutorialJVM/re-start

# stop server
sbt> tutorialJVM/re-stop

# status
sbt> re-status

# continuously restart server on changed code
sbt> ~tutorialJVM/restart
```

For the frontend we use [workbench](https://github.com/oyvindberg/workbench).
This works by redirecting messages from sbt to the browser so you can see the project
(not) compile, and automatically have the browser window reloaded on successful compilation.

Usage is again very simple:
```
sbt> ~;tutorialJS/fastOptJS;tutorialJS/refreshBrowsers
```

If you need both, this snippet should do it:
```
sbt> ~;tutorialJVM/re-start;tutorialJS/fastOptJS;tutorialJS/refreshBrowsers
```
Note that there is no synchronization between the two restarts, so
 it's possible that the client will reload just as the server is restarting.
In that case, simply reload the browser.

## Suggestions

The workshop is very much open ended,
these are some suggestions for things that could be fun to play with:

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

## Resources

[Gitter room](https://gitter.im/scala-js/scala-js)
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