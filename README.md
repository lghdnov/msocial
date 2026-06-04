# msocial

Социальная сеть на Spring Boot: пользователи с профилями и аватарами, посты с медиа, древовидные комментарии. Аутентификация через JWT и federated-логин Matrix (OIDC).

## Стек

- **Java 25**, **Spring Boot 4.0.6** (Web MVC, Data JPA, Security, Actuator, Flyway, Validation)
- **PostgreSQL** + **Flyway** для миграций
- **JWT** (`io.jsonwebtoken:jjwt 0.13.0`) + **OIDC** верификация через Matrix-сервер
- **Caffeine** для in-memory кэширования
- **MapStruct** + **Lombok** для маппинга и boilerplate
- **springdoc-openapi** + **Scalar** для документации API
- **JUnit 5**, **Mockito**, **WireMock**, **Testcontainers (PostgreSQL)**
- **JaCoCo** для отчётов покрытия

## Архитектура

Проект построен в стиле **гексагональной архитектуры (порты и адаптеры)**. Доменные фичи изолированы друг от друга и общаются через интерфейсы в пакете `api`.

```
src/main/java/lghdnov/msocial/
├── MsocialApplication.java
├── common/              # общее: exceptions, security (AuthFilter)
├── config/              # SecurityConfig, WebConfig, OpenApiConfig, RestClientConfig
├── controller/          # вспомогательные контроллеры (EchoController)
└── feature/
    ├── auth/            # JWT, сессии, OIDC (Matrix federation)
    │   ├── api/         # порты: AuthCommand, OidcVerification, Session, Token*
    │   ├── controller/  # /auth/login, /auth/refresh
    │   ├── entity/      # Session, JwtClaims, MatrixUserInfo
    │   ├── infrastructure/ # JwtProvider, MatrixFederationAdapter
    │   ├── presentation/   # DTO + AuthMapper
    │   └── service/     # AuthService, SessionService, TokenService
    ├── user/            # профиль, аватар, медиа
    ├── post/            # посты + медиа (локальное хранилище)
    └── comment/         # древовидные комментарии, лимиты
```

Каждая фича содержит:

- `api/` - порты для межфичевого взаимодействия
- `controller/` - REST-эндпоинты
- `service/` - бизнес-логика
- `entity/` + `repository/` - JPA-сущности и репозитории
- `infrastructure/` - внешние адаптеры (HTTP, файловая система)
- `presentation/` - DTO + MapStruct-мапперы

## Быстрый старт

### Требования

- JDK 25
- Docker (для Testcontainers в интеграционных тестах)
- PostgreSQL 16+ (для локального запуска)

### Переменные окружения

| Переменная | Назначение | По умолчанию |
|---|---|---|
| `SERVER_PORT` | HTTP-порт приложения | `8080` |
| `DB_URL` | JDBC URL Postgres | `jdbc:postgresql://localhost:5432/msocial` |
| `DB_USERNAME` / `DB_PASSWORD` | Креды БД | `postgres` / `postgres` |
| `JWT_SECRET` | Секрет для подписи JWT | `your-secret-key-here-change-in-production` |
| `JWT_EXPIRATION` | Время жизни токена, мс | `86400000` (24 ч) |
| `DEV_AUTH_SKIP_VERIFY` | Пропускать проверку OIDC (только dev) | `true` |
| `MATRIX_BASE_URL` | Базовый URL Matrix homeserver | `https://matrix.org` |
| `STORAGE_TYPE` | Тип хранилища медиа | `local` |
| `AVATAR_STORAGE_PATH` | Путь для аватаров | `./uploads/avatars` |
| `TRACK_STORAGE_PATH` | Путь для треков | `./uploads/tracks` |
| `POST_MEDIA_STORAGE_PATH` | Путь для медиа постов | `./uploads/post-media` |
| `LOG_LEVEL` | Уровень логирования | `INFO` |

### Локальный запуск

```bash
# 1. Запустить PostgreSQL (через docker compose или локально)
# 2. Переопределить секреты в .env или экспортировать переменные
./gradlew bootRun
```

Сервис поднимется на `http://localhost:8080`. Flyway автоматически накатит миграции из `src/main/resources/db/migration/`.

### Сборка и запуск в Docker

```bash
./gradlew bootJar
docker build -t msocial .
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/msocial \
  -e JWT_SECRET=$(openssl rand -base64 48) \
  msocial
```

Healthcheck доступен на `http://localhost:8081/actuator/health` (Actuator вынесен на отдельный порт 8081).

## Документация API

- **Scalar UI:** `http://localhost:8080/scalar`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

Конфигурация описана в `src/main/resources/scalar.yaml`.

## Тестирование

```bash
# все тесты
./gradlew test

# отчёт покрытия JaCoCo
./gradlew jacocoTestReport
# отчёты: build/reports/jacoco/test/html/index.html
#         build/reports/jacoco/test/report.xml
```

Структура тестов повторяет модульную структуру `main`:

```
src/test/java/lghdnov/msocial/
├── MsocialApplicationTests.java
├── TestcontainersConfiguration.java   # общий Testcontainers-контекст (PostgreSQL)
├── TestMsocialApplication.java       # альтернативный entry-point
└── feature/
    ├── auth/      # AuthServiceIntegrationTest, TokenServiceTest
    ├── user/      # UserRepositoryTest, UserServiceTest
    ├── post/      # PostRepositoryTest, PostServiceTest
    └── comment/   # CommentRepositoryTest
```

Интеграционные тесты поднимают Postgres в контейнере (нужен запущенный Docker). Юнит-тесты используют Mockito и WireMock.

## Миграции БД

Версионированные SQL-файлы в `src/main/resources/db/migration/`:

| Версия | Описание |
|---|---|
| `V1__create_auth_sessions.sql` | сессии авторизации |
| `V2__create_users_and_profiles.sql` | пользователи и профили |
| `V3__create_avatars_and_update_track.sql` | аватары + треки |
| `V4__create_posts_and_post_media.sql` | посты и прикреплённые медиа |
| `V5__add_published_to_posts.sql` | флаг публикации постов |
| `V6__create_comments.sql` | древовидные комментарии |

Миграции применяются автоматически при старте приложения (`spring.flyway.enabled: true`).

## CI/CD

`.github/workflows/docker-publish.yml` - пайплайн сборки и публикации Docker-образа (GitHub Actions).

## Лицензия

Не указана.
