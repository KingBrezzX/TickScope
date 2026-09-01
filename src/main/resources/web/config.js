(() => {
  const api = localStorage.getItem("tickscope_api") || "";
  const token = localStorage.getItem("tickscope_token") || "";
  window.TICKSCOPE_CONFIG = { apiBase: api.replace(/\/$/, ""), token };
})();