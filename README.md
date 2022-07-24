# WhackMe
First of all special thanks to everyone who purchased our plugin and support us. <3

Whack Me is an awesome, very easy to setup, lightweight, fun lobby game that every server needs. If you have any problem with this plugin check out our [wiki](https://github.com/Despical/WhackMe/wiki). If you still didn't find a your answer see documentation section below.

## Documentation
More detailed information can be found on the [wiki](https://github.com/Despical/WhackMe/wiki).
The Java documentation cannot be browsed due to privacy.<br>For questions or isseus use [issue tracker](https://github.com/Despical/WhackMe/issues).
Also you can join our [Discord community](https://www.discord.gg/rVkaGmyszE) to get support and news early.

## Translations
We are only supporting English for now and, also working on a language system.<br>
If you want to help us with translating take a look at our [language repository](https://github.com/Despical/LocaleStorage).

## Contributing
We can not accept contributions via anyway because of the privacy of the code.

## Using Whack Me API
We do not recommend using the public API to not let anyone who didn't buy the plugin won't have access to files.<br>To access
our API we recommend to add it as a system library with using some shading systems such as Maven or Gradle.

Firstly, create a folder and put the plugin in that folder then follow the instructions below.

### Maven dependency

```xml
<dependency>
    <groupId>me.despical</groupId>
    <artifactId>whack-me</artifactId>
    <version>${version-here}</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/whack-me-1.0.1.jar</systemPath>
</dependency>
```

### Gradle dependency
```
dependencies {
    compile files('libs/whack-me-1.0.1.jar')
    // or include all the jars in the folder
    compile fileTree(dir: 'libs', include: '*.jar')
}
```
