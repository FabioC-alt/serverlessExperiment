#!/bin/bash
set -e

echo '🟢 Creating serving resources'
sudo k3s kubectl apply -f serving-crds.yml 
sudo k3s kubectl apply -f serving-core.yml 
sudo k3s kubectl apply -f kourier.yaml 

echo '🟢 Setting Kourier as default networking layer'
sudo k3s kubectl patch configmap/config-network \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"ingress-class":"kourier.ingress.networking.knative.dev"}}'

echo "🔍 Validating the ingress-class patch..."
INGRESS_CLASS=$(sudo k3s kubectl get configmap config-network \
  --namespace knative-serving \
  -o jsonpath='{.data.ingress-class}')

if [[ "$INGRESS_CLASS" == "kourier.ingress.networking.knative.dev" ]]; then
  echo "✅ Validation successful: ingress-class is correctly set to '$INGRESS_CLASS'"
else
  echo "❌ Validation failed: ingress-class is set to '$INGRESS_CLASS'"
  exit 1
fi

echo "🛠️ Patching config-domain ConfigMap..."
sudo k3s kubectl patch configmap/config-domain \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"127.0.0.1.sslip.io":""}}'

echo "✅ Patch applied. Validating..."

# Get the configmap and search for the key
output=$(sudo k3s kubectl describe configmap/config-domain --namespace knative-serving)

if echo "$output" | grep -q "127.0.0.1.sslip.io"; then
    echo "✅ Validation successful: 127.0.0.1.sslip.io found in config-domain."
else
    echo "❌ Validation failed: 127.0.0.1.sslip.io not found in config-domain."
    exit 1
fi

echo 'Deploying Minio'
sudo k3s kubectl apply -f minio-dev.yml

echo '🎉 Cluster is configured correctly, enjoy :)'

