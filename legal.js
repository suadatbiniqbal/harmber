'use strict';

/* CURSOR */
(function(){
  const dot  = document.getElementById('cursorDot');
  const ring = document.getElementById('cursorRing');
  if(!dot||!ring) return;
  let rx=0,ry=0,mx=0,my=0;
  document.addEventListener('mousemove',e=>{ mx=e.clientX; my=e.clientY; });
  document.addEventListener('mouseleave',()=>{ dot.style.opacity='0'; ring.style.opacity='0'; });
  document.addEventListener('mouseenter',()=>{ dot.style.opacity='1'; ring.style.opacity='1'; });
  (function loop(){
    rx+=(mx-rx)*.13; ry+=(my-ry)*.13;
    dot.style.left  = mx+'px'; dot.style.top  = my+'px';
    ring.style.left = rx+'px'; ring.style.top = ry+'px';
    requestAnimationFrame(loop);
  })();
  document.querySelectorAll('a,button,.no-item,.contact-row,.tp-row,.tl,.btn-ghost-sm').forEach(el=>{
    el.addEventListener('mouseenter',()=>ring.classList.add('hover'));
    el.addEventListener('mouseleave',()=>ring.classList.remove('hover'));
  });
})();

/* NAV SHADOW ON SCROLL */
(function(){
  const nav = document.getElementById('nav');
  if(!nav) return;
  window.addEventListener('scroll',()=>{
    nav.classList.toggle('scrolled', window.scrollY > 20);
  },{ passive:true });
})();

/* ACTIVE TOC LINK ON SCROLL */
(function(){
  const links    = document.querySelectorAll('.tl');
  const sections = document.querySelectorAll('.ls[id]');
  if(!links.length||!sections.length) return;
  const obs = new IntersectionObserver(entries=>{
    entries.forEach(e=>{
      if(!e.isIntersecting) return;
      links.forEach(l=> l.classList.toggle('active', l.getAttribute('href')==='#'+e.target.id));
    });
  },{ rootMargin:'-28% 0px -62% 0px' });
  sections.forEach(s=>obs.observe(s));
})();

/* HOVER TILT ON NO-ITEMS */
(function(){
  document.querySelectorAll('.no-item').forEach(el=>{
    el.addEventListener('mousemove',e=>{
      const r=el.getBoundingClientRect();
      const x=((e.clientX-r.left)/r.width-.5)*14;
      const y=((e.clientY-r.top)/r.height-.5)*-14;
      el.style.transform=`perspective(600px) rotateX(${y}deg) rotateY(${x}deg) translateY(-2px)`;
    });
    el.addEventListener('mouseleave',()=>{ el.style.transform=''; });
  });
})();

/* SMOOTH SCROLL FOR TOC LINKS */
(function(){
  document.querySelectorAll('.tl[href^="#"]').forEach(link=>{
    link.addEventListener('click',e=>{
      e.preventDefault();
      const target = document.querySelector(link.getAttribute('href'));
      if(!target) return;
      window.scrollTo({ top: target.getBoundingClientRect().top + window.scrollY - 100, behavior:'smooth' });
    });
  });
})();
