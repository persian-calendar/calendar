# Calendar Web

A tiny HTML demo of this calendar library.

https://persian-calendar.github.io/calendar/

## Run locally

From the repository root:

```sh
# JS backend (faster to build)
./gradlew :web:jsBrowserDevelopmentRun

# Wasm backend
./gradlew :web:wasmJsBrowserDevelopmentRun
```

Then open http://localhost:8080/.

## Build a static bundle

```sh
# JS bundle  -> web/build/dist/js/productionExecutable/
./gradlew :web:jsBrowserDistribution

# Wasm bundle -> web/build/dist/wasmJs/productionExecutable/
./gradlew :web:wasmJsBrowserDistribution
```
