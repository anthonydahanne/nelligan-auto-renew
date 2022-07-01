#  Nelligan Auto Renew

A simple project to automatically renew the items (books, videos, etc.) you borrowed from one of the city of Montreal libraries (Nelligan network).

Mostly inspired by https://github.com/anthonydahanne/MyBanq and by https://github.com/julbrs/nelligan-api

This project uses Quarkus, the Supersonic Subatomic Java Framework.


# Run the project

Before you can run the project, you'll need a couple of environment variables to be set. (you could also use `application.properties` or VM arguments to configure the app)

```shell
export QUARKUS_MAILER_HOST=smtp.sendgrid.net
export QUARKUS_MAILER_FROM=nelligan@montreal.ca
export QUARKUS_MAILER_USERNAME=nelligan
export QUARKUS_MAILER_PASSWORD=password

export RENEW_EMAIL_DESTINATION=nelligan@montreal.ca

export NELLIGAN_CREDENTIALS_0__USERNAME=1277700000000 && export NELLIGAN_CREDENTIALS_0__PASSWORD=000000
export NELLIGAN_CREDENTIALS_1__USERNAME=1277700000001 && export NELLIGAN_CREDENTIALS_1__PASSWORD=111111
```

When the application starts up you should see:

```shell
[info]2022-07-01 22:12:29,846 DEBUG [net.dah.nel.aut.ren.Startup] (main) Starting with username 12777000000000, password seems to be set, it's 6 characters long.
[info]2022-07-01 22:12:29,847 DEBUG [net.dah.nel.aut.ren.Startup] (main) Starting with username 12777000000001, password seems to be set, it's 6 characters long.
[info]2022-07-01 22:12:29,847 DEBUG [net.dah.nel.aut.ren.Startup] (main) Starting with username 12777000000002, password seems to be set, it's 6 characters long.
[info]2022-07-01 22:12:29,986 INFO  [io.quarkus] (main) nelligan-auto-renew 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.10.1.Final) started in 1.600s. Listening on: http://0.0.0.0:8080
[info]2022-07-01 22:12:29,987 INFO  [io.quarkus] (main) Profile prod activated.
[info]2022-07-01 22:12:29,987 INFO  [io.quarkus] (main) Installed features: [cdi, mailer, qute, rest-client, rest-client-jackson, resteasy-jackson, resteasy-multipart, scheduler, smallrye-context-propagation, vertx]
[info]2022-07-01 22:12:32,173 INFO  [net.dah.nel.aut.ren.ScheduledOperation] (executor-thread-0) Working with patron named XXX, yyy; with 25 documents on file, will renew documents due before 1 days
[info]2022-07-01 22:12:32,898 INFO  [net.dah.nel.aut.ren.ScheduledOperation] (executor-thread-0) Working with patron named AAA, bbb; with 0 documents on file, will renew documents due before 1 days
[info]2022-07-01 22:12:33,359 INFO  [net.dah.nel.aut.ren.ScheduledOperation] (executor-thread-0) Working with patron named 123, 456; with 7 documents on file, will renew documents due before 1 days
```

By default, the app will check documents due date every day, and try and renew them if they are to be renewed in the next day.

##Deploying to fly.io

```shell
./mvnw clean package
flyctl launch --dockerfile ./src/main/docker/Dockerfile.jvm
flyctl secrets set QUARKUS_MAILER_HOST=smtp.sendgrid.net QUARKUS_MAILER_FROM=nelligan@montreal.ca
# etc.
./mvnw clean package
flyctl deploy --dockerfile ./src/main/docker/Dockerfile.jvm
```


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/nelligan-auto-renew-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

