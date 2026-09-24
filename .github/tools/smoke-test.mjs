// Smoke test for the isolated-vm sandbox used by fetch-calendar.mjs.
//
// Fails fast when:
//   * isolated-vm cannot load on the pinned Node version (ABI mismatch after a
//     dependency bump), or
//   * the sandbox stops isolating the Node.js host (process/fetch leak).

import ivm from 'isolated-vm';

const isolate = new ivm.Isolate({ memoryLimit: 16 });
try {
  const context = await isolate.createContext();
  try {
    const value = await context.eval('String(40 + 2)', { timeout: 1000 });
    if (value !== '42') {
      throw new Error(`Unexpected sandbox result: ${value}`);
    }

    for (const globalName of ['process', 'fetch', 'require', 'Buffer']) {
      const type = await context.eval(`typeof ${globalName}`, { timeout: 1000 });
      if (type !== 'undefined') {
        throw new Error(`Sandbox leaked host global: ${globalName}`);
      }
    }

    let escaped = false;
    try {
      await context.eval(
        'this.constructor.constructor("return process")().version',
        { timeout: 1000 },
      );
      escaped = true;
    } catch {
      // Expected: the escape attempt throws inside the isolate.
    }
    if (escaped) {
      throw new Error('Sandbox escape attempt unexpectedly succeeded');
    }
  } finally {
    context.release();
  }
} finally {
  isolate.dispose();
}

console.log('isolated-vm sandbox OK');
