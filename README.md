Quarkus guide: https://quarkus.io/guides/opentelemetry


$ oc apply -f src/main/openshift/my-otelcol.yaml 
configmap/my-otelcol-cabundle created
opentelemetrycollector.opentelemetry.io/my-otelcol created

$ ./mvnw clean package -Dquarkus.kubernetes.deploy=true