# File Upload App with Google SSO

Веб-приложение на Java Spark Framework для загрузки и управления файлами с аутентификацией через Google SSO.

## Возможности

- Аутентификация через Google Single Sign-On
- Загрузка файлов
- Просмотр списка загруженных файлов
- Скачивание файлов
- Каждый пользователь видит только свои файлы

## Технологии

- Java 11
- Spark Framework (веб-фреймворк)
- Freemarker (шаблонизатор)
- H2 Database (встроенная БД для метаданных)
- Google OAuth 2.0
- Maven

## Настройка

### 1. Получить Google OAuth Client ID

1. Перейти в [Google Cloud Console](https://console.cloud.google.com/)
2. Создать новый проект или выбрать существующий
3. Включить Google+ API
4. Перейти в "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"
5. Выбрать тип приложения "Web application"
6. Добавить Authorized redirect URIs: `http://localhost:4567`
7. Скопировать Client ID

### 2. Установить переменную окружения

```bash
export GOOGLE_CLIENT_ID="ваш-client-id"
```

### 3. Собрать проект

```bash
mvn clean package
```

### 4. Запустить приложение

```bash
java -jar target/file-upload-app-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Или через Maven:
```bash
mvn exec:java -Dexec.mainClass="com.example.App"
```

## Использование

1. Откройте браузер и перейдите по адресу: `http://localhost:4567`
2. Нажмите кнопку "Sign in with Google"
3. Авторизуйтесь через Google аккаунт
4. Загрузите файлы через форму загрузки
5. Просматривайте и скачивайте свои файлы

## Структура проекта

```
src/main/
├── java/com/example/
│   ├── App.java                    # Главный класс приложения
│   ├── auth/
│   │   └── GoogleAuth.java         # Google OAuth аутентификация
│   ├── controller/
│   │   ├── AuthController.java     # Контроллер аутентификации
│   │   └── FileController.java     # Контроллер для работы с файлами
│   ├── model/
│   │   └── FileMetadata.java       # Модель метаданных файла
│   ├── service/
│   │   └── FileService.java        # Сервис для работы с файлами
│   └── db/
│       └── Database.java           # Настройка БД
└── resources/
    └── templates/
        ├── login.ftl               # Шаблон страницы входа
        └── files.ftl               # Шаблон страницы файлов
```

## API Endpoints

- `GET /` - Страница входа
- `GET /files` - Страница со списком файлов (требует авторизации)
- `POST /api/auth/google` - Верификация Google токена
- `GET /api/auth/logout` - Выход из системы
- `GET /api/files` - Получить список файлов пользователя
- `POST /api/files/upload` - Загрузить файл
- `GET /api/files/:id/download` - Скачать файл

## Безопасность

- Все файлы привязаны к пользователю через Google User ID
- Пользователи могут видеть только свои файлы
- Файлы хранятся локально в папке `uploads/`
- Метаданные файлов хранятся в H2 базе данных
