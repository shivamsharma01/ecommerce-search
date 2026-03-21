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
| `app.security.enabled` | `false` | When `true`, `POST /api/search` requires a **Bearer JWT** (OAuth2 resource server), same model as **product** service |
| `app.security.required-scope` | _(unset)_ | If set, `POST`/`PUT`/… under `/api/**` require that OAuth2 scope (e.g. `search.query` → `SCOPE_search.query`) |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | _(unset)_ | Required when `app.security.enabled=true` — align with **user** service (`AUTH_ISSUER_URI` or env `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`) |

Override via environment variables (Spring relaxed binding), for example:

- `SPRING_ELASTICSEARCH_URIS=http://es:9200`
- `SERVER_PORT=8080`
- `APP_SECURITY_ENABLED=true` and `AUTH_ISSUER_URI=https://your-auth-issuer` for secured deployments

### Security (JWT)

- **Local / tests:** `app.security.enabled` defaults to `false` (open API), matching **product** service local defaults.
- **Deployed:** set `APP_SECURITY_ENABLED=true` and configure **`spring.security.oauth2.resourceserver.jwt.issuer-uri`** (or **`jwk-set-uri`**) to the same issuer used by **user** and **product** services. Call **`POST /api/search`** with header `Authorization: Bearer <access_token>`.
- **Public paths** (no JWT): `GET/HEAD` **health** (`/health`, `/actuator/health`, `/actuator/info`), **Swagger/OpenAPI** (`/v3/api-docs/**`, `/swagger-ui/**`) if enabled, and `OPTIONS /**`.

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
| `size` | int | no | Page size (default `20`, max `100`) |
| `sortBy` | string | no | `relevance` (default, `_score`), `price`, `rating`, or `name` (uses keyword subfield `name.sort`) |
| `sortOrder` | string | no | `asc` or `desc` (default `desc`) |

**Filters** (`filters` object)

| Field | Description |
|-------|-------------|
| `categories` | List of category values (`categories` field, keyword) |
| `brands` | List of brands |
| `minPrice` / `maxPrice` | Price range on `price` (each ≥ 0; if both set, `minPrice` ≤ `maxPrice`) |
| `inStock` | When set, matches that stock flag (`true` = in stock only, `false` = out of stock only) |
| `minRating` | Minimum `rating` (0–5) |
| `attributes` | Entries `"key:value"` → `attributes.<key>`; key: `[a-zA-Z0-9_-]{1,64}`, non-empty value |

Invalid filter or sort values return **400** with a JSON body such as:

```json
{ "error": "validation_failed", "fields": { "searchTerm": "Search term is required" } }
```

Malformed JSON returns **400** with `error: invalid_request_body`. Elasticsearch failures return **503** with `error: search_unavailable`.

**Response**

- `results`: array of products (`id`, `name`, `description`, `categories`, `brand`, `price`, `imageUrl`, `rating`, `inStock`, `attributes`, `version`, `updatedAt`) — same shape as documents written by **product-indexer** to the `products` index
- `totalHits`, `page`, `size`, `totalPages` (page count uses integer-safe math and caps at `Integer.MAX_VALUE` if needed)

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
- Tests: Spring Boot Test (lightweight unit + context smoke; no Docker)

## Build and test

```bash
./gradlew build
```

