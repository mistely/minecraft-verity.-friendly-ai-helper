# Verity AI Friend — добрый помощник (Forge 1.16.5)

Тёплый голосовой компаньон для Minecraft. Зажми клавишу (по умолчанию **X**), говори в микрофон,
отпусти — Verity распознаёт речь и отвечает настоящим LLM **на русском**, по-доброму помогает по игре.
Никакого хоррора и скримеров. Ответ выводится в чат, опционально озвучивается.

Это **самостоятельный мод**. Оригинальный хоррор-мод Verity ему НЕ нужен.
Работает на твоей сборке **ForgeOptiFine 1.16.5**.

## Что тебе нужно сделать (минимум)
1. Получить **бесплатный** ключ API на console.groq.com (регистрация ~1 минута). Это единственное,
   что я не могу сделать за тебя — ключ привязан к твоему аккаунту.
2. Получить готовый `.jar` (см. «Сборка в облаке» — собирается сам, без установки чего-либо у тебя).
3. Положить `.jar` в `mods`, один раз запустить игру, вписать ключ в конфиг, запустить снова. Готово.

## Сборка в облаке (GitHub Actions) — рекомендую
Компиляция Forge-мода тянет Minecraft/Forge с серверов, поэтому собирается на GitHub бесплатно:
1. Создай аккаунт на github.com (если нет) и новый **репозиторий** (можно private).
2. Загрузи в него содержимое этой папки (кнопка `Add file → Upload files`, перетащи всё, включая папку `.github`).
3. Вкладка **Actions** → workflow `build` запустится сам (или нажми `Run workflow`).
4. Дождись зелёной галочки (~3-6 мин первый раз), открой запуск → внизу **Artifacts** → скачай
   `verity-ai-friend-jar`. Внутри `verity_ai-1.0.0.jar`.

Workflow уже лежит в `.github/workflows/build.yml`, ничего настраивать не надо.

## Сборка локально (если хочешь сам)
Нужен JDK 8. Возьми официальный **Forge 1.16.5 MDK** (forge 36.2.42), скопируй в него `src/` и `mods.toml`,
в `build.gradle` укажи `mappings channel: 'snapshot', version: '20210309-1.16.5'`, затем `./gradlew build`.
Либо используй приложенные `build.gradle` / `settings.gradle` / `gradle.properties` как есть.

> Код написан под **MCP-маппинги (snapshot)**. Если соберёшь с `official`-маппингами — не скомпилируется,
> см. «Если не компилится» внизу.

## Установка в игру
1. `verity_ai-1.0.0.jar` → папка `mods` твоей сборки 1.16.5 (рядом с Forge/OptiFine).
2. Запусти игру один раз → закрой → открой `config/verity_ai.properties` → впиши `api_key` → запусти снова.

## Конфиг (создаётся сам при первом запуске)
По умолчанию уже прописан бесплатный Groq — нужно только вставить ключ:

```
enabled=true
base_url=https://api.groq.com/openai/v1
api_key=ВСТАВЬ_КЛЮЧ_С_console.groq.com
chat_model=llama-3.3-70b-versatile
stt_model=whisper-large-v3-turbo
language=ru
temperature=0.8
tts=false                            # true = озвучка через Windows SAPI (нужен русский голос в системе)
persona=                             # пусто = добрый помощник; впиши свой текст, чтобы изменить характер
```

Полностью оффлайн (без ключа и интернета): подними whisper.cpp server (STT) и Ollama/LM Studio (LLM),
поставь `base_url` на них.

> Имена моделей у Groq иногда меняются. Если в чат прилетит ошибка вида «model ... not found» —
> зайди в консоль Groq, посмотри доступные модели и поправь `chat_model` (например, `llama-3.1-8b-instant`
> — быстрее) или `stt_model`.

## Как разговаривать и играть с Verity
- Настройки → Управление → категория **Verity** — при желании перевесь клавишу с X.
- Зажми клавишу, скажи («что мне скрафтить?», «где искать алмазы?», «помоги с домом»), отпусти.
  Под прицелом мелькнёт «🎤 Verity слушает...», потом в чат придёт твой текст и тёплый ответ.
- Это твой спутник по игре: спрашивай совета, рассказывай, что строишь, проси идеи.

## Если не компилится (official-маппинги вместо snapshot)
Правки в основном в `PushToTalk.java`:
- `net.minecraft.client.settings.KeyBinding` → `net.minecraft.client.KeyMapping`
- `KeyBinding.isKeyDown()` → `isDown()`
- `mc.currentScreen` → `mc.screen`
- `player.sendStatusMessage(comp, true)` → `player.displayClientMessage(comp, true)`
- `player.sendMessage(comp, Util.DUMMY_UUID)` → `player.displayClientMessage(comp, false)`
- `StringTextComponent` → `TextComponent`

Проще собрать с `snapshot`-маппингами, тогда менять ничего не надо.
