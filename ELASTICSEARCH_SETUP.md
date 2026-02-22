# Elasticsearch Setup

## 1. Run Elasticsearch

Spring Boot 4.0 requires **Elasticsearch 9.x** (8.x causes connection errors).

```bash
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:9.2.5
```

If ES 8.x is already running: `docker stop elasticsearch && docker rm elasticsearch` first.

## 2. Load Sample Data

```bash
curl -X POST "http://localhost:9200/_bulk" -H "Content-Type: application/x-ndjson" --data-binary @sample-products.json
```

## 3. Start App & Test

```bash
./gradlew bootRun
```

Then in another terminal:

```bash
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{"searchTerm": "wireless"}'
```
