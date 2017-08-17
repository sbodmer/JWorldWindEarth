# WorldWindEarth
Java visual interface for the Nasa WorldWind SDK.

The main purpose of this project is to have a "reference" application using the
Nasa WorldWind SDK to expose all the layers available.

Each contributor could add a specific layer to the project (or add a reference
to the layer code) to increase the number of exposed layers.

A Java Web Start app is available to start the application without installation
[WorldWindEarthJWS](http://tools.knop-tech.com)

![New York](https://user-images.githubusercontent.com/18146968/29412234-243faed8-8358-11e7-9e26-3675b769c608.jpg =320x)

## Architecture
The project is base on a Java Swing framework called [TinyRCP](https://github.com/lsimedia/TinyRCP)

The system is complelty modular and plugin based

Each world wind layer is stored in a jar file with a specific manifest entry
    
    Tiny-Factory: {full qualified class name of the factory}


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

# Adding new layer
## Files
To add a new WorldWind layer you have to create a .jar which contains the the
classes and resources for your layer.

The manifest of the jar must be

    Tiny-Factory: {full qualified class name of the factory}

The jar itself should be placed in the WorldWindEarth lib folder

    {wwe}/lib/layers

## Classes
Your layer must be composed of

- A factory instance
- A plugin instance produced by the factory
- A WorldWind layer returned by the plugin instance

### Plugin factory
Your factory class must implement the tiny rcp framework factory

    org.tinyrcp.PluginFactory

See the available code for example, the important method to implement
is

    public TinyPlugin newPlugin(Object argument);

The passed argument will be an instance of a world wind window

    gov.nas.worldwind.WorldWindow

### Plugin
The plugin instance returned by the factory will be used for handling by
the WorldWindEarth and TinyTCP frameworks.

The important method to implement is

    public Layer getLayer();
    
The returned layer must be a  WorldWind layer instance and a reference to the plugin
which produced it must be passed as a layer value so the frame work knows the
origin of the layer

    mylayer.setValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN, myplugin)

The main JDesktopPane is passed as the argument in the method

    public void setup(Object argument);
