'use strict';

/* =========================================
   HARMERB LANDING PAGE — CLEAN APP.JS
========================================= */

(() => {
  const doc = document;
  const html = doc.documentElement;
  const body = doc.body;

  const $ = (selector, scope = doc) => scope.querySelector(selector);
  const $$ = (selector, scope = doc) => [...scope.querySelectorAll(selector)];

  const state = {
    reducedMotion: window.matchMedia('(prefers-reduced-motion: reduce)').matches,
    isTouch: window.matchMedia('(hover: none)').matches,
    isMobile: window.innerWidth <= 768,
    theme: 'dark'
  };

  const rafTasks = new Set();
  let rafId = null;

  const runRAF = (time) => {
    rafTasks.forEach(fn => fn(time));
    rafId = rafTasks.size ? requestAnimationFrame(runRAF) : null;
  };

  const addRAFTask = (fn) => {
    rafTasks.add(fn);
    if (!rafId) rafId = requestAnimationFrame(runRAF);
  };

  const removeRAFTask = (fn) => {
    rafTasks.delete(fn);
    if (!rafTasks.size && rafId) {
      cancelAnimationFrame(rafId);
      rafId = null;
    }
  };

  const onResizeFlags = () => {
    state.isMobile = window.innerWidth <= 768;
  };

  window.addEventListener('resize', onResizeFlags, { passive: true });

  /* =========================================
     THEME
  ========================================= */
  (() => {
    const KEY = 'harmber-theme';
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const initial = localStorage.getItem(KEY) || (prefersDark ? 'dark' : 'light');

    const syncIcons = (theme) => {
      $$('.theme-toggle, .theme-toggle-mob').forEach(btn => {
        const moon = $('.icon-moon', btn);
        const sun = $('.icon-sun', btn);
        if (moon) moon.style.display = theme === 'dark' ? 'block' : 'none';
        if (sun) sun.style.display = theme === 'light' ? 'block' : 'none';
      });
    };

    const applyTheme = (theme) => {
      state.theme = theme;
      html.setAttribute('data-theme', theme);
      localStorage.setItem(KEY, theme);

      const meta = $('meta[name="theme-color"]');
      if (meta) meta.content = theme === 'dark' ? '#000000' : '#f6f6f8';

      syncIcons(theme);
    };

    applyTheme(initial);

    $$('#themeToggle, #themeToggleMob').forEach(btn => {
      btn.addEventListener('click', () => {
        const next = state.theme === 'dark' ? 'light' : 'dark';
        applyTheme(next);
        spawnSparksFromElement(btn, 7);
      });
    });
  })();

  /* =========================================
     CURSOR + SPOTLIGHT
  ========================================= */
  (() => {
    const dot = $('#cursorDot');
    const ring = $('#cursorRing');

    if (!dot || !ring || state.isTouch) return;

    let mx = window.innerWidth / 2;
    let my = window.innerHeight / 2;
    let rx = mx;
    let ry = my;

    const updateCursor = () => {
      rx += (mx - rx) * 0.13;
      ry += (my - ry) * 0.13;
      ring.style.left = `${rx}px`;
      ring.style.top = `${ry}px`;
    };

    doc.addEventListener('mousemove', (e) => {
      mx = e.clientX;
      my = e.clientY;
      dot.style.left = `${mx}px`;
      dot.style.top = `${my}px`;
      body.style.setProperty('--cx', `${mx}px`);
      body.style.setProperty('--cy', `${my}px`);
    });

    doc.addEventListener('mouseleave', () => {
      dot.style.opacity = '0';
      ring.style.opacity = '0';
    });

    doc.addEventListener('mouseenter', () => {
      dot.style.opacity = '1';
      ring.style.opacity = '1';
    });

    addRAFTask(updateCursor);

    $$('a, button, .fc, .scr-item, .dl-card').forEach(el => {
      el.addEventListener('mouseenter', () => ring.classList.add('hover'));
      el.addEventListener('mouseleave', () => ring.classList.remove('hover'));
    });
  })();

  /* =========================================
     PROGRESS BAR + NAV
  ========================================= */
  (() => {
    const bar = $('#progressBar');
    const nav = $('#nav');
    if (!bar && !nav) return;

    let ticking = false;

    const update = () => {
      const maxScroll = doc.documentElement.scrollHeight - window.innerHeight;
      const progress = maxScroll > 0 ? (window.scrollY / maxScroll) * 100 : 0;

      if (bar) bar.style.width = `${progress}%`;
      if (nav) nav.classList.toggle('scrolled', window.scrollY > 30);

      ticking = false;
    };

    window.addEventListener('scroll', () => {
      if (ticking) return;
      ticking = true;
      requestAnimationFrame(update);
    }, { passive: true });

    update();
  })();

  /* =========================================
     MOBILE DRAWER
  ========================================= */
  (() => {
    const btn = $('#hamburger');
    const drawer = $('#mobileDrawer');
    const nav = $('#nav');
    if (!btn || !drawer || !nav) return;

    const openDrawer = () => {
      btn.classList.add('open');
      btn.setAttribute('aria-expanded', 'true');
      drawer.classList.add('open');
      drawer.setAttribute('aria-hidden', 'false');
      body.style.overflow = 'hidden';
    };

    const closeDrawer = () => {
      btn.classList.remove('open');
      btn.setAttribute('aria-expanded', 'false');
      drawer.classList.remove('open');
      drawer.setAttribute('aria-hidden', 'true');
      body.style.overflow = '';
    };

    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      drawer.classList.contains('open') ? closeDrawer() : openDrawer();
    });

    $$('.drawer-link, .drawer-cta', drawer).forEach(el => {
      el.addEventListener('click', closeDrawer);
    });

    doc.addEventListener('click', (e) => {
      if (!nav.contains(e.target)) closeDrawer();
    });

    doc.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') closeDrawer();
    });

    window.addEventListener('resize', () => {
      if (window.innerWidth > 768) closeDrawer();
    }, { passive: true });
  })();

  /* =========================================
     REVEAL OBSERVERS
  ========================================= */
  (() => {
    const revealItems = $$('.reveal');
    if (!revealItems.length) return;

    const observer = new IntersectionObserver((entries, obs) => {
      entries.forEach(entry => {
        if (!entry.isIntersecting) return;
        const delay = parseInt(entry.target.dataset.delay || '0', 10);
        window.setTimeout(() => entry.target.classList.add('in'), delay);
        obs.unobserve(entry.target);
      });
    }, { threshold: 0.08 });

    revealItems.forEach(el => observer.observe(el));
  })();

  (() => {
    const items = $$('.scr-item');
    if (!items.length) return;

    const observer = new IntersectionObserver((entries, obs) => {
      entries.forEach(entry => {
        if (!entry.isIntersecting) return;
        const index = items.indexOf(entry.target);
        window.setTimeout(() => entry.target.classList.add('in'), index * 90);
        obs.unobserve(entry.target);
      });
    }, { threshold: 0.12 });

    items.forEach(el => observer.observe(el));
  })();

  /* =========================================
     STATS COUNTER
  ========================================= */
  (() => {
    const counters = $$('.sn[data-target]');
    if (!counters.length) return;

    const easeOut = t => 1 - Math.pow(1 - t, 3);

    const observer = new IntersectionObserver((entries, obs) => {
      entries.forEach(entry => {
        if (!entry.isIntersecting) return;

        const el = entry.target;
        const target = parseFloat(el.dataset.target);
        if (Number.isNaN(target)) return;

        const start = performance.now();

        const animate = (now) => {
          const t = Math.min((now - start) / 1600, 1);
          el.textContent = Math.round(easeOut(t) * target);
          if (t < 1) requestAnimationFrame(animate);
        };

        requestAnimationFrame(animate);
        obs.unobserve(el);
      });
    }, { threshold: 0.5 });

    counters.forEach(el => observer.observe(el));
  })();

  /* =========================================
     CANVAS HELPERS
  ========================================= */
  const createCanvasController = (canvas, drawFrame) => {
    if (!canvas || state.reducedMotion || state.isMobile) return null;

    const ctx = canvas.getContext('2d');
    if (!ctx) return null;

    let width = 0;
    let height = 0;

    const resize = () => {
      width = canvas.width = canvas.offsetWidth || window.innerWidth;
      height = canvas.height = canvas.offsetHeight || window.innerHeight;
    };

    const loop = () => drawFrame({ ctx, width, height });

    resize();
    window.addEventListener('resize', resize, { passive: true });
    addRAFTask(loop);

    return { resize, destroy: () => removeRAFTask(loop) };
  };

  /* =========================================
     WAVE CANVAS
  ========================================= */
  (() => {
    const canvas = $('#waveCanvas');
    if (!canvas) return;

    const waves = [
      { a: 50, f: 0.009, s: 0.01, p: 0.0 },
      { a: 35, f: 0.015, s: 0.016, p: 2.3 },
      { a: 65, f: 0.006, s: 0.006, p: 1.0 },
      { a: 22, f: 0.024, s: 0.02, p: 3.7 }
    ];
    const alphas = [0.05, 0.034, 0.02, 0.024];

    createCanvasController(canvas, ({ ctx, width, height }) => {
      const base = state.theme === 'dark' ? '255,255,255' : '0,0,0';
      ctx.clearRect(0, 0, width, height);

      waves.forEach((wave, i) => {
        wave.p += wave.s;
        ctx.beginPath();
        ctx.moveTo(0, height * 0.58);

        for (let x = 0; x <= width; x += 3) {
          ctx.lineTo(x, height * 0.58 + Math.sin(x * wave.f + wave.p) * wave.a);
        }

        ctx.lineTo(width, height);
        ctx.lineTo(0, height);
        ctx.closePath();
        ctx.fillStyle = `rgba(${base},${alphas[i]})`;
        ctx.fill();
      });
    });
  })();

  /* =========================================
     VISUALIZER CANVAS
  ========================================= */
  (() => {
    const canvas = $('#vizCanvas');
    if (!canvas) return;

    const bars = Array.from({ length: 80 }, () => ({
      h: 10 + Math.random() * 40,
      speed: 0.018 + Math.random() * 0.035,
      phase: Math.random() * Math.PI * 2
    }));

    createCanvasController(canvas, ({ ctx, width, height }) => {
      const base = state.theme === 'dark' ? '255,255,255' : '0,0,0';
      const barWidth = width / bars.length;

      ctx.clearRect(0, 0, width, height);

      bars.forEach((bar, i) => {
        bar.phase += bar.speed;
        const h = bar.h * (0.4 + 0.6 * Math.sin(bar.phase));
        const grad = ctx.createLinearGradient(0, height - h, 0, height);
        grad.addColorStop(0, `rgba(${base},.3)`);
        grad.addColorStop(1, `rgba(${base},0)`);

        ctx.fillStyle = grad;
        ctx.beginPath();

        if (ctx.roundRect) {
          ctx.roundRect(i * barWidth + 1, height - h, barWidth - 2, h, 3);
        } else {
          ctx.rect(i * barWidth + 1, height - h, barWidth - 2, h);
        }

        ctx.fill();
      });
    });
  })();

  /* =========================================
     GLOBAL NOTE RAIN CANVAS
  ========================================= */
  (() => {
    const canvas = $('#musicCanvas');
    if (!canvas || state.reducedMotion) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = 0;
    let height = 0;

    const symbols = ['♩', '♪', '♫', '♬', '𝄞', '𝄢'];
    const noteCount = state.isMobile ? 10 : 20;

    const notes = Array.from({ length: noteCount }, () => ({
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      sym: symbols[Math.floor(Math.random() * symbols.length)],
      size: 10 + Math.random() * 14,
      speed: 0.2 + Math.random() * 0.45,
      drift: (Math.random() - 0.5) * 0.28,
      opacity: 0.04 + Math.random() * 0.065,
      rot: Math.random() * Math.PI * 2,
      rotSpeed: (Math.random() - 0.5) * 0.014
    }));

    const resize = () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };

    const draw = () => {
      const c = state.theme === 'dark' ? 255 : 0;
      ctx.clearRect(0, 0, width, height);

      notes.forEach(note => {
        note.y -= note.speed;
        note.x += note.drift;
        note.rot += note.rotSpeed;

        if (note.y < -30) {
          note.y = height + 20;
          note.x = Math.random() * width;
        }

        if (note.x < -40) note.x = width + 20;
        if (note.x > width + 40) note.x = -20;

        ctx.save();
        ctx.globalAlpha = note.opacity;
        ctx.fillStyle = `rgb(${c},${c},${c})`;
        ctx.font = `${note.size}px serif`;
        ctx.textAlign = 'center';
        ctx.translate(note.x, note.y);
        ctx.rotate(note.rot);
        ctx.fillText(note.sym, 0, 0);
        ctx.restore();
      });
    };

    resize();
    window.addEventListener('resize', resize, { passive: true });
    addRAFTask(draw);
  })();

  /* =========================================
     DOM PARTICLES
  ========================================= */
  (() => {
    const particles = $('#particles');
    if (!particles || state.reducedMotion || state.isMobile) return;

    const symbols = ['♩', '♪', '♫', '♬'];

    for (let i = 0; i < 12; i++) {
      const p = doc.createElement('span');
      p.className = 'particle';
      p.textContent = symbols[i % symbols.length];
      p.style.left = `${Math.random() * 100}%`;
      p.style.animationDuration = `${12 + Math.random() * 16}s`;
      p.style.animationDelay = `${-Math.random() * 22}s`;
      p.style.fontSize = `${9 + Math.random() * 10}px`;
      p.style.opacity = `${(0.05 + Math.random() * 0.07).toFixed(3)}`;
      particles.appendChild(p);
    }
  })();

  /* =========================================
     SPARKS
  ========================================= */
  function spawnSparks(x, y, count = 8) {
    if (state.reducedMotion) return;

    const symbols = ['♪', '♫', '♬', '♩'];

    for (let i = 0; i < count; i++) {
      const el = doc.createElement('span');
      const angle = (i / count) * Math.PI * 2;
      const distance = 50 + Math.random() * 60;

      el.className = 'note-spark';
      el.textContent = symbols[i % symbols.length];
      el.style.cssText = `
        position: fixed;
        left: ${x}px;
        top: ${y}px;
        --sx: ${(Math.cos(angle) * distance).toFixed(1)}px;
        --sy: ${(Math.sin(angle) * distance).toFixed(1)}px;
        font-size: ${14 + Math.random() * 10}px;
        animation-duration: ${(0.6 + Math.random() * 0.4).toFixed(2)}s;
        pointer-events: none;
        z-index: 9999;
        color: var(--text);
      `;

      body.appendChild(el);
      el.addEventListener('animationend', () => el.remove(), { once: true });
    }
  }

  function spawnSparksFromElement(el, count = 8) {
    const rect = el.getBoundingClientRect();
    spawnSparks(rect.left + rect.width / 2, rect.top + rect.height / 2, count);
  }

  (() => {
    doc.addEventListener('click', (e) => {
      spawnSparks(e.clientX, e.clientY, 5);
    });
  })();

  /* =========================================
     FEATURE CARD TILT
  ========================================= */
  (() => {
    if (state.isTouch || state.reducedMotion) return;

    $$('.fc').forEach(card => {
      const glow = $('.fc-glow', card);

      card.addEventListener('mousemove', (e) => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const rotX = ((y - rect.height / 2) / (rect.height / 2)) * -6;
        const rotY = ((x - rect.width / 2) / (rect.width / 2)) * 6;

        card.style.transform = `
          perspective(900px)
          translateY(-5px)
          rotateX(${rotX}deg)
          rotateY(${rotY}deg)
          scale(1.02)
        `;

        if (glow) {
          glow.style.opacity = '1';
          glow.style.background = `
            radial-gradient(
              circle at ${(x / rect.width) * 100}% ${(y / rect.height) * 100}%,
              color-mix(in srgb, var(--text) 9%, transparent),
              transparent 55%
            )
          `;
        }
      });

      card.addEventListener('mouseleave', () => {
        card.style.transform = '';
        if (glow) glow.style.opacity = '0';
      });
    });
  })();

  /* =========================================
     DOWNLOAD CARD SHINE
  ========================================= */
  (() => {
    const card = $('.dl-card');
    const shine = $('.dl-card-shine');
    if (!card || !shine || state.isTouch) return;

    card.addEventListener('mousemove', (e) => {
      const rect = card.getBoundingClientRect();
      const x = ((e.clientX - rect.left) / rect.width) * 100;
      const y = ((e.clientY - rect.top) / rect.height) * 100;

      shine.style.background = `
        radial-gradient(
          circle at ${x}% ${y}%,
          color-mix(in srgb, var(--text) 7%, transparent),
          transparent 55%
        )
      `;
    });

    card.addEventListener('mouseleave', () => {
      shine.style.background =
        'linear-gradient(135deg, color-mix(in srgb, var(--text) 5%, transparent), transparent 40%)';
    });
  })();

  /* =========================================
     SCREENSHOT DRAG SCROLL
  ========================================= */
  (() => {
    const rail = $('#scrScroll');
    if (!rail) return;

    let isDown = false;
    let startX = 0;
    let startScroll = 0;

    rail.addEventListener('mousedown', (e) => {
      isDown = true;
      startX = e.pageX - rail.offsetLeft;
      startScroll = rail.scrollLeft;
      rail.style.userSelect = 'none';
      rail.style.cursor = 'grabbing';
    });

    rail.addEventListener('mousemove', (e) => {
      if (!isDown) return;
      e.preventDefault();
      const x = e.pageX - rail.offsetLeft;
      const walk = (x - startX) * 1.6;
      rail.scrollLeft = startScroll - walk;
    });

    ['mouseleave', 'mouseup'].forEach(type => {
      rail.addEventListener(type, () => {
        isDown = false;
        rail.style.userSelect = '';
        rail.style.cursor = 'grab';
      });
    });
  })();

  /* =========================================
     HERO PARALLAX
  ========================================= */
  (() => {
    const phones = $('.hero-phones');
    if (!phones || state.reducedMotion || state.isMobile) return;

    let ticking = false;

    const update = () => {
      if (window.scrollY < window.innerHeight) {
        phones.style.transform = `translateY(${window.scrollY * 0.1}px)`;
      }
      ticking = false;
    };

    window.addEventListener('scroll', () => {
      if (ticking) return;
      ticking = true;
      requestAnimationFrame(update);
    }, { passive: true });
  })();

  /* =========================================
     SMOOTH ANCHOR LINKS
  ========================================= */
  (() => {
    $$('a[href^="#"]').forEach(link => {
      link.addEventListener('click', (e) => {
        const href = link.getAttribute('href');
        const target = href ? $(href) : null;
        if (!target) return;

        e.preventDefault();
        target.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      });
    });
  })();

  /* =========================================
     MARQUEE TOUCH PAUSE
  ========================================= */
  (() => {
    const wrap = $('.marquee-wrap');
    const track = $('.marquee-track');
    if (!wrap || !track) return;

    wrap.addEventListener('touchstart', () => {
      track.style.animationPlayState = 'paused';
    }, { passive: true });

    wrap.addEventListener('touchend', () => {
      track.style.animationPlayState = 'running';
    }, { passive: true });
  })();

  /* =========================================
     LOAD BURST
  ========================================= */
  window.addEventListener('load', () => {
    if (state.reducedMotion) return;
    window.setTimeout(() => {
      spawnSparks(window.innerWidth / 2, window.innerHeight / 2, 12);
    }, 700);
  });
})();
