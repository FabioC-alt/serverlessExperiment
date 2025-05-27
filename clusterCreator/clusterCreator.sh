#!/bin/bash
set -e

# Optional: Set default domain if not already set
: "${DOMAIN:=example.com}"

echo '🟢 Creating a cluster with kind'
kind create cluster --config deployment.yml

echo '🟢 Creating serving resources'
kubectl apply -f serving-crds.yml 
kubectl apply -f serving-core.yml 
kubectl apply -f kourier.yaml 

echo '🟢 Setting Kourier as default networking layer'
kubectl patch configmap/config-network \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"ingress-class":"kourier.ingress.networking.knative.dev"}}'

echo "🔍 Validating the ingress-class patch..."
INGRESS_CLASS=$(kubectl get configmap config-network \
  --namespace knative-serving \
  -o jsonpath='{.data.ingress-class}')

if [[ "$INGRESS_CLASS" == "kourier.ingress.networking.knative.dev" ]]; then
  echo "✅ Validation successful: ingress-class is correctly set to '$INGRESS_CLASS'"
else
  echo "❌ Validation failed: ingress-class is set to '$INGRESS_CLASS'"
  exit 1
fi

echo "🛠️ Patching config-domain ConfigMap..."
kubectl patch configmap/config-domain \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"127.0.0.1.sslip.io":""}}'

echo "✅ Patch applied. Validating..."

# Get the configmap and search for the key
output=$(kubectl describe configmap/config-domain --namespace knative-serving)

if echo "$output" | grep -q "127.0.0.1.sslip.io"; then
    echo "✅ Validation successful: 127.0.0.1.sslip.io found in config-domain."
else
    echo "❌ Validation failed: 127.0.0.1.sslip.io not found in config-domain."
    exit 1
fi


echo 'Waiting for pod to run'
sleep 300


# Shared pod check function
check_pod() {
  local POD_NAME_PATTERN=$1
  local NAMESPACE=$2
  local PODS_JSON=$(kubectl get pods --namespace "$NAMESPACE" -o json)

  local POD_ENTRY=$(echo "$PODS_JSON" | jq -r \
    --arg name "$POD_NAME_PATTERN" '.items[] | select(.metadata.name | test($name))')

  if [[ -z "$POD_ENTRY" ]]; then
    echo "❌ Pod matching '$POD_NAME_PATTERN' not found in namespace '$NAMESPACE'."
    return 1
  fi

  local POD_NAME=$(echo "$POD_ENTRY" | jq -r '.metadata.name')
  local POD_STATUS=$(echo "$POD_ENTRY" | jq -r '.status.phase')
  local CONTAINER_READY=$(echo "$POD_ENTRY" | jq -r '.status.containerStatuses[0].ready')

  if [[ "$POD_STATUS" == "Running" && "$CONTAINER_READY" == "true" ]]; then
    echo "✅ $POD_NAME is Running and Ready"
  else
    echo "❌ $POD_NAME is not healthy (Status: $POD_STATUS, Ready: $CONTAINER_READY)"
    return 1
  fi
}

# Check Knative Serving pods
echo "🔍 Checking Knative Serving pods..."
KNATIVE_NS="knative-serving"
REQUIRED_PODS=("activator" "autoscaler" "controller" "webhook")

for POD in "${REQUIRED_PODS[@]}"; do
  check_pod "$POD" "$KNATIVE_NS"
done

# Check Kourier pods
echo "🔍 Checking Kourier pods..."
KOURIER_NS="kourier-system"
REQUIRED_PODS=("3scale-kourier-control" "3scale-kourier-gateway")

for POD in "${REQUIRED_PODS[@]}"; do
  check_pod "$POD" "$KOURIER_NS"
done

# ---- Check Kourier service ----
echo "🔍 Checking Kourier service in namespace '$KOURIER_NS'..."
SERVICE_OUTPUT=$(kubectl get service kourier --namespace "$KOURIER_NS" -o json)
SERVICE_TYPE=$(echo "$SERVICE_OUTPUT" | jq -r '.spec.type')
PORTS=$(echo "$SERVICE_OUTPUT" | jq -r '.spec.ports[] | "\(.port):\(.nodePort)/\(.protocol)"' | paste -sd "," -)

if [[ "$SERVICE_TYPE" == "NodePort" ]]; then
  echo "✅ Kourier service type is NodePort"
  echo "   ➤ Ports: $PORTS"
else
  echo "❌ Kourier service is not NodePort (found: $SERVICE_TYPE)"
  exit 1
fi


echo 'Deploying Minio'
kubectl apply -f minio-dev.yml

echo '🎉 Cluster is configured correctly, enjoy :)'

