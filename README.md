[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/vaadin-jpacontainer)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/vaadin-jpacontainer.svg)](https://vaadin.com/directory/component/vaadin-jpacontainer)

JPAContainer README
======================

Prerequisites
----
* Java 6
* Maven 2.2
* A working internet connection

Checking out the code
----
The source code is maintained in GitHub. Clone the repository using:

```
$ git clone https://github.com/vaadin/jpacontainer
```

Editting the code in an IDE
----
JPAContainer currently contains configuration data for NetBeans 6.8, but any other IDE that supports Maven should work just fine.

Trying out the demo
----
1. Compile and install the entire project:
```
$ mvn install
```
2. Start the built-in Jetty web server:
```
$ cd jpacontainer-demo
$ mvn jetty:run
```
3. Open your favorite web browser and point it to:
```
http://localhost:8080/jpacontainer-demo/
```

Reading the manual
----
1. Generate the manual:
```
$ cd jpacontainer-manual
$ mvn docbkx:generate-html
```
2. Open the file jpacontainer-manual/target/docbkx/html/manual.html in your favorite web browser.

Checking the code coverage of your unit tests
----
1. Run the code coverage check:
```
$ cd jpacontainer-addon
$ mvn cobertura:cobertura
```
2. Open the file jpacontainer-addon/target/site/cobertura/index.html in your favorite web browser.

Contributing
----
See https://vaadin.com/wiki/-/wiki/Main/Contributing+Code for information about how to contribute code

More instructions will be added later.
