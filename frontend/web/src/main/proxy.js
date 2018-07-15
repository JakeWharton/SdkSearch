addEventListener('fetch', event => {
  event.respondWith(fetch("https://developer.android.com/_s/getsuggestions?hl=en&p=%2F&s=irina&c=3"));
})
