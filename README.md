# Cloud storage
Облачное файловое хранилище с возможностью хранения и загрузки файлов

# Содержание
1. [Возможности](#features)
2. [Технологии](#stack)
3. [Сборка и запуск](#build)


# Возможности <a id="features"></a>

### Пользователи:
- регистрация
- авторизация
- выход

### Папки и файлы:
- создание
- загрузка
- удаление
- переименование


# Технологии <a id="stack"></a>

### Backend:
- Spring boot
- Spring security
- Spring MVC + Thymeleaf + Bootstrap
- Spring Sessions + Redis
- Spring Data JPA + MySQL
- MinIO
- Junit5, Testcontainers
- Docker, docker-compose


# Сборка и запуск <a id="build"></a>


1. Скопировать репозиторий
```shell
git clone https://github.com/kolobkevic/cloud_storage.git
```
2. Перейти в корневую папку из скопированного репозитория
```shell
cd ./cloud_storage/compose
```
3. Собрать и запустить с помощью докера
```shell
docker-compose -f docker-compose.yaml up
```
4. Открыть в браузере
```shell
http://localhost:8080/
```