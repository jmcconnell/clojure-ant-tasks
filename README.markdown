# Clojure Ant Tasks

by J. McConnell

## Overview

This package provides Ant tasks to aid in the building and testing of
Clojure projects. Up 'til now, most Clojure projects are built by manually
calling out to the clojure.lang.Compile class using the <java> Ant task.
Similarly, they are normally tested by using the <java> Ant task to
load the namespaces to be tested and running a (run-all-tests). Besides
being tedious and copy-and-paste prone, these both have the problem that,
unless extra care is taken, compilation and test failures still result in
a "successful" build, in Ant's eyes.

The tasks in this package aim to DRY out the building and testing of Clojure
code in Ant while at the same time improve the status reporting of failed
builds and tests.

## Use

### Compilation

Define the task with ("clojure-compile" could be whatever name you like):

    <taskdef name="clojure-compile"
      classname="com.ubermensch.ant.clojure.CompileTask"
      classpath="lib/clojure-ant-tasks.jar:lib/clojure.jar:lib/clojure-contrib.jar" />

Then you can use your new task to compile Clojure code with:

    <clojure-compile>
      <classpath>
        <pathelement location="${src.dir}" />
        <pathelement location="${classes.dir}" />
        <path refid="references.paths.are.also.supported" />
      </classpath>
      <namespace>com.foo.first.namespace.to.compile</namespace>
      <namespace>com.foo.second.namespace.to.compile</namespace>
      <fileset dir="other/clojure/files" includes="**/*.clj" />
    </clojure-compile>

### Testing

Define the task with ("clojure-test" could be whatever name you like):

    <taskdef name="clojure-test"
      classname="com.ubermensch.ant.clojure.TestTask"
      classpath="lib/clojure-ant-tasks.jar:lib/clojure.jar:lib/clojure-contrib.jar" />

Then you can use your new task to test Clojure code with:

    <clojure-test>
      <classpath>
        <pathelement location="${classes.dir}" />
      </classpath>
      <namespace>com.foo.first.namespace.to.test</namespace>
      <namespace>com.foo.second.namespace.to.test</namespace>
    </clojure-test>

## License

Copyright (c) J. McConnell. All rights reserved.

The use and distribution terms for this software are covered by the
Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
which can be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by the
terms of this license.  You must not remove this notice, or any other, from
this software.
