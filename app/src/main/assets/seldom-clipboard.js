(() => {
  if (window.__seldomClipboard || !window.SeldomClipboard || !navigator.clipboard) return;
  window.__seldomClipboard = true;

  const bridge = window.SeldomClipboard;
  const pending = new Map();
  let nextId = 1;
  bridge.onmessage = (event) => {
    let reply;
    try { reply = JSON.parse(event.data); } catch (_) { return; }
    const done = pending.get(reply.id);
    if (done) done(typeof reply.text === 'string' ? reply.text : '');
  };
  const readPhoneText = () => new Promise((resolve) => {
    const id = nextId++;
    const finish = (text) => {
      if (!pending.delete(id)) return;
      clearTimeout(timer);
      resolve(text);
    };
    const timer = setTimeout(() => finish(''), 2000);
    pending.set(id, finish);
    bridge.postMessage(JSON.stringify({ id, op: 'readText' }));
  });

  navigator.clipboard.readText = () => readPhoneText();
  navigator.clipboard.read = async () => {
    const text = await readPhoneText();
    if (!text) return [];
    return [new ClipboardItem({ 'text/plain': new Blob([text], { type: 'text/plain' }) })];
  };

  if (navigator.permissions && navigator.permissions.query) {
    const query = navigator.permissions.query.bind(navigator.permissions);
    navigator.permissions.query = (descriptor) => {
      const name = descriptor && descriptor.name;
      if (name === 'clipboard-read' || name === 'clipboard-write') {
        return Promise.resolve({
          state: 'granted', name, onchange: null,
          addEventListener() {}, removeEventListener() {},
        });
      }
      return query(descriptor);
    };
  }
})();
