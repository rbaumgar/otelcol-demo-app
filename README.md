# otelcol-demo-app

Quarkus demo app to show using OpenTelemetry and Jaeger with Your Own Services/Application

Quarkus guide: [Quarkus - USING OPENTELEMETRY](https://quarkus.io/guides/opentelemetry)

The application has APIs */hello*, */sayHello/text* and */sayRemote/text*

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
```

# Using OpenTelemetryCollector

When you have already an instance of Jaeger and OpenTelemetryCollector running (see [OpenTelemetry](OpenTelemetry.md)) you can expose the collector port to localhost.
Execute in a new window:

```shell
$ oc port-forward deployment/my-otelcol-collector 4317:4317

```

So the number of API calls to *hello* is **greetings**.

# Build an image with podman

```shell
$ mvn clean package -DskipTests
...
$ podman build -f src/main/docker/Dockerfile.jvm -t quarkus/otelcol-demo-app-jvm .
exec java -Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -javaagent:/opt/agent-bond/agent-bond.jar=jmx_exporter{{9779:/opt/agent-bond/jmx_exporter_config.yml}} -XX:+UseParallelGC -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40 -XX:+ExitOnOutOfMemoryError -cp . -jar /deployments/app.jar
2020-04-17 08:15:33,649 INFO  [io.quarkus] (main) otelcol-demo-app 1.0-SNAPSHOT (running on Quarkus 1.0.0.CR1) started in 0.593s. Listening on: http://0.0.0.0:8080
2020-04-17 08:15:33,667 INFO  [io.quarkus] (main) Profile prod activated. 
2020-04-17 08:15:33,667 INFO  [io.quarkus] (main) Installed features: [cdi, resteasy, smallrye-metrics]
```

You can also use *docker*.

# Build a quarkus native image

You need to install Graal VM and set the correct pointer.

```shell
$ export GRAALVM_HOME=~/graalvm-ce-java11-21.3.0/
$ export JAVA_HOME=$GRAALVM_HOME
$ mvn package -Pnative -DskipTests -Dquarkus.native.container-runtime=[podman | docker]
$ ls file target/otelcol-demo-app-1.0-SNAPSHOT-runner

$ target/otelcol-demo-app-1.0-SNAPSHOT-runner

$ 

```

```
$ oc new-build quay.io/quarkus/ubi-quarkus-native-binary-s2i:1.0 --binary --name=monitor-demo -l app=monitor-demo

This build uses the new Red Hat Universal Base Image, providing foundational software needed to run most applications, while staying at a reasonable size.

And then start and watch the build, which will take about a minute or two to complete:

$ oc start-build monitor-demo --from-file=target/otelcol-demo-app-1.0-SNAPSHOT-runner --follow

Once that's done, we'll deploy it as an OpenShift application:

$ oc new-app monitor-demo

and expose it to the world:

$ oc expose service monitor-demo

Finally, make sure it's actually done rolling out:

$ oc rollout status -w dc/monitor-demo
```

# Run the image

```shell
$ podman run -i --rm -p 8080:8080 quarkus/otelcol-demo-app-jvm
```

# Push image to registry

Find the *image id* and push it. You might need to login at first.

```shell
$ podman images localhost/quarkus/otelcol-demo-app-jvm
REPOSITORY                               TAG      IMAGE ID       CREATED      SIZE
localhost/quarkus/otelcol-demo-app-jvm   latest   0a68fa7e569f   2 days ago   108 MB
$ podman push `podman images localhost/quarkus/otelcol-demo-app-jvm -q` docker://quay.io/rbaumgar/otelcol-demo-app-jvm
```



Quarkus guide: https://quarkus.io/guides/opentelemetry


$ oc apply -f src/main/openshift/my-otelcol.yaml 
configmap/my-otelcol-cabundle created
opentelemetrycollector.opentelemetry.io/my-otelcol created

$ ./mvnw clean package -Dquarkus.kubernetes.deploy=true