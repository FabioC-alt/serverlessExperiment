FROM debian:stable-slim

# Install build tools and libcurl
RUN apt-get update && apt-get install -y \
    build-essential \
    libcurl4-openssl-dev \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Copy and compile the program
COPY latency_test.c /app/
WORKDIR /app
RUN gcc latency_test.c -o latency_test -lcurl

# Optional: provide default URL through entrypoint
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

