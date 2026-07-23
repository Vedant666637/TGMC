const http = require('http');

http.get('http://127.0.0.1:3000/health', (res) => {
  let data = '';
  res.on('data', (chunk) => data += chunk);
  res.on('end', () => console.log('Backend response:', data));
}).on('error', (err) => console.log('Backend is offline:', err.message));
