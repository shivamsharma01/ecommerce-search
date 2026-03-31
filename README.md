# Search service

HTTP API over **OpenSearch** (Elasticsearch-compatible REST API) for the shared **`products`** index (written by product-indexer). Optional JWT on `POST /api/search` when `APP_SECURITY_ENABLED=true`.

## Requirements

- Java 21
- OpenSearch **2.x / 3.x**

## Run OpenSearch locally

```bash
docker run -d --name opensearch-local \
  -p 9200:9200 -p 9600:9600 \
  -e discovery.type=single-node \
  -e DISABLE_SECURITY_PLUGIN=true \
  -e OPENSEARCH_JAVA_OPTS="-Xms512m -Xmx512m" \
  opensearchproject/opensearch:2.19.2
```

## Run the app

```bash
./gradlew bootRun
```

| Variable | Purpose |
|----------|---------|
| `SERVER_PORT` | Default `8083` |
| `OPENSEARCH_URIS` | OpenSearch URL(s) |
| `APP_SECURITY_ENABLED` | `true` to require Bearer JWT |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | Issuer when security is on |

## API

`POST /api/search` with JSON body: `searchTerm` (required), optional `filters`, `page`, `size`, `sortBy`, `sortOrder`.

## Build and test

```bash
./gradlew build
```

Tests are a light Spring context load (no Elasticsearch container).

## Container

```bash
docker build -t search:local .
docker run --rm -p 8083:8083 \
  -e OPENSEARCH_URIS=http://host.docker.internal:9200 \
  search:local
```

Kubernetes: **`ecomm-infra/deploy/k8s/apps/search/`** — image URI pattern **`asia-south2-docker.pkg.dev/ecommerce-491019/docker-apps/search:<short-sha>`** (Cloud Build). See **`ecomm-infra/README.md`**.
