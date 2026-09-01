function disconnectServer() {
  localStorage.removeItem("tickscope_api");
  localStorage.removeItem("tickscope_token");
  location.href = "connect.html";
}