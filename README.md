# WorldWindEarth
Java visual interface for the Nasa WorldWind SDK.

The main purpose of this project is to have a "reference" application using the
Nasa WorldWind SDK to expose all the layers available.

Each contributor could add a specific layer to the project (or add a reference
to the layer code) to increase the number of exposed layers.

## Architecture
The project is base on a Java Swing framework called [TinyRCP](https://github.com/lsimedia/TinyRCP)

The system is complelty modular and plugin based

Each world wind layer is stored in a jar file with a specific manifest entry
    
    Tiny-Factory: {full qualified class name of the factory


The layers are recursively loaded from file system in the default folders

    {cwd}/lib/ext

## Release
Work in progress, alpha quality for the moment...

## Building
The project is a Netbeans project, for manual compiling, use the ant build.xml

    cd {cwd}
    ant jar

To run the application cd in the newly created dist dir

    cd dist
    java -jar WWEarth.jar

## Features
TODO

