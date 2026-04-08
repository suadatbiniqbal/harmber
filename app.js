'use strict';

/* ── CURSOR ── */
(function () {
  const dot = document.getElementById('cursorDot');
  const ring = document.getElementById('cursorRing');
  if (!dot || !ring) return;

  let rx = 0, ry = 0, mx = 0, my = 0;

  document.addEventListener('mousemove', e => {
    mx = e.clientX;
    my = e.clientY;
    document.body.style.setProperty('--cx', mx + 'px');
    document.body.style.setProperty('--cy', my + 'px');
  });

  document.addEventListener('mouseleave', () => {
    dot.style.opacity = '0';
    ring.style.opacity = '0';
  });

  document.addEventListener('mouseenter', () => {
    dot.style.opacity = '1';
    ring.style.opacity = '1';
  });

  (function loop() {
    rx += (mx - rx) * 0.13;
    ry += (my - ry) * 0.13;
    dot.style.left = mx + 'px';
    dot.style.top = my + 'px';
    ring.style.left = rx + 'px';
    ring.style.top = ry + 'px';
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
    W = c.width = c.offsetWidth;
    H = c.height = c.offsetHeight;
  }

  window.addEventListener('resize', resize, { passive: true });
  resize();

  const waves = [
    { a: 60, f: 0.009, s: 0.011, p: 0,   col: 'rgba(255,255,255,.055)' },
    { a: 40, f: 0.016, s: 0.017, p: 2.3, col: 'rgba(255,255,255,.038)' },
    { a: 80, f: 0.006, s: 0.006, p: 1.0, col: 'rgba(255,255,255,.022)' },
    { a: 28, f: 0.025, s: 0.021, p: 3.7, col: 'rgba(255,255,255,.028)' },
  ];

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    waves.forEach(w => {
      w.p += w.s;
      ctx.beginPath();
      ctx.moveTo(0, H * 0.62);
      for (let x = 0; x <= W; x += 3) {
        ctx.lineTo(x, H * 0.62 + Math.sin(x * w.f + w.p) * w.a);
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
      const r = card.getBoundingClientRect();
      const x = e.clientX - r.left;
      const y = e.clientY - r.top;
      const cx = r.width / 2;
      const cy = r.height / 2;
      const rotX = ((y - cy) / cy) * -7;
      const rotY = ((x - cx) / cx) * 7;

      card.style.transform = `perspective(900px) translateY(-6px) rotateX(${rotX}deg) rotateY(${rotY}deg) scale(1.03)`;

      card.style.setProperty('--x', x + 'px');
      card.style.setProperty('--y', y + 'px');

      const glow = card.querySelector('.fc-glow');
      if (glow) {
        const px = (x / r.width) * 100;
        const py = (y / r.height) * 100;
        glow.style.background = `radial-gradient(circle at ${px}% ${py}%, rgba(255,255,255,.09) 0%, transparent 60%)`;
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
  });
  el.addEventListener('mouseleave', () => down = false);
  el.addEventListener('mouseup', () => down = false);
  el.addEventListener('mousemove', e => {
    if (!down) return;
    e.preventDefault();
    el.scrollLeft = sl - (e.pageX - el.offsetLeft - sx) * 1.6;
  });
})();

/* ── APK DOWNLOAD BUTTON ── */
(function () {
  const btn = document.getElementById('apk-btn');
  if (!btn) return;

  btn.addEventListener('click', e => {
    e.preventDefault();
    window.location.href = 'https://github.com/suadatbiniqbal/harmber/releases/download/1.1.3/base.apk';
  });
})();
