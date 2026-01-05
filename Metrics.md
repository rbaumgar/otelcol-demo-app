To send application metrics from OpenTelemetry to OpenShift’s **User-Workload Monitoring**, you have two primary methods: the "Scrape" method (OpenShift pulls data from the Collector) or the "Push" method (the Collector sends data to OpenShift).

On OpenShift, the **Scrape method** is the standard, most reliable approach as it leverages the built-in Prometheus Operator.

---

## Step 1: Enable User-Workload Monitoring

First, ensure your cluster is configured to monitor user-defined projects.

1. Check if the configuration exists:
```bash
oc get configmap cluster-monitoring-config -n openshift-monitoring
```

2. If it doesn't, or if `enableUserWorkload` is not `true`, create/edit the ConfigMap:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: cluster-monitoring-config
  namespace: openshift-monitoring
data:
  config.yaml: |
    enableUserWorkload: true
```

---

## Step 2: Configure the Collector to Expose Metrics

You must configure the Collector to act as a Prometheus target. This involves adding a `prometheus` exporter and enabling the Operator to create a `ServiceMonitor`.

Update your `OpenTelemetryCollector` CR:

```yaml
apiVersion: opentelemetry.io/v1beta1
kind: OpenTelemetryCollector
metadata:
  name: otel
  namespace: otel-demo
spec:
  observability:
    metrics:
      # This tells the Operator to automatically create a ServiceMonitor
      enableMetrics: true
  config:
    receivers:
      otlp:
        protocols:
          grpc: {}
    exporters:
      prometheus:
        endpoint: "0.0.0.0:8889"
        # Optional: Add resource attributes as Prometheus labels
        resource_to_telemetry_conversion:
          enabled: true 
    service:
      pipelines:
        metrics:
          receivers: [otlp]
          exporters: [prometheus]
```

---

## Step 3: Verify the ServiceMonitor

The OpenTelemetry Operator will automatically detect `enableMetrics: true` and create a **ServiceMonitor** in your namespace. This resource tells OpenShift's Prometheus instance exactly where to find your OTel metrics.

Verify it was created:

```bash
oc get servicemonitor -n otel-demo
```

---

## Step 4: View Metrics in the Console

Once configured, your application metrics (sent via OTLP to the collector) are converted to Prometheus format and scraped by the platform.

1. Switch to the **Developer** perspective in the OpenShift Web Console.
2. Go to **Observe > Metrics**.
3. In the "Query" box, try searching for a metric emitted by your app or a standard OTel metric like `otelcol_process_uptime`.
4. **Note:** It may take 1-2 minutes for the first data points to appear after the ServiceMonitor is created.

---

## Summary of the Data Flow

| Component | Role |
| --- | --- |
| **App SDK** | Pushes OTLP metrics to the Collector. |
| **Collector Receiver** | Accepts the OTLP data. |
| **Collector Exporter** | Hosts a `/metrics` endpoint on port `8889`. |
| **ServiceMonitor** | Tells OpenShift Prometheus to scrape that port. |
| **User Prometheus** | Stores the data and makes it available for alerts/dashboards. |

---

### Pro-Tip: The "Push" Alternative

If you prefer to **push** metrics directly to the OpenShift monitoring stack (Remote Write) rather than having Prometheus scrape the collector, you would use the `prometheusremotewrite` exporter. However, this requires handling authentication via a ServiceAccount token, making the **Scrape** method (Step 2 above) the much simpler "OpenShift-native" choice.

## What's next?

To visualize these metrics effectively, you can use the built-in **OpenShift Monitoring UI**.

---

## The OpenShift Native Way 

Since you enabled **User-Workload Monitoring**, OpenShift already provides a place to query and view these metrics without installing extra tools.

1. Switch to the **Developer Perspective** in the web console.
2. Go to **Observe > Metrics**.
3. In the **Expression** field, enter this PromQL query to see your application's request rate:
```promql
sum(rate(http_server_duration_count[5m])) by (service_name)
```

4. To see the Collector's own health (how much data it's processing), try:
```promql
sum(rate(otelcol_receiver_accepted_metric_points[5m])) by (receiver)
```

