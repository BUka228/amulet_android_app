# Минимальный backend на Supabase для Amulet

Этот документ описывает **минимальный** набор таблиц в Supabase, достаточный для работы текущего клиента:

- авторизация и пользователи
- паттерны, сегменты и маркеры
- пары, эмоции, quick replies и объятия
- push-уведомления через OneSignal

Структура основана на актуальных доменных моделях (`shared/domain`) и DTO (`core/network/dto`).

---

## 1. Auth и пользователи

### 1.1. Auth

Можно использовать стандартный `auth.users` Supabase для хранения учётных записей.

### 1.2. Таблица `profiles`

Хранит профиль пользователя, связанный с `auth.users`.

- `id` (uuid, PK, FK → auth.users.id)
- `created_at` (timestamptz)
- `updated_at` (timestamptz)
- дополнительные поля профиля по необходимости (имя, аватар и т.п.)

> Клиент в домене работает с `UserId` как со строковым идентификатором; на бэкенде можно использовать `uuid`.

---

## 2. Паттерны, сегменты и маркеры

Соответствие моделям:
- домен: `Pattern`, `PatternSpec`, `PatternMarkers`
- DTO: `PatternDto`, `PatternSpecDto`, `PatternTimelineDto`, `PatternMarkersDto`
- локальная БД: `PatternEntity`, `PatternMarkersEntity`, `TagEntity`, `PatternTagCrossRef`, `PatternShareEntity`

### 2.1. Таблица `patterns`

- `id` (uuid / text, PK)
- `owner_id` (uuid / text, FK → profiles.id, nullable) — владелец паттерна
- `kind` (text, not null) — тип паттерна (строка, маппится на доменный enum)
- `hardware_version` (int, not null)
- `title` (text, not null)
- `description` (text, nullable)
- `spec` (jsonb, not null) — сериализованный `PatternSpecDto`
- `public` (boolean, not null, default false)
- `review_status` (text, nullable)
- `usage_count` (int, nullable)
- `version` (int, not null, default 1)
- `created_at` (timestamptz, nullable)
- `updated_at` (timestamptz, nullable)
- `parent_pattern_id` (uuid / text, FK → patterns.id, nullable) — если не null, это **сегмент** другого паттерна
- `segment_index` (int, nullable)
- `segment_start_ms` (int, nullable)
- `segment_end_ms` (int, nullable)

**Индексы (минимум):**
- по (`owner_id`)
- по (`public`, `hardware_version`, `kind`)
- по (`parent_pattern_id`) для выборки сегментов

> **Сегменты** реализуются как записи в этой же таблице, где `parent_pattern_id = <id родительского паттерна>`.

### 2.2. Таблица `tags`

- `id` (uuid / text, PK)
- `name` (text, unique, not null)

### 2.3. Таблица `pattern_tags`

Связь многие-ко-многим `pattern` ↔ `tag`.

- `pattern_id` (uuid / text, PK часть, FK → patterns.id)
- `tag_id` (uuid / text, PK часть, FK → tags.id)

Индексы:
- по (`tag_id`)

### 2.4. Таблица `pattern_shares`

Шаринг паттернов с другими пользователями.

- `pattern_id` (uuid / text, PK часть, FK → patterns.id)
- `user_id` (uuid / text, PK часть, FK → profiles.id)

### 2.5. Таблица `pattern_markers`

- `pattern_id` (uuid / text, PK, FK → patterns.id, unique)
- `markers_ms` (jsonb, not null) — массив `int` (маркеры таймлайна в миллисекундах)

В коде:
- DTO: `PatternMarkersDto(patternId: String, markersMs: List<Int>)`
- локально: `PatternMarkersEntity(patternId, markersJson)`

На Supabase логичнее хранить сразу как `jsonb` массив чисел.

---

## 3. Пары, эмоции и quick replies

Соответствие моделям:
- домен: `Pair`, `PairMember`, `PairMemberSettings`, `PairEmotion`, `PairQuickReply`, `PairStatus`
- DTO: `PairDto`, `PairEmotionDto`, `PairMemberSettingsDto`, `PairQuickReplyDto`
- локальная БД: `PairEntity`, `PairMemberEntity`

### 3.1. Таблица `pairs`

- `id` (uuid / text, PK)
- `status` (text, not null) — строки, соответствующие доменному `PairStatus` (`ACTIVE`, `PENDING`, `BLOCKED`)
- `blocked_by` (uuid / text, FK → profiles.id, nullable)
- `blocked_at` (timestamptz, nullable)
- `created_at` (timestamptz, not null)

Индексы:
- по (`status`)
- по (`created_at` desc)
- по (`blocked_by`)

### 3.2. Таблица `pair_members`

- `pair_id` (uuid / text, PK часть, FK → pairs.id)
- `user_id` (uuid / text, PK часть, FK → profiles.id)
- `joined_at` (timestamptz, not null)
- `muted` (boolean, not null, default false)
- `quiet_hours_start_minutes` (int, nullable)
- `quiet_hours_end_minutes` (int, nullable)
- `max_hugs_per_hour` (int, nullable)

Это соответствует `PairMemberEntity` и `PairMemberSettingsDto`.

### 3.3. Таблица `pair_emotions`

- `id` (uuid / text, PK)
- `pair_id` (uuid / text, FK → pairs.id, not null)
- `name` (text, not null)
- `color_hex` (text, not null)
- `pattern_id` (uuid / text, FK → patterns.id, nullable)
- `order` (int, not null)

Соответствует `PairEmotionDto` и доменной `PairEmotion`.

### 3.4. Таблица `pair_quick_replies`

- `pair_id` (uuid / text, PK часть, FK → pairs.id)
- `user_id` (uuid / text, PK часть, FK → profiles.id)
- `gesture_type` (text, PK часть) — строка `"DOUBLE_TAP"` / `"LONG_PRESS"`
- `emotion_id` (uuid / text, FK → pair_emotions.id, **nullable**)

---

## 4. Объятия (hugs)

Соответствие моделям:
- домен: `Hug`, `HugStatus`, `Emotion`
- DTO: `HugDto`, `HugEmotionDto`, `HugSendRequestDto`, `HugStatusUpdateRequestDto`
- локальная БД: `HugEntity`

### 4.1. Таблица `hugs`

- `id` (uuid / text, PK)
- `from_user_id` (uuid / text, FK → profiles.id, nullable)
- `to_user_id` (uuid / text, FK → profiles.id, nullable)
- `pair_id` (uuid / text, FK → pairs.id, nullable)
- `emotion_color` (text, nullable) — ARGB/HEX цвет эмоции
- `emotion_pattern_id` (uuid / text, FK → patterns.id, nullable)
- `payload` (jsonb, nullable) — произвольный payload (соответствует `JsonObject` в DTO)
- `in_reply_to_hug_id` (uuid / text, FK → hugs.id, nullable)
- `status` (text, not null, default `"SENT"`) — значения доменного `HugStatus` (`SENT`, `DELIVERED`, `READ`, `EXPIRED`)
- `delivered_at` (timestamptz, nullable)
- `created_at` (timestamptz, not null)

Индексы (по аналогии с Room):
- по (`created_at` desc)
- по (`from_user_id`, `created_at`)
- по (`to_user_id`, `created_at`)
- по (`pair_id`, `created_at`)
- по (`emotion_pattern_id`)
- по (`status`)

> Для API можно использовать DTO из `HugDtos.kt` без изменений — они согласуются с доменными моделями.

---

## 5. Push-уведомления OneSignal

OneSignal не требует отдельных таблиц в Supabase, но важно:

1. **Push-пейлоад для входящего объятия** (используется в `PushNotificationRouter`):

   ```json
   {
     "type": "hug",
     "hugId": "<uuid>",
     "pairId": "<uuid>",
     "fromUserId": "<uuid>",
     "toUserId": "<uuid>",
     "patternId": "<uuid>",
     "emotionColorHex": "#FF6699"
   }
   ```

   - `type` = `"hug"` — роутер выбирает обработку объекта объятия.
   - `fromUserId`, `toUserId`, `pairId`, `hugId`, `patternId` — обязательны для корректной работы клиентской логики.
   - `emotionColorHex` — опционален (цвет эмоции).

2. **Отправка push с бэкенда**

   Минимально бэкенд должен уметь:

   - при создании нового `hug` (запись в таблицу `hugs`) определить получателя (`to_user_id`) и его OneSignal player id;
   - сформировать описанный выше data‑payload;
   - вызвать OneSignal REST API (из edge function / serverless‑функции Supabase) для отправки уведомления.

   Для этого может понадобиться дополнительная таблица, например `push_tokens`:

   ### 5.1. Таблица `push_tokens`

   - `user_id` (uuid / text, PK часть, FK → profiles.id)
   - `device_id` (text, PK часть) — идентификатор устройства/клиента
   - `onesignal_player_id` (text, not null)
   - `created_at` (timestamptz)

   Клиент уже имеет use case `SyncPushTokenUseCase`, так что бэкенд должен принимать и сохранять player id OneSignal в эту таблицу.

---

## 6. Сводка по актуальности DTO

- **Hug DTOs (`HugDtos.kt`)** — соответствуют доменному уровню и локальной схеме `hugs`. Могут использоваться без изменений.
- **Pair DTOs (`PairDtos.kt`)**:
  - `PairDto`, `PairEmotionDto`, `PairMemberSettingsDto` согласуются с доменными моделями и локальными сущностями.
  - `PairQuickReplyDto.emotionId` теперь `String?` и соответствует доменной модели `PairQuickReply.emotionId: String?` и nullable полю `emotion_id` в таблице `pair_quick_replies`.
- **Pattern DTOs (`PatternDtos.kt`)** — согласуются с локальными сущностями `PatternEntity` и `PatternMarkersEntity`: поля `parentPatternId`, `segmentIndex`, `segmentStartMs`, `segmentEndMs` и `PatternMarkersDto` уже учтены в коде клиента.

В остальном заметных расхождений между DTO и актуальными доменными моделями/Room-схемой нет: Supabase‑бэкенд можно проектировать по описанным выше таблицам.

---

## 7. Минимальное HTTP API (совместимое с текущим клиентом)

Ниже — ориентир по endpoint'ам и payload, сопоставленный с DTO в `core/network/service/*`.

### 7.1. Auth / профиль
- Использовать Supabase Auth (`auth.users`), клиент — через Supabase SDK.
- При необходимости отдельный `GET /profile` для дополнительных полей из `profiles`.

### 7.2. Паттерны
- `GET /patterns/{id}` → `PatternResponseDto`.
- `GET /patterns` (публичные/фильтр) → `PatternListResponseDto` (параметры: `public`, `hardwareVersion`, `kind`, `tags`).
- `POST /patterns` → `PatternCreateRequestDto` → `PatternResponseDto`.
- `PUT /patterns/{id}` → `PatternUpdateRequestDto` → `PatternResponseDto`.
- `DELETE /patterns/{id}`.
- `GET /patterns/{id}/segments` → `PatternListResponseDto` (items — сегменты с `parentPatternId = id`).
- `PUT /patterns/{id}/segments` → тело `PatternListResponseDto` (items = сегменты) → `PatternListResponseDto`.
- `GET /patterns/{id}/markers` → `PatternMarkersDto?`.
- `PUT /patterns/{id}/markers` → `PatternMarkersDto` → `PatternMarkersDto`.
- Теги/шаринг: по необходимости `POST /patterns/{id}/share`, `GET/PUT /patterns/{id}/tags` (согласовать по MVP).

### 7.3. Пары и эмоции
- `GET /pairs` → `PairListResponseDto`.
- `POST /pairs/invite` → `PairInviteRequestDto` → `PairInviteResponseDto`.
- `POST /pairs/accept` → `PairAcceptRequestDto` → `PairResponseDto`.
- `POST /pairs/{id}/block` → `PairResponseDto`; `POST /pairs/{id}/unblock` → `PairResponseDto`.
- Эмоции пары:
  - `GET /pairs/{id}/emotions` → `PairEmotionListResponseDto`.
  - `PUT /pairs/{id}/emotions` → `PairEmotionUpdateRequestDto` → `PairEmotionListResponseDto`.
- Настройки участника пары:
  - `PUT /pairs/{id}/members/{userId}/settings` → `PairMemberSettingsUpdateRequestDto` → `PairResponseDto`.
- Quick replies:
  - `GET /pairs/{id}/quick-replies?userId=...` → `PairQuickReplyListResponseDto`.
  - `PUT /pairs/{id}/quick-replies` → `PairQuickReplyUpdateRequestDto` → `PairQuickReplyListResponseDto`.

### 7.4. Объятия (hugs)
- `POST /hugs` → `HugSendRequestDto` → `HugSendResponseDto` (создаёт запись в `hugs` и триггерит push).
- `GET /hugs` → `HugListResponseDto` (поддержка пагинации cursor).
- `GET /hugs/{id}` → `HugResponseDto`.
- `PATCH /hugs/{id}/status` → `HugStatusUpdateRequestDto`.
- При создании нового hug сервер отправляет push (см. секцию 5) получателю (`toUserId`) с data‑payload.

### 7.5. Push токены (OneSignal)
- `POST /push-tokens` — тело: `{ userId, deviceId, onesignalPlayerId }` (соответствует `SyncPushTokenUseCase`); upsert по `(userId, deviceId)`.
- По желанию — `DELETE /push-tokens` для отписки устройства.

### 7.6. Интеграция push
- Бэкенд отправляет OneSignal REST `notifications` с `data` из п.5; `contents` можно держать минимальным (клиент сам обрабатывает data).
- Для rate-limit: на бэкенде проверять `PairMemberSettings` (mute/quiet hours) перед отправкой push.

---
