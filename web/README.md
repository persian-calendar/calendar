# Calendar Web

A tiny HTML demo of this calendar library.

https://persian-calendar.github.io/calendar/

## Links to a date

The address bar always holds what the page is showing, so copying it gives a link to that
date, and opening one starts there:

```
#date=2025-04-04&calendar=gregorian
#date=1304-04-04&calendar=islamic
#date=1404-04-04&calendar=persian
```

It is a fragment rather than a query string, so the address is never sent to the server:
a link says where it came from to nobody.

`calendar` takes the ICU ids — `gregorian`, `persian` and `islamic` — which are also the
values of the selector; the library knows the first one as `civil`, and the page maps
between the two. `date` is `year-month-day` in the named calendar's own year, with the year
allowed to be negative. Month and day are written padded, and `1404-4-4` is understood just
as well as `1404-04-04`.

A date the named calendar does not have — a thirteenth month, a thirty-first day of a
thirty-day month, a year beyond what the page accepts — is shown as an error rather than
corrected into a neighbouring date, and it is never handed to the library. The address bar
keeps the link as it arrived until the reader changes a field, so reloading shows the same
error.

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
of a cached page running against a newer script.

```sh
./gradlew :web:webStandalone
```

The task leaves the normal distribution alone, so `jsBrowserDistribution` still produces the
separate files for development. Two things are adjusted in the copy: the source map comments
go, since the maps are no longer files of their own, and `main.js` and `web.js` are dropped
from the service worker's precache list, which no longer needs them.

Only the JS target is handled. The Wasm build's glue loads a `.wasm` file of its own, and that
has to stay a separate request, while the JS bundle is self-contained.

The service worker is stamped with the commit at deploy time, not by this task: the
browser only looks for a new worker when `sw.js` has changed, so putting the revision inside
it is what turns a deploy into a new cache and the end of the old one. A local build leaves it
at `'dev'`, where the name of the cache does not matter.
