// Fetches https://calendar.ut.ac.ir/ and prints the rendered HTML to stdout.
//
// The site sits behind an ArvanCloud JavaScript challenge. That challenge only
// computes two cookies (__arcsjs / __arcsjsc) from obfuscated inline JS and
// then reloads the page. We solve that locally instead of running a headless
// browser, which keeps the GitHub Actions job fast.
//
// The challenge code is untrusted. It is evaluated inside an isolated V8
// context (isolated-vm) with no access to process, filesystem, network or any
// other Node.js host API, and is bounded by time and memory limits. It is never
// executed with `eval` in the main process.

import ivm from 'isolated-vm';

const URL = 'https://calendar.ut.ac.ir/';

const USER_AGENT =
  'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 ' +
  '(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36';

const MAX_ATTEMPTS = 3;

// Limits for the sandbox in which the untrusted challenge code runs.
const EVAL_TIMEOUT_MS = 1000;
const EVAL_MEMORY_MB = 16;

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

// Evaluates the untrusted challenge expressions inside an isolated V8 context.
// The isolate has none of Node.js's host globals (process, Buffer, fetch, ...),
// no filesystem, network or subprocess access, and is bounded by time and
// memory limits. Each expression must evaluate to a plain string, which is what
// the challenge cookies are built from.
async function evaluateChallenge(exprs) {
  const isolate = new ivm.Isolate({ memoryLimit: EVAL_MEMORY_MB });
  try {
    const context = await isolate.createContext();
    try {
      const values = [];
      for (const expr of exprs) {
        const value = await context.eval(expr, { timeout: EVAL_TIMEOUT_MS });
        if (typeof value !== 'string') {
          throw new Error(
            `Challenge expression did not evaluate to a string (got ${typeof value})`,
          );
        }
        values.push(value);
      }
      return values;
    } finally {
      context.release();
    }
  } finally {
    isolate.dispose();
  }
}

async function solveChallenge(html) {
  const exprs = [];
  const re = /eval\("((?:[^"\\]|\\.)*)"\)/g;
  let match;
  while ((match = re.exec(html)) !== null) {
    exprs.push(match[1]);
  }
  if (exprs.length < 2) {
    return null;
  }

  let values;
  try {
    values = await evaluateChallenge(exprs.slice(0, 2));
  } catch (error) {
    process.stderr.write(
      `Failed to evaluate the challenge in the sandbox: ${
        error && error.message ? error.message : error
      }\n`,
    );
    return null;
  }

  const [valueV1, value] = values;
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

    cookie = await solveChallenge(body);
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
