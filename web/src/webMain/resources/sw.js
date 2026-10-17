// The revision the page was built from, put here when the site is deployed: the browser only
// looks for a new service worker when sw.js itself has changed, so a deploy has to change it
// to be noticed. A new worker means a new cache, and `activate` drops the one before it. An
// ordinary build leaves it at 'dev', where the name of the cache does not matter.
const BUILD = 'dev';

const CACHE = 'calendar-converter-' + BUILD;

const PRECACHE = [
  './',
  './index.html',
  './main.js',
  './web.js',
  './manifest.webmanifest',
  './icons/icon-192.png',
  './icons/icon-512.png',
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE).then((cache) => cache.addAll(PRECACHE)).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((key) => key !== CACHE).map((key) => caches.delete(key))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  event.respondWith(
    caches.match(event.request).then((cached) => {
      if (cached) return cached;
      return fetch(event.request).then((response) => {
        if (response && response.status === 200) {
          const copy = response.clone();
          caches.open(CACHE).then((cache) => cache.put(event.request, copy));
        }
        return response;
      });
    })
  );
});
