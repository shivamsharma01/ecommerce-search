# Elasticsearch Setup

## 1. Run Elasticsearch

Spring Boot 4.0 requires **Elasticsearch 9.x** (8.x causes connection errors).

```bash
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:9.2.5
```