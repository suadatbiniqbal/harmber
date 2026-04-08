'use strict';

/* ══════════════════════════════════════════
   THEME TOGGLE
══════════════════════════════════════════ */
(function () {
  const html    = document.documentElement;
  const btn     = document.getElementById('themeToggle');
  const KEY     = 'harmber-theme';
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  const saved   = localStorage.getItem(KEY);
  const initial = saved || (prefersDark ? 'dark' : 'light');

  function apply(theme) {
    html.setAttribute('data-theme', theme);
    localStorage.setItem(KEY, theme);
    const meta = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.content = theme === 'dark' ? '#000000' : '#f6f6f7';
  }

  apply(initial);

  if (btn) {
    btn.addEventListener('click', () => {
      const next = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      apply(next);
      spawnSparks(btn, 7);
    });
  }
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
    mx = e.clientX;
    my = e.clientY;
    dot.style.left = mx + 'px';
    dot.style.top  = my + 'px';
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
    const total = document.documentElement.scrollHeight - window.innerHeight;
    if (total > 0) bar.style.width = (window.scrollY / total * 100) + '%';
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
   WAVE CANVAS
══════════════════════════════════════════ */
(function () {
  const c = document.getElementById('waveCanvas');
  if (!c) return;
  const ctx = c.getContext('2d');
  let W = 0, H = 0;

  function resize() {
    W = c.width  = c.offsetWidth;
    H = c.height = c.offsetHeight;
  }
  window.addEventListener('resize', resize, { passive: true });
  resize();

  const waves = [
    { a: 50, f: 0.009, s: 0.010, p: 0.0 },
    { a: 35, f: 0.015, s: 0.016, p: 2.3 },
    { a: 65, f: 0.006, s: 0.006, p: 1.0 },
    { a: 22, f: 0.024, s: 0.020, p: 3.7 },
  ];
  const alphas = [0.05, 0.034, 0.02, 0.024];

  function isDark() {
    return document.documentElement.getAttribute('data-theme') !== 'light';
  }

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    const base = isDark() ? '255,255,255' : '0,0,0';
    waves.forEach((w, i) => {
      w.p += w.s;
      ctx.beginPath();
      ctx.moveTo(0, H * 0.58);
      for (let x = 0; x <= W; x += 3)
        ctx.lineTo(x, H * 0.58 + Math.sin(x * w.f + w.p) * w.a);
      ctx.lineTo(W, H);
      ctx.lineTo(0, H);
      ctx.closePath();
      ctx.fillStyle = `rgba(${base},${alphas[i]})`;
      ctx.fill();
    });
    requestAnimationFrame(draw);
  })();
})();

/* ══════════════════════════════════════════
   VISUALIZER CANVAS (bar equalizer)
══════════════════════════════════════════ */
(function () {
  const c = document.getElementById('vizCanvas');
  if (!c) return;
  const ctx = c.getContext('2d');
  let W = 0, H = 0;

  function resize() {
    W = c.width  = c.offsetWidth;
    H = c.height = c.offsetHeight;
  }
  window.addEventListener('resize', resize, { passive: true });
  resize();

  function isDark() {
    return document.documentElement.getAttribute('data-theme') !== 'light';
  }

  const bars = Array.from({ length: 80 }, () => ({
    h:     10 + Math.random() * 40,
    speed: 0.018 + Math.random() * 0.035,
    phase: Math.random() * Math.PI * 2,
  }));

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    const base = isDark() ? '255,255,255' : '0,0,0';
    const bw = W / bars.length;
    bars.forEach((b, i) => {
      b.phase += b.speed;
      const h = b.h * (0.4 + 0.6 * Math.sin(b.phase));
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

  function resize() {
    W = c.width  = window.innerWidth;
    H = c.height = window.innerHeight;
  }
  window.addEventListener('resize', resize, { passive: true });
  resize();

  function isDark() {
    return document.documentElement.getAttribute('data-theme') !== 'light';
  }

  const SYMBOLS = ['♩', '♪', '♫', '♬', '𝄞', '𝄢'];
  const notes = Array.from({ length: 26 }, () => ({
    x:        Math.random() * 1200,
    y:        Math.random() * 900,
    sym:      SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)],
    size:     10 + Math.random() * 14,
    speed:    0.2 + Math.random() * 0.45,
    drift:    (Math.random() - 0.5) * 0.28,
    opacity:  0.04 + Math.random() * 0.065,
    rot:      Math.random() * Math.PI * 2,
    rotSpeed: (Math.random() - 0.5) * 0.014,
  }));

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    const col = isDark() ? 255 : 0;
    notes.forEach(n => {
      n.y   -= n.speed;
      n.x   += n.drift;
      n.rot += n.rotSpeed;
      if (n.y < -30)   { n.y = H + 20;            n.x = Math.random() * W; }
      if (n.x < -40)   { n.x = W + 20; }
      if (n.x > W + 40){ n.x = -20; }

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
   DOM FLOATING PARTICLES (hero notes)
══════════════════════════════════════════ */
(function () {
  const el = document.getElementById('particles');
  if (!el) return;
  const syms = ['♩', '♪', '♫', '♬'];
  for (let i = 0; i < 14; i++) {
    const p       = document.createElement('span');
    p.className   = 'particle';
    p.textContent = syms[i % syms.length];
    p.style.cssText = `
      left:              ${Math.random() * 100}%;
      animation-duration:${12 + Math.random() * 16}s;
      animation-delay:   ${-Math.random() * 22}s;
      font-size:         ${9 + Math.random() * 10}px;
      opacity:           ${(0.05 + Math.random() * 0.07).toFixed(3)};
    `;
    el.appendChild(p);
  }
})();

/* ══════════════════════════════════════════
   NOTE SPARKS BURST (global helper)
══════════════════════════════════════════ */
function spawnSparks(origin, count) {
  count = count || 8;
  const container = document.getElementById('noteSparks') || document.body;
  const r  = origin.getBoundingClientRect();
  const cx = r.left + r.width  / 2;
  const cy = r.top  + r.height / 2;
  const syms = ['♪', '♫', '♬', '♩'];

  for (let i = 0; i < count; i++) {
    const el   = document.createElement('span');
    el.className   = 'note-spark';
    el.textContent = syms[i % syms.length];
    const angle = (i / count) * Math.PI * 2;
    const dist  = 50 + Math.random() * 60;
    el.style.cssText = `
      position: fixed;
      left: ${cx}px;
      top:  ${cy}px;
      --sx: ${(Math.cos(angle) * dist).toFixed(1)}px;
      --sy: ${(Math.sin(angle) * dist).toFixed(1)}px;
      font-size:          ${14 + Math.random() * 10}px;
      animation-duration: ${(0.6 + Math.random() * 0.4).toFixed(2)}s;
      pointer-events:     none;
      z-index:            9999;
      color:              var(--text);
    `;
    container.appendChild(el);
    el.addEventListener('animationend', () => el.remove(), { once: true });
  }
}

/* ══════════════════════════════════════════
   CLICK-TO-SPAWN SPARKS (whole page)
══════════════════════════════════════════ */
(function () {
  document.addEventListener('click', e => {
    const fake = { getBoundingClientRect: () => ({
      left: e.clientX, top: e.clientY, width: 0, height: 0
    })};
    spawnSparks(fake, 5);
  });
})();

/* ══════════════════════════════════════════
   REVEAL ON SCROLL
══════════════════════════════════════════ */
(function () {
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const delay = parseInt(e.target.dataset.delay || 0);
      setTimeout(() => e.target.classList.add('in'), delay);
      obs.unobserve(e.target);
    });
  }, { threshold: 0.07 });

  document.querySelectorAll('.reveal').forEach(el => obs.observe(el));
})();

/* ══════════════════════════════════════════
   SCREENSHOT STAGGERED REVEAL
══════════════════════════════════════════ */
(function () {
  const items = [...document.querySelectorAll('.scr-item')];
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const i = items.indexOf(e.target);
      setTimeout(() => e.target.classList.add('in'), i * 90);
      obs.unobserve(e.target);
    });
  }, { threshold: 0.1 });
  items.forEach(el => obs.observe(el));
})();

/* ══════════════════════════════════════════
   3D CARD TILT (feature cards)
══════════════════════════════════════════ */
(function () {
  document.querySelectorAll('.fc').forEach(card => {
    card.addEventListener('mousemove', e => {
      const r    = card.getBoundingClientRect();
      const x    = e.clientX - r.left;
      const y    = e.clientY - r.top;
      const rotX = ((y - r.height / 2) / (r.height / 2)) * -6;
      const rotY = ((x - r.width  / 2) / (r.width  / 2)) *  6;
      card.style.transform =
        `perspective(900px) translateY(-5px) rotateX(${rotX}deg) rotateY(${rotY}deg) scale(1.02)`;
      const glow = card.querySelector('.fc-glow');
      if (glow) {
        glow.style.opacity    = '1';
        glow.style.background =
          `radial-gradient(circle at ${(x/r.width*100).toFixed(1)}% ${(y/r.height*100).toFixed(1)}%,
           color-mix(in srgb, var(--text) 9%, transparent), transparent 55%)`;
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
    down       = true;
    startX     = e.pageX - el.offsetLeft;
    scrollLeft = el.scrollLeft;
    el.style.userSelect = 'none';
    el.style.cursor     = 'grabbing';
  });

  el.addEventListener('mouseleave', () => {
    down = false;
    el.style.cursor = 'grab';
  });

  el.addEventListener('mouseup', () => {
    down = false;
    el.style.userSelect = '';
    el.style.cursor     = 'grab';
  });

  el.addEventListener('mousemove', e => {
    if (!down) return;
    e.preventDefault();
    const x    = e.pageX - el.offsetLeft;
    const walk = (x - startX) * 1.6;
    el.scrollLeft = scrollLeft - walk;
  });
})();

/* ══════════════════════════════════════════
   STAT COUNTER ANIMATION
══════════════════════════════════════════ */
(function () {
  const easeOut = t => 1 - Math.pow(1 - t, 3);

  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const el     = e.target;
      const target = parseFloat(el.dataset.target);
      if (isNaN(target)) return;
      const dur    = 1600;
      const start  = performance.now();

      (function tick(now) {
        const t   = Math.min((now - start) / dur, 1);
        el.textContent = Math.round(easeOut(t) * target);
        if (t < 1) requestAnimationFrame(tick);
      })(start);

      obs.unobserve(el);
    });
  }, { threshold: 0.5 });

  document.querySelectorAll('.sn[data-target]').forEach(el => obs.observe(el));
})();

/* ══════════════════════════════════════════
   PHONE PROGRESS BAR ANIMATION
══════════════════════════════════════════ */
(function () {
  const fill    = document.getElementById('phProgressFill');
  const dot     = document.getElementById('phProgressDot');
  const timeEl  = document.getElementById('phTimeNow');
  if (!fill) return;

  let pct = 38;
  const TOTAL_SECS = 227; // 3:47

  setInterval(() => {
    pct = pct >= 96 ? 4 : pct + 0.35;
    fill.style.width = pct + '%';
    if (dot)    dot.style.left = pct + '%';
    if (timeEl) {
      const s = Math.round((pct / 100) * TOTAL_SECS);
      timeEl.textContent =
        `${Math.floor(s / 60)}:${(s % 60).toString().padStart(2, '0')}`;
    }
  }, 120);
})();



  let idx = 0;

  function update() {
    idx = (idx + 1) % peekSets.length;

    // Center phone — peek lyrics
    document.querySelectorAll('#phLyrics .lyr').forEach((el, i) => {
      el.style.opacity   = '0';
      el.style.transform = 'translateY(-6px)';
      setTimeout(() => {
        el.textContent     = peekSets[idx][i] || '';
        el.style.transition = 'opacity .5s ease, transform .5s ease';
        el.style.opacity   = '1';
        el.style.transform = 'translateY(0)';
      }, i * 110);
    });

    // Right phone — full lyrics (if present)
    const fullEl = document.getElementById('phLyrFull');
    if (fullEl) {
      [...fullEl.querySelectorAll('.ph-lf')].forEach((el, i) => {
        el.style.opacity = '0';
        setTimeout(() => {
          el.textContent = fullSets[idx][i] || '';
          el.style.transition = 'opacity .4s ease';
          el.style.opacity = '1';
        }, i * 55);
      });
    }
  }

  setInterval(update, 3400);
})();



/* ══════════════════════════════════════════
   HERO PHONES PARALLAX
══════════════════════════════════════════ */
(function () {
  const row = document.querySelector('.hero-phones');
  if (!row) return;
  window.addEventListener('scroll', () => {
    const y = window.scrollY;
    if (y > window.innerHeight) return;
    row.style.transform = `translateY(${y * 0.1}px)`;
  }, { passive: true });
})();

/* ══════════════════════════════════════════
   DOWNLOAD CARD CURSOR GLOW
══════════════════════════════════════════ */
(function () {
  const card = document.querySelector('.dl-card');
  if (!card) return;
  const shine = card.querySelector('.dl-card-shine');

  card.addEventListener('mousemove', e => {
    const r = card.getBoundingClientRect();
    const x = ((e.clientX - r.left) / r.width)  * 100;
    const y = ((e.clientY - r.top)  / r.height) * 100;
    if (shine) {
      shine.style.background =
        `radial-gradient(circle at ${x}% ${y}%,
         color-mix(in srgb, var(--text) 7%, transparent), transparent 55%)`;
    }
  });

  card.addEventListener('mouseleave', () => {
    if (shine) {
      shine.style.background =
        'linear-gradient(135deg, color-mix(in srgb, var(--text) 5%, transparent), transparent 40%)';
    }
  });
})();

/* ══════════════════════════════════════════
   SMOOTH ANCHOR SCROLL
══════════════════════════════════════════ */
(function () {
  document.querySelectorAll('a[href^="#"]').forEach(a => {
    a.addEventListener('click', e => {
      const id     = a.getAttribute('href');
      const target = document.querySelector(id);
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
  const vinyl = document.querySelector('.vinyl');
  if (!vinyl) return;
  vinyl.addEventListener('mouseenter', () => {
    vinyl.style.animationDuration = '3s';
    vinyl.style.opacity = '0.35';
  });
  vinyl.addEventListener('mouseleave', () => {
    vinyl.style.animationDuration = '12s';
    vinyl.style.opacity = '';
  });
})();

/* ══════════════════════════════════════════
   MARQUEE PAUSE ON HOVER (already CSS,
   JS fallback for touch)
══════════════════════════════════════════ */
(function () {
  const track = document.querySelector('.marquee-track');
  const wrap  = document.querySelector('.marquee-wrap');
  if (!track || !wrap) return;
  wrap.addEventListener('touchstart', () =>
    (track.style.animationPlayState = 'paused'), { passive: true });
  wrap.addEventListener('touchend', () =>
    (track.style.animationPlayState = 'running'), { passive: true });
})();

/* ══════════════════════════════════════════
   EQUALIZER BARS — feature card icons
   (add pulsing EQ to any .fi with data-eq)
══════════════════════════════════════════ */
(function () {
  document.querySelectorAll('.fi[data-eq]').forEach(fi => {
    fi.innerHTML = '';
    const eq = document.createElement('div');
    eq.className = 'fi-eq';
    for (let i = 0; i < 4; i++) {
      const bar = document.createElement('span');
      bar.style.animationDelay = (i * 0.12) + 's';
      eq.appendChild(bar);
    }
    fi.appendChild(eq);
  });
})();

/* ══════════════════════════════════════════
   PAGE LOAD MUSIC BURST
══════════════════════════════════════════ */
(function () {
  window.addEventListener('load', () => {
    const fake = {
      getBoundingClientRect: () => ({
        left:   window.innerWidth  / 2,
        top:    window.innerHeight / 2,
        width:  0,
        height: 0,
      }),
    };
    setTimeout(() => spawnSparks(fake, 12), 800);
  });
})();
