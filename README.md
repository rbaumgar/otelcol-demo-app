In this project you will  find two interesting guides:

- [Using OpenTelemetry and Jaeger with Your Own Application](OpenTelemetry.md)
- [Using OpenTelemetry and Grafana Tempo with Your Own Services/Application](OpenTelemetry_with_Tempo.md)
- [Service Performance Monitoring (SPM) with Jaeger of your Own Application](ServicePerformanceMonitoring.md)

# otelcol-demo-app

Quarkus demo app to show using OpenTelemetry and Jaeger with Your Own Services/Application

Quarkus guide: [Quarkus - Using OpenTelemetry](https://quarkus.io/guides/opentelemetry)

The application has APIs */hello*, */hello/text*, */sayHello/text*, */sayRemote/text* and */prime/number*.

# Interactive run

You can run the application by

```shell
$ mvn quarkus:dev
[INFO] Scanning for projects...
[INFO] 
[INFO] -----------------< org.acme:opentelemetry-quickstart >------------------
[INFO] Building opentelemetry-quickstart 1.0.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- quarkus-maven-plugin:2.5.2.Final:dev (default-cli) @ opentelemetry-quickstart ---
[INFO] Invoking org.apache.maven.plugins:maven-resources-plugin:2.6:resources) @ opentelemetry-quickstart
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO] Invoking org.apache.maven.plugins:maven-compiler-plugin:3.1:compile) @ opentelemetry-quickstart
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 2 source files to /home/rbaumgar/demo/jaeger/otelcol-demo-app/target/classes
[INFO] Invoking org.apache.maven.plugins:maven-resources-plugin:2.6:testResources) @ opentelemetry-quickstart
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/rbaumgar/demo/jaeger/otelcol-demo-app/src/test/resources
[INFO] Invoking org.apache.maven.plugins:maven-compiler-plugin:3.1:testCompile) @ opentelemetry-quickstart
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 2 source files to /home/rbaumgar/demo/jaeger/otelcol-demo-app/target/test-classes
Listening for transport dt_socket at address: 5005

--
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2022-01-04 15:56:49,165 INFO  [io.quarkus] (Quarkus Main Thread) my-service 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.5.2.Final) started in 2.495s. Listening on: http://localhost:8080

2022-01-04 15:56:49,169 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2022-01-04 15:56:49,169 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kubernetes, opentelemetry, opentelemetry-otlp-exporter, rest-client, resteasy, smallrye-context-propagation, vertx]
```

and from an other window

```shell
$ curl localhost:8080/hello
hello
$ curl localhost:8080/sayHello/demo1
hello: demo1
$ curl localhost:8080/sayRemote/demo1
hello: demo1 from http://localhost:8080/
$ curl localhost:8080/prime/2797
2797 is prime.
```

# Using OpenTelemetryCollector or Jaeger 1.35+

When you have already an instance of Jaeger and OpenTelemetryCollector running (see [OpenTelemetry](OpenTelemetry.md)) you can expose the collector port to localhost.
Execute in a new window:

```shell
$ oc port-forward deployment/my-otelcol-collector 4317:4317
```

When you run Jaeger version 1.35+ you do no longer need the OpenTelemetryCollector. See [Jaeger](https://medium.com/jaegertracing/introducing-native-support-for-opentelemetry-in-jaeger-eb661be8183c)

```shell
$ oc port-forward svc/my-jaeger-collector 4317:4317
```

# Build an image with podman

```shell
$ mvn clean package -DskipTests
...
$ podman build -f src/main/docker/Dockerfile.jvm -t quay.io/rbaumgar/otelcol-demo-app-jvm .
STEP 1/8: FROM registry.access.redhat.com/ubi8/openjdk-11-runtime
...
Successfully tagged quay.io/rbaumgar/otelcol-demo-app-jvm:latest
8f9e14fd9336b488c27c5b3dfc4dbd41222089b54824137d3cdc6f67aac66565
```

You can also use *docker*.

# Build a quarkus native image

You need to install Graal VM and set the correct pointer.

```shell
$ export GRAALVM_HOME=~/graalvm-ce-java11-21.3.0/
$ export JAVA_HOME=$GRAALVM_HOME
$ mvn package -Pnative -DskipTests -Dquarkus.native.container-runtime=[podman | docker]
$ ls file target/opentelemetry-quickstart-1.0.0-SNAPSHOT-runner

$ target/opentelemetry-quickstart-1.0.0-SNAPSHOT-runner
```


Once that's done, we'll deploy it as an OpenShift application:

```shell
$ oc new-app monitor-demo
```

and expose it to the world:

```shell
$ oc expose service monitor-demo
```

Finally, make sure it's actually done rolling out:

```shell
$ oc rollout status -w dc/monitor-demo
```

# Run the image

```shell
$ podman run -i --rm -p 8080:8080 rbaumgar/otelcol-demo-app-jvm
```

# Push image to registry

Find the *image id* and push it. You might need to login at first.

```shell
$ podman push quay.io/rbaumgar/otelcol-demo-app-jvm
Getting image source signatures
Copying blob 446c899d1b6b done  
Copying blob a60e61cb3ce5 done  
Copying blob 302f8dece865 done  
Copying blob 762908e901a6 skipped: already exists  
Copying blob 371fa27c3d36 skipped: already exists  
Copying blob ad5cedfcc311 skipped: already exists  
Copying config 4f6eb5ee54 done  
Writing manifest to image destination
Storing signatures
```



Quarkus guide: https://quarkus.io/guides/opentelemetry

```shell
$ oc apply -f src/main/openshift/my-otelcol.yaml 
configmap/my-otelcol-cabundle created
opentelemetrycollector.opentelemetry.io/my-otelcol created

$ ./mvnw clean package -Dquarkus.kubernetes.deploy=true
```