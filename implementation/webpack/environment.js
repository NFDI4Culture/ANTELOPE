module.exports = {
  I18N_HASH: 'generated_hash',
  SERVER_API_URL: '', //options.env === 'development' ? "http://localhost:8080/" : "http://nfdi4cultureann21.service.tib.eu:8080/",
  __VERSION__: process.env.hasOwnProperty('APP_VERSION') ? process.env.APP_VERSION : 'DEV',
  __DEBUG_INFO_ENABLED__: false,
};
