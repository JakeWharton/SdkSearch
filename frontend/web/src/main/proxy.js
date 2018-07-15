addEventListener('fetch', event => {
  event.respondWith(proxy(event.request));
})

async function proxy(request) {
  const referer = request.headers.get('Referer');
  if (!referer || new URL(referer).hostname !== 'sdksearch.app') {
    // Only proxy requests from the app.
    return new Response('404 Not Found', { status: 404 });
  }

  const newUrl = new URL(request.url);
  newUrl.host = 'developer.android.com'

  const newRequest = new Request(newUrl, {
    method: request.method,
    headers: request.headers,
  });
  const response = await fetch(newRequest);

  const newHeaders = new Headers(response.headers);
  newHeaders.append('Access-Control-Allow-Origin', 'https://sdksearch.app');

  return new Response(response.body, {
    status: response.status,
    statusText: response.statusText,
    headers: newHeaders,
  });
}
