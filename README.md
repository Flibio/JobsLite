JobsLite
========

**Currently not stable and under heavy development**

A minecraft plugin for Sponge platforms.
Dedicated Jobs plugin.


## Links ##
* [Source]
* [Wiki]
* [Issues]

## License ##
This plugin is licensed under [MIT License].
This means that you are allowed to code in any way you would like.

## Prerequisites ##
* [Java] 8

## Clone ##
The following steps will ensure your project is cloned properly

1. `git clone git@github.com:Flibio/JobsLite.git`
2. `cd JobsLite`

## Development Environment ##
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for
Windows systems in place of any 'gradle' command.

If you are a contributor, it is important that your development environment is setup properly. After cloning, as shown
above, follow the given steps for your ide:

#### [IntelliJ]

1. `gradle idea --refresh-dependencies`

#### [Eclipse]

1. `gradle eclipse --refresh-dependencies`

## Updating your Clone ##
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for
Windows systems in place of any 'gradle' command.

The following steps will update your clone with the official repo.

* `git pull`
* `gradle --refresh-dependencies`

## Building
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for
Windows systems in place of any 'gradle' command.

We use [Gradle] for JobsLite.

In order to build JobsLite you simply need to run the `gradle` command.
You can find the compiled JAR file in `./build/libs` labeled similarly to 'JobsLite-x.x.x.jar'.

[Source]: https://github.com/Flibio/JobsLite/
[Wiki]: https://github.com/Flibio/JobsLite/wiki
[Issues]: https://github.com/Flibio/JobsLite/issues
[MIT License]: https://tldrlegal.com/license/mit-license
[Java]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[IntelliJ]: https://www.jetbrains.com/idea/
[Eclipse]: https://www.eclipse.org/
[Gradle]: https://www.gradle.org/