# WorldWindEarth
Java visual interface for the Nasa WorldWind SDK.

The main purpose of this project is to have a "reference" application using the
Nasa WorldWind SDK to expose all the layers available.

Each contributor could add a specific layer to the project (or add a reference
to the layer code) to increase the number of exposed layers.

## Architecture
The project is base on a Java Swing framework called TinyRCP
[Github TinyRCP project](https://github.com/lsimedia/TinyRCP)

The system is complelty modular and plugin based

Each visual component is stored in a jar file with a specific manifest entry
    
    TinyRCP-Factory: {full qualified class name of the factory


The components are recursively loaded from file system in the default folders

    {cwd}/lib/ext

## Release
Work in progress, alpha quality for the moment...

## Building
The project is a Netbean project, for manual compiling, use the ant build.xml
file

    ant jar

To run the application cd in the dist dir

    cd dist
    java -jar WorldWindEarth.jar

## Features
TODO

