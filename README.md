# Search service

HTTP API over **Elasticsearch** for the shared **`products`** index (written by product-indexer). Optional JWT on `POST /api/search` when `APP_SECURITY_ENABLED=true`.

## Requirements

- Java 17
- Elasticsearch **9.x** (aligned with Spring Boot 4 stack in this module)

## Run Elasticsearch locally

```bash
docker run -d --name elasticsearch -p 9200:9200 \
  -e "discovery.type=single-node" -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:9.2.5
```

## Run the app

```bash
./gradlew bootRun
```

| Variable | Purpose |
|----------|---------|
| `SERVER_PORT` | Default `8083` |
| `SPRING_ELASTICSEARCH_URIS` | ES URL(s) |
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
  -e SPRING_ELASTICSEARCH_URIS=http://host.docker.internal:9200 \
  search:local
```

Kubernetes: **`ecomm-infra/deploy/k8s/apps/search/`** — image URI pattern **`asia-south2-docker.pkg.dev/ecommerce-491019/docker-apps/search:<short-sha>`** (Cloud Build). See **`ecomm-infra/README.md`**.
