#!/bin/bash

# Usage: ./curl_latency.sh <url> <number_of_requests>

URL=$1
COUNT=$2

if [ -z "$URL" ] || [ -z "$COUNT" ]; then
  echo "Usage: $0 <url> <number_of_requests>"
  exit 1
fi

total_time=0

for ((i=1; i<=COUNT; i++)); do
  # -o /dev/null to discard output, -s for silent, -w %{time_total} to get total time in seconds
  time_taken=$(curl -o /dev/null -s -w "%{time_total}" "$URL")
  # Convert to milliseconds (multiply by 1000)
  time_ms=$(echo "$time_taken * 1000" | bc)
  echo "Request $i: ${time_ms} ms"
  total_time=$(echo "$total_time + $time_ms" | bc)
done

average=$(echo "scale=2; $total_time / $COUNT" | bc)

echo "Average latency: ${average} ms"

