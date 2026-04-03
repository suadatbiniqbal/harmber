const express = require('express');
const fetch = require('node-fetch');
const path = require('path');

const app = express();
const PORT = 3000;

const API_KEY = 'AIzaSyBXZxLTFUmiFRliRRcEuV1Baxmzq8JiZ_0';

// serve frontend
app.use(express.static(path.join(__dirname, '../public')));

// OG preview route
app.get('/watch', async (req, res) => {
  const id = req.query.v;

  if (!id) return res.send('No video ID');

  try {
    const yt = await fetch(
      `https://www.googleapis.com/youtube/v3/videos?part=snippet&id=${id}&key=${API_KEY}`
    );
    const data = await yt.json();

    if (!data.items.length) return res.send('Video not found');

    const v = data.items[0].snippet;

    const title = v.title;
    const desc  = v.description.slice(0, 150);
    const thumb = v.thumbnails.high.url;

    res.send(`
<!DOCTYPE html>
<html>
<head>
  <title>${title}</title>

  <!-- OG tags -->
  <meta property="og:title" content="${title}" />
  <meta property="og:description" content="${desc}" />
  <meta property="og:image" content="${thumb}" />
  <meta property="og:url" content="http://localhost:3000/watch?v=${id}" />
  <meta name="twitter:card" content="summary_large_image" />

  <!-- redirect to frontend -->
  <script>
    window.location.href = "/watch.html?v=${id}";
  </script>
</head>
<body></body>
</html>
    `);

  } catch (err) {
    res.send('Error fetching video');
  }
});

// start server
app.listen(PORT, () => {
  console.log("Server running → http://localhost:" + PORT);
});
