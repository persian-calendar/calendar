// Fetches https://calendar.ut.ac.ir/ and prints the rendered HTML to stdout.
//
// The site sits behind an ArvanCloud JavaScript challenge. That challenge only
// computes two cookies (__arcsjs / __arcsjsc) from obfuscated inline JS and
// then reloads the page. We solve that locally instead of running a headless
// browser, which keeps the GitHub Actions job fast.
//
// The challenge code is untrusted. Each expression is evaluated in a separate
// Deno subprocess (`deno run --no-config --no-prompt -`). `deno run` denies all
// permissions by default and `--no-prompt` turns the interactive permission
// prompt into an automatic denial, so the code has no network, filesystem,
// environment or subprocess access. The process is also killed if it exceeds
// the timeout. It is never executed with `eval` in the main process.

import { spawnSync } from 'node:child_process';

const URL = 'https://calendar.ut.ac.ir/';

const USER_AGENT =
  'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 ' +
  '(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36';

const MAX_ATTEMPTS = 3;

// Timeout for each untrusted Deno subprocess.
const EVAL_TIMEOUT_MS = 1000;

// Timeout for each HTTP request, including reading the response body. Node's
// fetch has no default timeout, so without this a stalled connection would
// hang the script forever.
const FETCH_TIMEOUT_MS = 30_000;

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

// Evaluates one untrusted challenge expression in a separate Deno process.
// `deno run` denies all permissions by default, `--no-prompt` makes it deny
// rather than interactively asking, and `--no-config` ignores any stray
// deno.json. The process is also killed if it runs past the timeout.
function evaluateChallengeExpr(expr) {
  // Embed the expression as a JSON string literal, then eval it in Deno's
  // global scope and print the result. JSON.stringify makes the embedding safe
  // regardless of quotes or backslashes in the expression.
  const code = `console.log(String((0, eval)(${JSON.stringify(expr)})));`;
  const result = spawnSync('deno', ['run', '--no-config', '--no-prompt', '-'], {
    input: code,
    timeout: EVAL_TIMEOUT_MS,
    encoding: 'utf8',
  });

  if (result.error) {
    throw result.error;
  }
  if (result.status !== 0) {
    throw new Error(
      `deno run failed (status ${result.status}): ${result.stderr}`,
    );
  }

  const value = result.stdout.trimEnd();
  if (!value) {
    throw new Error('deno run produced no output');
  }
  return value;
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
    values = exprs.slice(0, 2).map(evaluateChallengeExpr);
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
  const res = await fetch(URL, {
    headers,
    redirect: 'follow',
    signal: AbortSignal.timeout(FETCH_TIMEOUT_MS),
  });
  return res.text();
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function main() {
  let cookie = '';

  for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt += 1) {
    let body;
    try {
      body = await fetchPage(cookie);
    } catch (error) {
      process.stderr.write(
        `Fetch attempt ${attempt + 1} failed: ${
          error && error.message ? error.message : error
        }\n`,
      );
      await sleep(1000);
      continue;
    }

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
