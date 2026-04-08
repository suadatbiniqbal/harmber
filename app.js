'use strict';

/* ── CURSOR ── */
(function () {
  const dot  = document.getElementById('cursorDot');
  const ring = document.getElementById('cursorRing');
  if (!dot || !ring) return;

  let rx = 0, ry = 0, mx = window.innerWidth / 2, my = window.innerHeight / 2;

  document.addEventListener('mousemove', e => {
    mx = e.clientX; my = e.clientY;
    document.body.style.setProperty('--cx', mx + 'px');
    document.body.style.setProperty('--cy', my + 'px');
  });
  document.addEventListener('mouseleave', () => { dot.style.opacity = '0'; ring.style.opacity = '0'; });
  document.addEventListener('mouseenter', () => { dot.style.opacity = '1'; ring.style.opacity = '1'; });

  (function loop() {
    rx += (mx - rx) * 0.13; ry += (my - ry) * 0.13;
    dot.style.left  = mx + 'px'; dot.style.top  = my + 'px';
    ring.style.left = rx + 'px'; ring.style.top = ry + 'px';
    requestAnimationFrame(loop);
  })();

  document.querySelectorAll('a, button, .fc, .scr-item, .dl-card, .ph-ctrl').forEach(el => {
    el.addEventListener('mouseenter', () => ring.classList.add('hover'));
    el.addEventListener('mouseleave', () => ring.classList.remove('hover'));
  });
})();

/* ── PROGRESS BAR ── */
(function () {
  const bar = document.getElementById('progressBar');
  if (!bar) return;
  window.addEventListener('scroll', () => {
    const t = document.documentElement.scrollHeight - window.innerHeight;
    bar.style.width = (window.scrollY / t * 100) + '%';
  }, { passive: true });
})();

/* ── NAV ── */
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

  function resize() { W = c.width = c.offsetWidth; H = c.height = c.offsetHeight; }
  window.addEventListener('resize', resize, { passive: true });
  resize();

  const waves = [
    { a: 50, f: .009, s: .010, p: 0.0, col: 'rgba(255,255,255,.05)' },
    { a: 35, f: .015, s: .016, p: 2.3, col: 'rgba(255,255,255,.034)' },
    { a: 65, f: .006, s: .006, p: 1.0, col: 'rgba(255,255,255,.02)' },
    { a: 22, f: .024, s: .020, p: 3.7, col: 'rgba(255,255,255,.024)' },
  ];

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    waves.forEach(w => {
      w.p += w.s;
      ctx.beginPath(); ctx.moveTo(0, H * .58);
      for (let x = 0; x <= W; x += 3) ctx.lineTo(x, H * .58 + Math.sin(x * w.f + w.p) * w.a);
      ctx.lineTo(W, H); ctx.lineTo(0, H); ctx.closePath();
      ctx.fillStyle = w.col; ctx.fill();
    });
    requestAnimationFrame(draw);
  })();
})();

/* ── DECORATIVE VISUALIZER CANVAS ── */
(function () {
  const c = document.getElementById('vizCanvas');
  if (!c) return;
  const ctx = c.getContext('2d');
  let W, H;

  function resize() { W = c.width = c.offsetWidth; H = c.height = c.offsetHeight; }
  window.addEventListener('resize', resize, { passive: true });
  resize();

  const bars = Array.from({ length: 80 }, (_, i) => ({
    x: i,
    h: 10 + Math.random() * 40,
    speed: .02 + Math.random() * .04,
    phase: Math.random() * Math.PI * 2,
  }));

  (function draw() {
    ctx.clearRect(0, 0, W, H);
    const barW = W / bars.length;
    bars.forEach((b, i) => {
      b.phase += b.speed;
      const h = b.h * (.5 + .5 * Math.sin(b.phase));
      const x = i * barW;
      const grad = ctx.createLinearGradient(0, H - h, 0, H);
      grad.addColorStop(0, 'rgba(255,255,255,.35)');
      grad.addColorStop(1, 'rgba(255,255,255,0)');
      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.roundRect(x + 1, H - h, barW - 2, h, 3);
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
  for (let i = 0; i < 16; i++) {
    const p = document.createElement('span');
    p.className = 'particle';
    p.textContent = symbols[Math.floor(Math.random() * symbols.length)];
    p.style.cssText = `
      left:${Math.random() * 100}%;
      animation-duration:${12 + Math.random() * 16}s;
      animation-delay:${-Math.random() * 22}s;
      font-size:${9 + Math.random() * 10}px;
      opacity:${(.05 + Math.random() * .07).toFixed(3)};
    `;
    el.appendChild(p);
  }
})();

/* ── REVEAL ── */
(function () {
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const delay = parseInt(e.target.dataset.delay || 0);
      setTimeout(() => e.target.classList.add('in'), delay);
      obs.unobserve(e.target);
    });
  }, { threshold: .07 });
  document.querySelectorAll('.reveal').forEach(el => obs.observe(el));
})();

/* ── SCREENSHOT REVEAL ── */
(function () {
  const items = document.querySelectorAll('.scr-item');
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const i = Array.from(items).indexOf(e.target);
      setTimeout(() => e.target.classList.add('in'), i * 90);
      obs.unobserve(e.target);
    });
  }, { threshold: .1 });
  items.forEach(el => obs.observe(el));
})();

/* ── 3D CARD TILT ── */
(function () {
  document.querySelectorAll('.fc').forEach(card => {
    card.addEventListener('mousemove', e => {
      const r = card.getBoundingClientRect();
      const x = e.clientX - r.left, y = e.clientY - r.top;
      const cx = r.width / 2, cy = r.height / 2;
      const rotX = ((y - cy) / cy) * -6;
      const rotY = ((x - cx) / cx) *  6;
      card.style.transform = `perspective(900px) translateY(-5px) rotateX(${rotX}deg) rotateY(${rotY}deg) scale(1.02)`;
      card.style.setProperty('--x', x + 'px');
      card.style.setProperty('--y', y + 'px');
      const glow = card.querySelector('.fc-glow');
      if (glow) glow.style.background = `radial-gradient(circle at ${x/r.width*100}% ${y/r.height*100}%, rgba(255,255,255,.09), transparent 55%)`;
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
  el.addEventListener('mousedown', e => { down = true; sx = e.pageX - el.offsetLeft; sl = el.scrollLeft; el.style.userSelect = 'none'; });
  el.addEventListener('mouseleave', () => down = false);
  el.addEventListener('mouseup',    () => { down = false; el.style.userSelect = ''; });
  el.addEventListener('mousemove',  e => {
    if (!down) return; e.preventDefault();
    el.scrollLeft = sl - (e.pageX - el.offsetLeft - sx) * 1.6;
  });
})();

/* ── STAT COUNTER ── */
(function () {
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const el = e.target, target = +el.dataset.target;
      const start = performance.now();
      const dur = 1600;
      function easeOut(t) { return 1 - Math.pow(1 - t, 3); }
      (function tick(now) {
        const t = Math.min((now - start) / dur, 1);
        el.textContent = Math.round(easeOut(t) * target);
        if (t < 1) requestAnimationFrame(tick);
      })(start);
      obs.unobserve(el);
    });
  }, { threshold: .5 });
  document.querySelectorAll('.sn[data-target]').forEach(el => obs.observe(el));
})();

/* ── PHONE PROGRESS ANIMATION ── */
(function () {
  const fill  = document.getElementById('phProgressFill');
  const thumb = document.getElementById('phProgressThumb');
  const timeEl = document.getElementById('phTimeNow');
  if (!fill || !thumb) return;

  let pct = 38;
  let totalSec = 227; // 3:47

  setInterval(() => {
    pct = pct >= 96 ? 4 : pct + .35;
    fill.style.width  = pct + '%';
    thumb.style.left  = pct + '%';
    const elapsed = Math.round((pct / 100) * totalSec);
    const m = Math.floor(elapsed / 60);
    const s = elapsed % 60;
    if (timeEl) timeEl.textContent = `${m}:${s.toString().padStart(2,'0')}`;
  }, 120);
})();

/* ── PHONE LYRICS ANIMATION ── */
(function () {
  const sets = [
    ['♪ In the silence between the notes', '♪ I find the music in my soul',  '♪ Every beat a different story'],
    ['♪ Echoes fill the empty room',        '♪ A melody that lights the dark', '♪ Sound becomes a second home'],
    ['♪ Turn the volume to the sky',         '♪ Let the rhythm carry you',      '♪ Nothing else but music now'],
    ['♪ Colors in a minor key',              '♪ Dancing through the frequencies','♪ This is where I want to be'],
  ];

  const fullSets = [
    ['In the silence','Between the notes','I find the music','In my soul','Every beat tells','A different story','Echoes of a','Forgotten melody'],
    ['Colors dancing','In a minor key','Frequencies align','Setting spirits    ['Colors dancing','In a minor key','Frequencies align','Setting spirits free','The bass drops low','The highs take flight','Music fills the','Empty of the night'],
    ['Turn the volume','To the sky','Let the rhythm','Carry you and I','Nothing else','But music now','Drifting through','The endless sound'],
  ];

  let idx = 0;

  function updateLyrics() {
    idx = (idx + 1) % sets.length;

    // Peek lyrics (center phone)
    const peekLines = document.querySelectorAll('#phLyrics .ph-lyric-line');
    peekLines.forEach((el, i) => {
      el.style.opacity = '0';
      el.style.transform = 'translateY(-6px)';
      setTimeout(() => {
        el.textContent = sets[idx][i];
        el.style.transition = 'opacity .5s ease, transform .5s ease';
        el.style.opacity = '1';
        el.style.transform = 'translateY(0)';
      }, i * 120);
    });

    // Full lyrics (right phone)
    const fullLines = document.querySelectorAll('#phLyrFull .ph-lf');
    fullLines.forEach((el, i) => {
      el.style.opacity = '0';
      setTimeout(() => {
        el.textContent = fullSets[idx][i] || '';
        el.style.transition = 'opacity .4s ease';
        el.style.opacity = '1';
      }, i * 60);
    });
  }

  setInterval(updateLyrics, 3200);
})();

/* ── PHONE ARTWORK COLOR SHIFT ── */
(function () {
  const artwork = document.getElementById('phArtwork');
  if (!artwork) return;

  const gradients = [
    'linear-gradient(135deg, rgba(255,255,255,.07), rgba(255,255,255,.02))',
    'linear-gradient(135deg, rgba(180,180,255,.08), rgba(255,255,255,.02))',
    'linear-gradient(135deg, rgba(255,200,200,.07), rgba(255,255,255,.02))',
    'linear-gradient(135deg, rgba(180,255,220,.07), rgba(255,255,255,.02))',
  ];

  let gi = 0;
  setInterval(() => {
    gi = (gi + 1) % gradients.length;
    artwork.style.transition = 'background 1.5s ease';
    artwork.style.background = gradients[gi];
  }, 3200);
})();

/* ── PHONE PLAY BUTTON TOGGLE ── */
(function () {
  const btn = document.getElementById('phPlayBtn');
  if (!btn) return;
  let playing = true;

  const playIcon = `<svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24" focusable="false"><path d="M8 5v14l11-7z"/></svg>`;
  const pauseIcon = `<svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24" focusable="false"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg>`;

  btn.innerHTML = pauseIcon; // starts as "playing"

  btn.addEventListener('click', () => {
    playing = !playing;
    btn.innerHTML = playing ? pauseIcon : playIcon;
    btn.style.transform = 'scale(.92)';
    setTimeout(() => btn.style.transform = '', 150);
  });
})();

/* ── HERO MOCKUP PARALLAX ── */
(function () {
  const row = document.querySelector('.hero-mockup-row');
  if (!row) return;

  window.addEventListener('scroll', () => {
    const scrolled = window.scrollY;
    if (scrolled > window.innerHeight) return;
    row.style.transform = `translateY(${scrolled * .12}px)`;
  }, { passive: true });
})();

/* ── GLOWING HOVER CARDS (download card) ── */
(function () {
  const card = document.querySelector('.dl-card');
  if (!card) return;

  card.addEventListener('mousemove', e => {
    const r = card.getBoundingClientRect();
    const x = ((e.clientX - r.left) / r.width)  * 100;
    const y = ((e.clientY - r.top)  / r.height) * 100;
    card.style.setProperty('--mx', x + '%');
    card.style.setProperty('--my', y + '%');
    card.querySelector('.dl-card-shine').style.background =
      `radial-gradient(circle at ${x}% ${y}%, rgba(255,255,255,.07), transparent 55%)`;
  });

  card.addEventListener('mouseleave', () => {
    card.querySelector('.dl-card-shine').style.background =
      'linear-gradient(135deg, rgba(255,255,255,.05), transparent 40%)';
  });
})();

/* ── SMOOTH ANCHOR SCROLL ── */
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
