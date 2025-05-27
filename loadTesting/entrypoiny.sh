#!/bin/bash

# Default to Knative URL if not provided
URL="${1:-http://helloworld-go.default.example.com}"

echo "Testing latency to $URL"
./latency_test "$URL"

