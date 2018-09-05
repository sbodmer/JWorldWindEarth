# JWorldWindEarth
Java visual interface for the Nasa WorldWind SDK.

The main purpose of this project is to have a "reference" application using the
Nasa WorldWind SDK to expose all the layers available.

Each contributor could add a specific layer to the project (or add a reference
to the layer code) to increase the number of exposed layers.

A Java Web Start app is available to start the application without installation
[WorldWindEarthJWS](http://tools.knop-tech.com)

![New York](https://user-images.githubusercontent.com/18146968/29412234-243faed8-8358-11e7-9e26-3675b769c608.jpg)

[Other screen shots](https://github.com/sbodmer/JWorldWindEarth/issues/1)

## Architecture
The project is base on a Java Swing framework called [TinyRCP](https://github.com/lsimedia/TinyRCP).

The projet is a Netbeans project.

The system is completely modular and plugin based. The main idea is to wrap 
WorldWind layers in a framework class which will expose the layer functionalities
without interfering with the layer implementation. So the layer implementation
keeps to be independent of the WorldWindEarth integration.

Each world wind layer is stored in a jar file with a specific manifest entry
    
    Tiny-Factory: {full qualified class name of the factory}


The layers are recursively loaded from file system in the default folders

    {cwd}/lib/ext

## Release
Work in progress, but enough for a Preview...

## Building
The project is a Netbeans project, for manual compiling, use the ant build.xml

    cd {cwd}
    ant jar

To run the application cd in the newly created dist dir

    cd dist
    java -jar WWEarth.jar

To use only the World Wind Layer in other project, the folder dist contains the
standalone WW layers libraries.


## Features
TODO

# Adding new layer
## Files
To add a new WorldWindEarth layer you have to create a .jar which contains the
classes and resources for your layer.

The manifest of the jar must be

    Tiny-Factory: {full qualified class name of the factory}

The jar itself should be placed in the WorldWindEarth lib folder

    {wwe}/lib/layers
    {www}/lib/...

## Classes
Your layer must be composed of

- A factory instance class
- A plugin instance class produced by the factory
- A WorldWind layer instance class returned by the plugin instance

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
    
The returned layer must be a  WorldWind layer instance. A reference to the plugin
which produced it will be added by the framework, but if the layer is changed
outside of it's control, you have to set the reference with the setValue() call

    mylayer.setValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN, myplugin);

The main JDesktopPane on which the world wind window is used is passed as the
argument in the setup method.

    public void setup(Object argument);
