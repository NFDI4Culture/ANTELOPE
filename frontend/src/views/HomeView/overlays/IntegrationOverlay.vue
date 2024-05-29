<template>
  <h2>Integration</h2>
  <p>ANTELOPE can be used in various ways. You may:</p>
  <ul>
    <li>Use this website directly for your search</li>
    <li>Embed the antelope search form in your website (see below)</li>
    <li>Connect to our HTTP API</li>
  </ul>
  <h3>Via iframe</h3>
  <p>
    Copy the code below and place it on your website or web-based software. This will display the ANTELOPE frontend component within your
    site.
  </p>
  <pre><code>&lt;iframe 
  src="#" 
  height="600" width="800" name="antelope" sandbox="allow-scripts"&gt;
&lt;/iframe&gt;</code></pre>
  <p>
    You may send search requests to the component from your own site. After your users select entities withinthe ANTELOPE component, the
    results are available in your site for further use.
    <a
      href="https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/-/blob/main/frontend/examples/iframe/index.html"
      target="_blank"
      >View Embed documentation</a
    >.
  </p>
  <h3>Via HTTP API</h3>
  <p>
    You can use the full functionality of this website via our API.
    <a href="./api.htm" target="_blank">View HTTP API documentation</a>.
  </p>
</template>
