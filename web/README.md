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

## A single file build

`webStandalone` copies the JS distribution to `web/build/dist/js/standalone/` with `main.js`
and `web.js` written into `index.html` instead of being loaded as separate files. Publishing
that directory means publishing one document, with no separate script requests and no chance
of a cached page running against a newer script:

```sh
./gradlew :web:webStandalone
```

The task leaves the normal distribution alone, so `jsBrowserDistribution` still produces the
separate files for development. Two things are adjusted in the copy: the source map comments
go, since the maps are no longer files of their own, and `main.js` and `web.js` are dropped
from the service worker's precache list, which no longer needs them.

Only the JS target is handled. The Wasm build's glue loads a `.wasm` file of its own, and that
has to stay a separate request, while the JS bundle is self-contained.
