'use strict';

/* ══════════════════════════════════════════
   THEME TOGGLE
══════════════════════════════════════════ */
(function () {
  const html        = document.documentElement;
  const KEY         = 'harmber-theme';
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  const initial     = localStorage.getItem(KEY) || (prefersDark ? 'dark' : 'light');

  function applyTheme(theme) {
    html.setAttribute('data-theme', theme);
    localStorage.setItem(KEY, theme);
    const meta = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.content = theme === 'dark' ? '#000000' : '#f6f6f7';
    syncAllToggleIcons(theme);
  }

  function syncAllToggleIcons(theme) {
    document.querySelectorAll('.theme-toggle, .theme-toggle-mob').forEach(btn => {
      const moon = btn.querySelector('.icon-moon');
      const sun  = btn.querySelector('.icon-sun');
      if (moon) moon.style.display = theme === 'dark'  ? 'block' : 'none';
      if (sun)  sun.style.display  = theme === 'light' ? 'block' : 'none';
    });
  }

  applyTheme(initial);

  document.querySelectorAll('#themeToggle, #themeToggleMob').forEach(btn => {
    if (!btn) return;
    btn.addEventListener('click', () => {
      const next = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      applyTheme(next);
      spawnSparks(btn, 7);
    });
  });
})();

/* ══════════════════════════════════════════
   CURSOR
══════════════════════════════════════════ */
(function () {
  const dot  = document.getElementById('cursorDot');
  const ring = document.getElementById('cursorRing');
  if (!dot || !ring) return;

  let rx = window.innerWidth  / 2;
  let ry = window.innerHeight / 2;
  let mx = rx, my = ry;

  document.addEventListener('mousemove', e => {
    mx = e.clientX; my = e.clientY;
    dot.style.left = mx + 'px';
    dot.style.top  = my + 'px';
    document.body.style.setProperty('--cx', mx + 'px');
    document.body.style.setProperty('--cy', my + 'px');
  });
  document.addEventListener('mouseleave', () => { dot.style.opacity = ring.style.opacity = '0'; });
  document.addEventListener('mouseenter', () => { dot.style.opacity = ring.style.opacity = '1'; });

  (function loop() {
    rx += (mx - rx) * 0.13;
    ry += (my - ry) * 0.13;
    ring.style.left = rx + 'px';
    ring.style.top  = ry + 'px';
    requestAnimationFrame(loop);
  })();

  document.querySelectorAll('a, button, .fc, .scr-item, .dl-card').forEach(el => {
    el.addEventListener('mouseenter', () => ring.classList.add('hover'));
    el.addEventListener('mouseleave', () => ring.classList.remove('hover'));
  });
})();

/* ══════════════════════════════════════════
   SCROLL PROGRESS BAR
══════════════════════════════════════════ */
(function () {
  const bar = document.getElementById('progressBar');
  if (!bar) return;
  window.addEventListener('scroll', () => {
    const t = document.documentElement.scrollHeight - window.innerHeight;
    if (t > 0) bar.style.width = (window.scrollY / t * 100) + '%';
  }, { passive: true });
})();

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

  function openDrawer() {
    btn.classList.add('open');
    btn.setAttribute('aria-expanded', 'true');
    drawer.classList.add('open');
    drawer.removeAttribute('aria-hidden');
    document.body.style.overflow = 'hidden';
  }

  function closeDrawer() {
    btn.classList.remove('open');
    btn.setAttribute('aria-expanded', 'false');
    drawer.classList.remove('open');
    drawer.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }

  btn.addEventListener('click', e => {
    e.stopPropagation();
    drawer.classList.contains('open') ? closeDrawer() : openDrawer();
  });

  drawer.querySelectorAll('.drawer-link, .drawer-cta').forEach(el =>
    el.addEventListener('click', closeDrawer)
  );

  document.addEventListener('click', e => {
    const nav = document.getElementById('nav');
    if (nav && !nav.contains(e.target)) closeDrawer();
  });

  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') closeDrawer();
  });

  window.addEventListener('resize', () => {
    if (window.innerWidth > 768) closeDrawer();
  }, { passive: true });
})();

/* ══════════════════════════════════════════
   WAVE CANVAS
══════════════════════════════════════════ */
(function () {
  const c = document.getElementById('waveCanvas');
  if (!c) return;
  const ctx = c.getContext('2d');
  let W = 0, H = 0;

  function resize() { W = c.width = c.offsetWidth; H = c.height = c.offsetHeight; }
  window.addEventListener('resize', resize, { passive: true });
  resize();

  const isDark = () => document.documentElement.getAttribute('data-theme') !== 'light';
  const waves  = [
    { a: 50, f: .009, s: .010, p: 0.0 },
    { a: 35, f: .015, s: .016, p: 2.3 },
    { a: 65, f: .006, s: .006, p: 1.0 },
    { a: 22, f: .024, s: .020, p: 3.7 },
  ];
  const alphas = [.05, .034, .02, .024];

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    const base = isDark() ? '255,255,255' : '0,0,0';
    waves.forEach((w, i) => {
      w.p += w.s;
      ctx.beginPath();
      ctx.moveTo(0, H * .58);
      for (let x = 0; x <= W; x += 3)
        ctx.lineTo(x, H * .58 + Math.sin(x * w.f + w.p) * w.a);
      ctx.lineTo(W, H); ctx.lineTo(0, H); ctx.closePath();
      ctx.fillStyle = `rgba(${base},${alphas[i]})`;
      ctx.fill();
    });
    requestAnimationFrame(draw);
  })();
})();

/* ══════════════════════════════════════════
   VISUALIZER CANVAS (equalizer bars)
══════════════════════════════════════════ */
(function () {
  const c = document.getElementById('vizCanvas');
  if (!c) return;
  const ctx = c.getContext('2d');
  let W = 0, H = 0;

  function resize() { W = c.width = c.offsetWidth; H = c.height = c.offsetHeight; }
  window.addEventListener('resize', resize, { passive: true });
  resize();

  const isDark = () => document.documentElement.getAttribute('data-theme') !== 'light';
  const bars   = Array.from({ length: 80 }, () => ({
    h:     10 + Math.random() * 40,
    speed: .018 + Math.random() * .035,
    phase: Math.random() * Math.PI * 2,
  }));

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    const base = isDark() ? '255,255,255' : '0,0,0';
    const bw   = W / bars.length;
    bars.forEach((b, i) => {
      b.phase += b.speed;
      const h    = b.h * (.4 + .6 * Math.sin(b.phase));
      const grad = ctx.createLinearGradient(0, H - h, 0, H);
      grad.addColorStop(0, `rgba(${base},.3)`);
      grad.addColorStop(1, `rgba(${base},0)`);
      ctx.fillStyle = grad;
      ctx.beginPath();
      if (ctx.roundRect) ctx.roundRect(i * bw + 1, H - h, bw - 2, h, 3);
      else ctx.rect(i * bw + 1, H - h, bw - 2, h);
      ctx.fill();
    });
    requestAnimationFrame(draw);
  })();
})();

/* ══════════════════════════════════════════
   GLOBAL MUSIC NOTE RAIN CANVAS
══════════════════════════════════════════ */
(function () {
  const c = document.getElementById('musicCanvas');
  if (!c) return;
  const ctx = c.getContext('2d');
  let W = 0, H = 0;

  function resize() { W = c.width = window.innerWidth; H = c.height = window.innerHeight; }
  window.addEventListener('resize', resize, { passive: true });
  resize();

  const isDark  = () => document.documentElement.getAttribute('data-theme') !== 'light';
  const SYMBOLS = ['♩','♪','♫','♬','𝄞','𝄢'];
  const notes   = Array.from({ length: 26 }, () => ({
    x:        Math.random() * 1200,
    y:        Math.random() * 900,
    sym:      SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)],
    size:     10 + Math.random() * 14,
    speed:    .2  + Math.random() * .45,
    drift:    (Math.random() - .5) * .28,
    opacity:  .04 + Math.random() * .065,
    rot:      Math.random() * Math.PI * 2,
    rotSpeed: (Math.random() - .5) * .014,
  }));

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    const col = isDark() ? 255 : 0;
    notes.forEach(n => {
      n.y -= n.speed; n.x += n.drift; n.rot += n.rotSpeed;
      if (n.y < -30)    { n.y = H + 20; n.x = Math.random() * W; }
      if (n.x < -40)    n.x = W + 20;
      if (n.x > W + 40) n.x = -20;
      ctx.save();
      ctx.globalAlpha = n.opacity;
      ctx.fillStyle   = `rgb(${col},${col},${col})`;
      ctx.font        = `${n.size}px serif`;
      ctx.textAlign   = 'center';
      ctx.translate(n.x, n.y);
      ctx.rotate(n.rot);
      ctx.fillText(n.sym, 0, 0);
      ctx.restore();
    });
    requestAnimationFrame(draw);
  })();
})();

/* ══════════════════════════════════════════
   DOM FLOATING PARTICLES (hero)
══════════════════════════════════════════ */
(function () {
  const el = document.getElementById('particles');
  if (!el) return;
  const syms = ['♩','♪','♫','♬'];
  for (let i = 0; i < 14; i++) {
    const p       = document.createElement('span');
    p.className   = 'particle';
    p.textContent = syms[i % syms.length];
    p.style.cssText = `
      left:               ${Math.random() * 100}%;
      animation-duration: ${12 + Math.random() * 16}s;
      animation-delay:    ${-Math.random() * 22}s;
      font-size:          ${9 + Math.random() * 10}px;
      opacity:            ${(.05 + Math.random() * .07).toFixed(3)};
    `;
    el.appendChild(p);
  }
})();

/* ══════════════════════════════════════════
   NOTE SPARKS — global helper
══════════════════════════════════════════ */
function spawnSparks(origin, count) {
  count = count || 8;
  const r   = origin.getBoundingClientRect();
  const cx  = r.left + r.width  / 2;
  const cy  = r.top  + r.height / 2;
  const syms = ['♪','♫','♬','♩'];

  for (let i = 0; i < count; i++) {
    const el       = document.createElement('span');
    el.className   = 'note-spark';
    el.textContent = syms[i % syms.length];
    const angle = (i / count) * Math.PI * 2;
    const dist  = 50 + Math.random() * 60;
    el.style.cssText = `
      position:fixed; left:${cx}px; top:${cy}px;
      --sx:${(Math.cos(angle)*dist).toFixed(1)}px;
      --sy:${(Math.sin(angle)*dist).toFixed(1)}px;
      font-size:${14+Math.random()*10}px;
      animation-duration:${(.6+Math.random()*.4).toFixed(2)}s;
      pointer-events:none; z-index:9999; color:var(--text);
    `;
    document.body.appendChild(el);
    el.addEventListener('animationend', () => el.remove(), { once: true });
  }
}

/* ══════════════════════════════════════════
   CLICK SPARKS (anywhere on page)
══════════════════════════════════════════ */
(function () {
  document.addEventListener('click', e => {
    spawnSparks({ getBoundingClientRect: () =>
      ({ left: e.clientX, top: e.clientY, width: 0, height: 0 }) }, 5);
  });
})();

/* ══════════════════════════════════════════
   REVEAL ON SCROLL
══════════════════════════════════════════ */
(function () {
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      setTimeout(() => e.target.classList.add('in'), parseInt(e.target.dataset.delay || 0));
      obs.unobserve(e.target);
    });
  }, { threshold: .07 });
  document.querySelectorAll('.reveal').forEach(el => obs.observe(el));
})();

/* ══════════════════════════════════════════
   SCREENSHOT STAGGERED REVEAL
══════════════════════════════════════════ */
(function () {
  const items = [...document.querySelectorAll('.scr-item')];
  const obs   = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      setTimeout(() => e.target.classList.add('in'), items.indexOf(e.target) * 90);
      obs.unobserve(e.target);
    });
  }, { threshold: .1 });
  items.forEach(el => obs.observe(el));
})();

/* ══════════════════════════════════════════
   3D CARD TILT
══════════════════════════════════════════ */
(function () {
  document.querySelectorAll('.fc').forEach(card => {
    card.addEventListener('mousemove', e => {
      const r    = card.getBoundingClientRect();
      const x    = e.clientX - r.left;
      const y    = e.clientY - r.top;
      const rotX = ((y - r.height/2) / (r.height/2)) * -6;
      const rotY = ((x - r.width /2) / (r.width /2)) *  6;
      card.style.transform =
        `perspective(900px) translateY(-5px) rotateX(${rotX}deg) rotateY(${rotY}deg) scale(1.02)`;
      const glow = card.querySelector('.fc-glow');
      if (glow) {
        glow.style.opacity    = '1';
        glow.style.background =
          `radial-gradient(circle at ${(x/r.width*100).toFixed(1)}% ${(y/r.height*100).toFixed(1)}%,
           color-mix(in srgb,var(--text) 9%,transparent),transparent 55%)`;
      }
    });
    card.addEventListener('mouseleave', () => {
      card.style.transform = '';
      const glow = card.querySelector('.fc-glow');
      if (glow) glow.style.opacity = '0';
    });
  });
})();

/* ══════════════════════════════════════════
   DRAG-TO-SCROLL (screenshot rail)
══════════════════════════════════════════ */
(function () {
  const el = document.getElementById('scrScroll');
  if (!el) return;
  let down = false, startX = 0, scrollLeft = 0;

  el.addEventListener('mousedown', e => {
    down = true; startX = e.pageX - el.offsetLeft; scrollLeft = el.scrollLeft;
    el.style.userSelect = 'none'; el.style.cursor = 'grabbing';
  });
  el.addEventListener('mouseleave', () => { down = false; el.style.cursor = 'grab'; });
  el.addEventListener('mouseup',    () => { down = false; el.style.userSelect = ''; el.style.cursor = 'grab'; });
  el.addEventListener('mousemove',  e => {
    if (!down) return;
    e.preventDefault();
    el.scrollLeft = scrollLeft - (e.pageX - el.offsetLeft - startX) * 1.6;
  });
})();

/* ══════════════════════════════════════════
   STAT COUNTER
══════════════════════════════════════════ */
(function () {
  const easeOut = t => 1 - Math.pow(1 - t, 3);
  const obs     = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const el     = e.target;
      const target = parseFloat(el.dataset.target);
      if (isNaN(target)) return;
      const start = performance.now();
      (function tick(now) {
        const t = Math.min((now - start) / 1600, 1);
        el.textContent = Math.round(easeOut(t) * target);
        if (t < 1) requestAnimationFrame(tick);
      })(start);
      obs.unobserve(el);
    });
  }, { threshold: .5 });
  document.querySelectorAll('.sn[data-target]').forEach(el => obs.observe(el));
})();

/* ══════════════════════════════════════════
   HERO PHONES PARALLAX
══════════════════════════════════════════ */
(function () {
  const row = document.querySelector('.hero-phones');
  if (!row) return;
  window.addEventListener('scroll', () => {
    if (window.scrollY < window.innerHeight)
      row.style.transform = `translateY(${window.scrollY * .1}px)`;
  }, { passive: true });
})();

/* ══════════════════════════════════════════
   DOWNLOAD CARD GLOW
══════════════════════════════════════════ */
(function () {
  const card  = document.querySelector('.dl-card');
  if (!card) return;
  const shine = card.querySelector('.dl-card-shine');
  card.addEventListener('mousemove', e => {
    const r = card.getBoundingClientRect();
    const x = ((e.clientX - r.left) / r.width  * 100).toFixed(1);
    const y = ((e.clientY - r.top)  / r.height * 100).toFixed(1);
    if (shine) shine.style.background =
      `radial-gradient(circle at ${x}% ${y}%,color-mix(in srgb,var(--text) 7%,transparent),transparent 55%)`;
  });
  card.addEventListener('mouseleave', () => {
    if (shine) shine.style.background =
      'linear-gradient(135deg,color-mix(in srgb,var(--text) 5%,transparent),transparent 40%)';
  });
})();

/* ══════════════════════════════════════════
   SMOOTH ANCHOR SCROLL
══════════════════════════════════════════ */
(function () {
  document.querySelectorAll('a[href^="#"]').forEach(a => {
    a.addEventListener('click', e => {
      const target = document.querySelector(a.getAttribute('href'));
      if (!target) return;
      e.preventDefault();
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  });
})();

/* ══════════════════════════════════════════
   VINYL HOVER SPEED-UP
══════════════════════════════════════════ */
(function () {
  const v = document.querySelector('.vinyl');
  if (!v) return;
  v.addEventListener('mouseenter', () => { v.style.animationDuration = '3s'; v.style.opacity = '.32'; });
  v.addEventListener('mouseleave', () => { v.style.animationDuration = '12s'; v.style.opacity = ''; });
})();

/* ══════════════════════════════════════════
   MARQUEE TOUCH PAUSE
══════════════════════════════════════════ */
(function () {
  const track = document.querySelector('.marquee-track');
  const wrap  = document.querySelector('.marquee-wrap');
  if (!track || !wrap) return;
  wrap.addEventListener('touchstart', () => track.style.animationPlayState = 'paused',  { passive: true });
  wrap.addEventListener('touchend',   () => track.style.animationPlayState = 'running', { passive: true });
})();

/* ══════════════════════════════════════════
   PAGE LOAD MUSIC BURST
══════════════════════════════════════════ */
window.addEventListener('load', () => {
  setTimeout(() => spawnSparks({
    getBoundingClientRect: () => ({
      left: window.innerWidth / 2, top: window.innerHeight / 2, width: 0, height: 0
    })
  }, 12), 800);
});
