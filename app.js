/* ══════════════════════════════════════════
   NAV SCROLL STATE
══════════════════════════════════════════ */
(function () {
  const nav = document.getElementById('nav');
  if (!nav) return;
  window.addEventListener('scroll', () =>
    nav.classList.toggle('scrolled', window.scrollY > 30),
  { passive: true });
})();

/* ══════════════════════════════════════════
   HAMBURGER MENU
══════════════════════════════════════════ */
(function () {
  const btn    = document.getElementById('hamburger');
  const drawer = document.getElementById('mobileDrawer');
  if (!btn || !drawer) return;

  function open() {
    btn.classList.add('open');
    btn.setAttribute('aria-expanded', 'true');
    drawer.classList.add('open');
    drawer.removeAttribute('aria-hidden');
    document.body.style.overflow = 'hidden';
  }

  function close() {
    btn.classList.remove('open');
    btn.setAttribute('aria-expanded', 'false');
    drawer.classList.remove('open');
    drawer.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }

  btn.addEventListener('click', () =>
    drawer.classList.contains('open') ? close() : open()
  );

  // Close on any drawer link click
  drawer.querySelectorAll('[data-close], .drawer-link, .drawer-cta').forEach(el =>
    el.addEventListener('click', close)
  );

  // Close on outside click
  document.addEventListener('click', e => {
    const nav = document.getElementById('nav');
    if (nav && !nav.contains(e.target)) close();
  });

  // Close on Escape
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') close();
  });

  // Close on resize back to desktop
  window.addEventListener('resize', () => {
    if (window.innerWidth > 768) close();
  }, { passive: true });
})();

/* ══════════════════════════════════════════
   MOBILE THEME TOGGLE (mirrors desktop)
══════════════════════════════════════════ */
(function () {
  const mobBtn = document.getElementById('themeToggleMob');
  if (!mobBtn) return;

  function syncIcons(theme) {
    mobBtn.querySelectorAll('.icon-sun, .icon-moon').forEach(ic => {
      ic.style.display = ic.classList.contains(
        theme === 'dark' ? 'icon-moon' : 'icon-sun'
      ) ? 'block' : 'none';
    });
  }

  // Sync on load
  syncIcons(document.documentElement.getAttribute('data-theme') || 'dark');

  mobBtn.addEventListener('click', () => {
    const html  = document.documentElement;
    const next  = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
    html.setAttribute('data-theme', next);
    localStorage.setItem('harmber-theme', next);
    const meta  = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.content = next === 'dark' ? '#000000' : '#f6f6f7';
    syncIcons(next);
    spawnSparks(mobBtn, 6);

    // Also sync desktop toggle icons
    const deskBtn = document.getElementById('themeToggle');
    if (deskBtn) {
      deskBtn.querySelectorAll('.icon-sun, .icon-moon').forEach(ic => {
        ic.style.display = ic.classList.contains(
          next === 'dark' ? 'icon-moon' : 'icon-sun'
        ) ? 'block' : 'none';
      });
    }
  });
})();
