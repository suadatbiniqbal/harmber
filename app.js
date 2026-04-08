'use strict';

/* ── CURSOR ── */
(function () {
  const dot  = document.getElementById('cursorDot');
  const ring = document.getElementById('cursorRing');
  if (!dot || !ring) return;

  let rx = 0, ry = 0, mx = window.innerWidth / 2, my = window.innerHeight / 2;

  document.addEventListener('mousemove', e => {
    mx = e.clientX;
    my = e.clientY;
    document.body.style.setProperty('--cx', mx + 'px');
    document.body.style.setProperty('--cy', my + 'px');
  });

  document.addEventListener('mouseleave', () => {
    dot.style.opacity  = '0';
    ring.style.opacity = '0';
  });

  document.addEventListener('mouseenter', () => {
    dot.style.opacity  = '1';
    ring.style.opacity = '1';
  });

  (function loop() {
    rx += (mx - rx) * 0.13;
    ry += (my - ry) * 0.13;
    dot.style.left  = mx + 'px';
    dot.style.top   = my + 'px';
    ring.style.left = rx + 'px';
    ring.style.top  = ry + 'px';
    requestAnimationFrame(loop);
  })();

  document.querySelectorAll('a, button, .fc, .scr-item, .dl-card').forEach(el => {
    el.addEventListener('mouseenter', () => ring.classList.add('hover'));
    el.addEventListener('mouseleave', () => ring.classList.remove('hover'));
  });
})();

/* ── PROGRESS BAR ── */
(function () {
  const bar = document.getElementById('progressBar');
  if (!bar) return;
  window.addEventListener('scroll', () => {
    const total = document.documentElement.scrollHeight - window.innerHeight;
    bar.style.width = (window.scrollY / total * 100) + '%';
  }, { passive: true });
})();

/* ── NAV SCROLL STATE ── */
(function () {
  const nav = document.getElementById('nav');
  if (!nav) return;
  window.addEventListener('scroll', () => {
    nav.classList.toggle('scrolled', window.scrollY > 30);
  }, { passive: true });
})();

/* ── WAVE CANVAS ── */
(function () {
  const c = document.getElementById('waveCanvas');
  if (!c) return;
  const ctx = c.getContext('2d');
  let W, H;

  function resize() {
    W = c.width  = c.offsetWidth;
    H = c.height = c.offsetHeight;
  }

  window.addEventListener('resize', resize, { passive: true });
  resize();

  const waves = [
    { a: 55, f: 0.009, s: 0.010, p: 0.0, col: 'rgba(255,255,255,.05)' },
    { a: 38, f: 0.015, s: 0.016, p: 2.3, col: 'rgba(255,255,255,.035)' },
    { a: 70, f: 0.006, s: 0.006, p: 1.0, col: 'rgba(255,255,255,.02)' },
    { a: 25, f: 0.024, s: 0.020, p: 3.7, col: 'rgba(255,255,255,.025)' },
  ];

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    waves.forEach(w => {
      w.p += w.s;
      ctx.beginPath();
      ctx.moveTo(0, H * 0.6);
      for (let x = 0; x <= W; x += 3) {
        ctx.lineTo(x, H * 0.6 + Math.sin(x * w.f + w.p) * w.a);
      }
      ctx.lineTo(W, H);
      ctx.lineTo(0, H);
      ctx.closePath();
      ctx.fillStyle = w.col;
      ctx.fill();
    });
    requestAnimationFrame(draw);
  })();
})();

/* ── PARTICLES ── */
(function () {
  const el = document.getElementById('particles');
  if (!el) return;
  const symbols = ['♩', '♪', '♫', '♬'];
  for (let i = 0; i < 14; i++) {
    const p = document.createElement('span');
    p.className = 'particle';
    p.textContent = symbols[Math.floor(Math.random() * symbols.length)];
    p.style.cssText = `
      left: ${Math.random() * 100}%;
      animation-duration: ${10 + Math.random() * 18}s;
      animation-delay: ${-Math.random() * 20}s;
      font-size: ${10 + Math.random() * 10}px;
      opacity: ${(0.06 + Math.random() * 0.08).toFixed(3)};
    `;
    el.appendChild(p);
  }
})();

/* ── REVEAL — General ── */
(function () {
  const els = document.querySelectorAll('.reveal');
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const delay = parseInt(e.target.dataset.delay || 0);
      setTimeout(() => e.target.classList.add('in'), delay);
      obs.unobserve(e.target);
    });
  }, { threshold: 0.07 });
  els.forEach(el => obs.observe(el));
})();

/* ── REVEAL — Screenshots ── */
(function () {
  const items = document.querySelectorAll('.scr-item');
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const i = Array.from(items).indexOf(e.target);
      setTimeout(() => e.target.classList.add('in'), i * 90);
      obs.unobserve(e.target);
    });
  }, { threshold: 0.12 });
  items.forEach(el => obs.observe(el));
})();

/* ── 3D CARD TILT ── */
(function () {
  document.querySelectorAll('.fc').forEach(card => {
    card.addEventListener('mousemove', e => {
      const r   = card.getBoundingClientRect();
      const x   = e.clientX - r.left;
      const y   = e.clientY - r.top;
      const cx  = r.width  / 2;
      const cy  = r.height / 2;
      const rotX = ((y - cy) / cy) * -6;
      const rotY = ((x - cx) / cx) *  6;

      card.style.transform =
        `perspective(900px) translateY(-5px) rotateX(${rotX}deg) rotateY(${rotY}deg) scale(1.02)`;

      card.style.setProperty('--x', x + 'px');
      card.style.setProperty('--y', y + 'px');

      const glow = card.querySelector('.fc-glow');
      if (glow) {
        glow.style.background = `radial-gradient(
          circle at ${(x / r.width) * 100}% ${(y / r.height) * 100}%,
          rgba(255,255,255,.10), transparent 55%
        )`;
      }
    });

    card.addEventListener('mouseleave', () => {
      card.style.transform = 'perspective(900px) rotateX(0deg) rotateY(0deg) scale(1)';
    });
  });
})();

/* ── DRAG SCROLL ── */
(function () {
  const el = document.getElementById('scrScroll');
  if (!el) return;
  let down = false, sx = 0, sl = 0;

  el.addEventListener('mousedown', e => {
    down = true;
    sx = e.pageX - el.offsetLeft;
    sl = el.scrollLeft;
    el.style.userSelect = 'none';
  });
  el.addEventListener('mouseleave', () => { down = false; });
  el.addEventListener('mouseup',    () => { down = false; el.style.userSelect = ''; });
  el.addEventListener('mousemove',  e => {
    if (!down) return;
    e.preventDefault();
    el.scrollLeft = sl - (e.pageX - el.offsetLeft - sx) * 1.6;
  });
})();

/* ── STAT COUNTER ── */
(function () {
  const stats = document.querySelectorAll('.sn[data-target]');
  if (!stats.length) return;

  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const el     = e.target;
      const target = +el.dataset.target;
      const dur    = 1600;
      const start  = performance.now();

      function tick(now) {
        const t   = Math.min((now - start) / dur, 1);
        const val = Math.round(easeOut(t) * target);
        el.textContent = val;
        if (t < 1) requestAnimationFrame(tick);
      }

      function easeOut(t) { return 1 - Math.pow(1 - t, 3); }

      requestAnimationFrame(tick);
      obs.unobserve(el);
    });
  }, { threshold: 0.5 });

  stats.forEach(el => obs.observe(el));
})();

/* ── APK DOWNLOAD BUTTON ── */
(function () {
  const btn = document.getElementById('apk-btn');
  if (!btn) return;
  // href is already set directly in HTML — JS fallback only
  btn.addEventListener('click', e => {
    if (!btn.href || btn.href === '#') {
      e.preventDefault();
      window.location.href =
        'https://github.com/suadatbiniqbal/harmber/releases/download/1.1.3/base.apk';
    }
  });
})();
