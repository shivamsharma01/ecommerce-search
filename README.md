# Search service

Spring Boot service that exposes product search over **Elasticsearch**. It targets the `products` index and supports full-text search on name and description, plus optional e-commerce filters and pagination.

## Requirements

- **Java 17**
- **Elasticsearch 9.x** — Spring Boot 4.x expects Elasticsearch 9; 8.x can cause connection errors.

## Configuration

Settings live in `src/main/resources/application.yml`:

| Property | Default | Description |
|----------|---------|-------------|
| `spring.application.name` | `search` | Application name |
| `server.port` | `8083` | HTTP port |
| `spring.elasticsearch.uris` | `http://localhost:9200` | Elasticsearch cluster URL(s) |

Override via environment variables (Spring relaxed binding), for example:

- `SPRING_ELASTICSEARCH_URIS=http://es:9200`
- `SERVER_PORT=8080`

## Run Elasticsearch locally

```bash
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:9.2.5
```

## Run the application

```bash
./gradlew bootRun
```

## API

### `POST /api/search`

JSON body: search term (required), optional filters, pagination.

**Request body**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `searchTerm` | string | yes | Matched against product `name` and `description` (OR) |
| `filters` | object | no | See filters below |
| `page` | int | no | Zero-based page (default `0`) |
| `size` | int | no | Page size (default `20`) |
| `sortBy` | string | no | Present on the model; query sorting is not applied in the current implementation |
| `sortOrder` | string | no | Same as `sortBy` |

**Filters** (`filters` object)

| Field | Description |
|-------|-------------|
| `categories` | List of category values (`categories` field, keyword) |
| `brands` | List of brands |
| `minPrice` / `maxPrice` | Price range on `price` |
| `inStock` | If `true`, only `inStock: true` |
| `minRating` | Minimum `rating` |
| `attributes` | Strings `"key:value"` → match `attributes.<key>` (e.g. `color:red`) |

**Response**

- `results`: array of products (`id`, `name`, `description`, `categories`, `brand`, `price`, `imageUrl`, `rating`, `inStock`, `attributes`)
- `totalHits`, `page`, `size`, `totalPages`

**Example**

```bash
curl -s -X POST http://localhost:8083/api/search \
  -H "Content-Type: application/json" \
  -d '{
    "searchTerm": "laptop",
    "page": 0,
    "size": 10,
    "filters": {
      "categories": ["electronics"],
      "minPrice": 100,
      "maxPrice": 2000,
      "inStock": true
    }
  }'
```

## Stack

- Spring Boot 4.0 (Web, Data Elasticsearch, Validation)
- Lombok, MapStruct
- Tests: Spring Boot Test, Testcontainers (Elasticsearch)

## Build and test

```bash
./gradlew build
```
