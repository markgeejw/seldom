(() => {
  if (window.__seldomInput) return;
  window.__seldomInput = true;

  const held = new Set();
  const track = (e) => {
    if (e.code !== 'MetaLeft' && e.code !== 'MetaRight') return;
    if (e.type === 'keydown') held.add(e.code); else held.delete(e.code);
  };
  window.addEventListener('keydown', track, true);
  window.addEventListener('keyup', track, true);
  const clear = () => held.clear();
  window.addEventListener('blur', clear, true);
  document.addEventListener('visibilitychange', () => { if (document.hidden) clear(); }, true);
  const isMeta = (e) => e.code === 'MetaLeft' || e.code === 'MetaRight';
  const metaDown = (e) => {
    if (e instanceof KeyboardEvent && isMeta(e)) return e.type === 'keydown';
    return held.size > 0;
  };
  for (const proto of [KeyboardEvent.prototype, MouseEvent.prototype]) {
    const desc = Object.getOwnPropertyDescriptor(proto, 'metaKey');
    if (desc && desc.get) {
      Object.defineProperty(proto, 'metaKey', {
        configurable: true, enumerable: desc.enumerable,
        get() { return desc.get.call(this) || metaDown(this); },
      });
    }
    const original = proto.getModifierState;
    if (original) {
      proto.getModifierState = function (key) {
        if (key === 'Meta' || key === 'OS') return original.call(this, key) || metaDown(this);
        return original.call(this, key);
      };
    }
  }

  let lastTrusted = 0;
  window.addEventListener('mousemove', (e) => { if (e.isTrusted) lastTrusted = performance.now(); }, true);
  window.__seldomHover = (px, py) => {
    if (performance.now() - lastTrusted < 250) return;
    const x = px / window.devicePixelRatio, y = py / window.devicePixelRatio;
    const target = document.elementFromPoint(x, y) || document.body;
    const init = { bubbles: true, cancelable: true, composed: true, clientX: x, clientY: y, screenX: x, screenY: y, buttons: 0, view: window };
    target.dispatchEvent(new PointerEvent('pointermove', { ...init, pointerType: 'mouse', pointerId: 1, isPrimary: true }));
    target.dispatchEvent(new MouseEvent('mousemove', init));
  };
})();
