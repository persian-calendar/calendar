// Fetches https://calendar.ut.ac.ir/ and prints the rendered HTML to stdout.
//
// The site sits behind an ArvanCloud JavaScript challenge. That challenge only
// computes two cookies (__arcsjs / __arcsjsc) from obfuscated inline JS and
// then reloads the page. We solve that locally instead of running a headless
// browser, which keeps the GitHub Actions job fast and dependency-free.

const URL = 'https://calendar.ut.ac.ir/';

const USER_AGENT =
  'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 ' +
  '(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36';

const MAX_ATTEMPTS = 3;

// The challenge XORs each character with 6, twice (E(E(value))).
function xorEncode(s, key = 6) {
  let out = '';
  for (let i = 0; i < s.length; i += 1) {
    out += String.fromCharCode(key ^ s.charCodeAt(i));
  }
  return out;
}

function isChallengePage(html) {
  return (
    html.includes('__arcsjs') ||
    html.includes('Transferring to the website') ||
    html.includes('__arcsjsc')
  );
}

function solveChallenge(html) {
  const exprs = [];
  const re = /eval\("((?:[^"\\]|\\.)*)"\)/g;
  let match;
  while ((match = re.exec(html)) !== null) {
    exprs.push(match[1]);
  }
  if (exprs.length < 2) {
    return null;
  }

  const valueV1 = (0, eval)(exprs[0]);
  const value = (0, eval)(exprs[1]);
  const hashV1 = xorEncode(xorEncode(valueV1));
  const hash = xorEncode(xorEncode(value));

  return `__arcsjs=${encodeURIComponent(hashV1)}; __arcsjsc=${encodeURIComponent(hash)}`;
}

async function fetchPage(cookie) {
  const headers = { 'user-agent': USER_AGENT };
  if (cookie) {
    headers.cookie = cookie;
  }
  const res = await fetch(URL, { headers, redirect: 'follow' });
  return res.text();
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function main() {
  let cookie = '';

  for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt += 1) {
    const body = await fetchPage(cookie);

    if (!isChallengePage(body)) {
      process.stdout.write(body);
      return;
    }

    cookie = solveChallenge(body);
    if (!cookie) {
      break;
    }

    // The challenge page reloads itself after 2-3s; a short pause keeps us
    // from hammering the edge before its cookie is accepted.
    await sleep(1000);
  }

  process.stderr.write('Could not retrieve the calendar page content.\n');
  process.exit(1);
}

main().catch((error) => {
  process.stderr.write(`${error && error.stack ? error.stack : error}\n`);
  process.exit(1);
});
