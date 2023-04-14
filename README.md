# GraphQL Ktor + GraalVM Native Example

Example app showcasing the usage of [graphql-kotlin](https://github.com/ExpediaGroup/graphql-kotlin/) library to build
a GraphQL Ktor server that can be compiled to GraalVM native.

## Building locally

This project uses Gradle and you can build it locally using

```shell script
./gradlew clean build
```

## Running locally

* Run `com.example.Application.kt` directly from your IDE
* Alternatively you can also use the Gradle application plugin by running `./gradlew run` from the command line.

Once the app has started you can explore the example schema by opening the GraphiQL IDE endpoint at http://localhost:8080/graphiql.

## GraalVM Native

### Building Native Image

In order to generate GraalVM native image we need to run following task 

```shell
./gradlew nativeCompile
```

### Running Native Image

Once application is compiled by native image, you will find the native executable under `build/native/nativeCompile`.