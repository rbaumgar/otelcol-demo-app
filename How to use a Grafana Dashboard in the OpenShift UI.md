
OpenShift Console UI Monitoring Dashboard Definition
https://access.redhat.com/solutions/7013854


oc create cm apm-dashboard --from-file APM.json 

# To appear in the "Administrator" UI, it needs to have the following label:
oc label cm apm-dashboard   console.openshift.io/dashboard='true'

# To appear in the "Developer" UI, it needs to have the following label:
oc label cm apm-dashboard   console.openshift.io/odc-dashboard='true'

