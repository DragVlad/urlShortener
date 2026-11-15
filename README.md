# urlShortener

## Сборка проекта
Консольное Java-приложение для создания коротких ссылок с лимитом переходов, сроком жизни и управлением доступом по владельцу.
Приложенние реализованно на `Java 21`.

- Клонирование проекта:
```bash
git clone git@github.com:DragVlad/urlShortener.git
```

- Проверка необходимых компонентов `Java`:
```bash
java -version
# Ожидаем
openjdk version "21.0.13.0.3" 2026-10-24
OpenJDK Runtime Environment (build 21.0.13.0.3+1-1-26.04.1-Ubuntu)
OpenJDK 64-Bit Server VM (build 21.0.13.0.3+1-1-26.04.1-Ubuntu, mixed mode, sharing)

javac -version
# Ожидаем
javac 21.0.13.0.3

mvn -version
# Ожидаем
Apache Maven 3.9.12
Maven home: /usr/share/maven
Java version: 21.0.13.0.3, vendor: Ubuntu, runtime: /usr/lib/jvm/java-21-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.18.58.1-microsoft-standard-wsl2", arch: "amd64", family: "unix"
```

- Локальная сборка проекта:
```bash
cd urlShortener/app/
mvn clean package
# Ожидаем
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.069 s
[INFO] Finished at: 2026-11-16T00:19:55+03:00
[INFO] ------------------------------------------------------------------------
```
## Тестирование проекта

- После успешной сборки запускаем `Main.java`:
```bash
Сервис сокращения ссылок. Введите help для списка команд.
> help
Доступные команды:
  create <url> [limit]      - сократить ссылку
  open <code>               - открыть короткую ссылку
  edit_limit <code> <limit> - изменить лимит переходов
  delete <code>             - удалить ссылку
  list                      - список ваших ссылок
  help                      - показать эту справку
  exit                      - выход
> create https://google.com
Создан новый пользователь. Ваш UUID: 7de54a40-6ee8-469b-bfa9-29e29a97228d
Короткая ссылка: clck.ru/gYR1b (лимит: 10)
> open clck.ru/gYR1b
> list
Ваши ссылки:
  clck.ru/gYR1b | https://google.com | статус: ACTIVE | переходы: 1/10 | истекает: 2026-11-15T22:31:52.465317Z
> edit_limit clck.ru/gYR1b 25
Лимит обновлён: 25
> list
Ваши ссылки:
  clck.ru/gYR1b | https://google.com | статус: ACTIVE | переходы: 1/25 | истекает: 2026-11-15T22:31:52.465317Z
> delete clck.ru/gYR1b 25
Ссылка удалена.
> list
Ваши ссылки:
  clck.ru/gYR1b | https://google.com | статус: DELETED | переходы: 1/25 | истекает: 2026-11-15T22:31:52.465317Z
```