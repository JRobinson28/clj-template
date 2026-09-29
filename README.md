# clj-template

Minimal Clojure service template: [Integrant](https://github.com/weavejester/integrant) system, [Aero](https://github.com/juxt/aero) config, Ring/Jetty and [Telemere](https://github.com/taoensso/telemere) logging.

## Setup

Tools are pinned in `mise.toml`:

```sh
mise install
```

## Tasks

| Task                  | Description                               |
|-----------------------|-------------------------------------------|
| `mise run dev`        | Start the system (`:dev` profile) + nREPL |
| `mise run run`        | Run the app (`:prod` profile)             |
| `mise run test`       | Run tests                                 |
| `mise run lint`       | Lint with clj-kondo                       |
| `mise run fmt`        | Format with cljfmt                        |
| `mise run fmt:check`  | Check formatting                          |
| `mise run build`      | Build `target/standalone.jar`             |
| `mise run outdated`       | List outdated dependencies                |
| `mise run outdated:upgrade` | Upgrade outdated dependencies           |
| `mise run ci`         | lint + fmt:check + test                   |
| `mise run rename <name>` | Rename the project (see below)         |

## REPL

Start a REPL with the `:dev` alias (or connect to `mise run dev`), then in `user`:

```clojure
(go)     ; start the system
(reset)  ; reload changed namespaces and restart
(halt)   ; stop the system
```

## Config

`resources/config.edn` is read by `clj-template.system/read-config`; the `:ig/system` key is the Integrant config. The HTTP port defaults to 2800 and can be overridden with `PORT`.

## Docker

```sh
docker compose up --build
```

## Using the template

Rename `clj-template` / `clj_template` throughout (namespaces, directories, `build.clj`, `config.edn`) with:

```sh
mise run rename my-app       # or a qualified name, e.g. acme.my-app
```

The [Babashka](https://babashka.org) script `scripts/rename.clj` rewrites tracked files, moves the source directories and removes this section.
