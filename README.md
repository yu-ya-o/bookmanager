# 📚 書籍管理システム Web API

このリポジトリは、**書籍および著者情報を管理するWeb API**です。  
Kotlin + Spring Boot + jOOQ を用いて実装されており、RESTful APIとして機能します。  
本APIは、**書籍管理業務や関連サービスとの統合**を目的としています。
## 📄 補足

- README、コーディング、単体テストの一部は生成AIを利用して作成しています。
- 本APIはコーディングテスト用に作成されていますが、業務を想定した実践的な実装を目指しています。

---

## ✅ 機能概要

- 書籍と著者の登録・更新
- 著者に紐づく書籍の取得

### 書籍の属性

| 属性名            | 説明                            | 制約                 |
|-------------------|-------------------------------|--------------------|
| `title`           | 書籍のタイトル                       | 必須                 |
| `price`           | 書籍の価格                         | 必須、0以上             |
| `authors`         | 書籍の著者リスト                      | 最低1人、複数可           |
| `publishedStatus` | 出版状況（UNPUBLISHED / PUBLISHED） | 必須、出版済み→未出版への変更は禁止 |

### 著者の属性

| 属性名      | 説明                   | 制約             |
|-------------|------------------------|----------------|
| `name`      | 著者名                 | 必須             |
| `birthday`  | 生年月日               | 必須、現在より過去の日付であること |
| `books`     | 執筆した書籍           | 複数可            |

---

## 🚀 APIエンドポイント

### 書籍登録

`POST /books`

```json
{
  "title": "Effective Kotlin",
  "price": 3200,
  "authorIds": [1, 2],
  "publishedStatus": "UNPUBLISHED"
}
```

### 書籍更新

`PUT /books/{id}`  
※出版済み → 未出版 への変更は不可

### 著者登録

`POST /authors`

```json
{
  "name": "山田 太郎",
  "birthday": "1980-05-12"
}
```

### 著者更新

`PUT /authors/{id}`

### 著者に紐づく書籍一覧取得

`GET /authors/{id}/books`

---

## 📦 セットアップ手順

### 必要環境

- Java 21
- Docker & Docker Compose

### プロジェクト生成元

- [Spring Initializr](https://start.spring.io/)
- 使用オプション：
    - Gradle（Groovy DSL）
    - Kotlin
    - JOOQ Access Layer
    - Flyway Migration
    - PostgreSQL Driver
    - Docker Compose Support

### 起動方法

```bash
# 1. DockerでDBを起動
docker-compose up -d

# 2. flywayマイグレーションを実行
./gradlew flywayMigrate

# 3. jooqコード生成
./gradlew jooqCodegen

# 3. アプリケーション起動
./gradlew bootRun
```

---

## 🛠️ 技術スタック

- **言語**: Kotlin
- **フレームワーク**: Spring Boot
- **DBアクセス**: jOOQ
- **DBマイグレーション**: Flyway
- **RDBMS**: PostgreSQL
- **その他**: Docker Compose

---
