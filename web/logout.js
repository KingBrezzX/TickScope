function disconnectServer() {

    localStorage.removeItem(
        "tickscope_api"
    );

    localStorage.removeItem(
        "tickscope_token"
    );

    window.location.href =
        "connect.html";
}
