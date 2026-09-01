(function () {

    const savedApi =
        localStorage.getItem(
            "tickscope_api"
        );

    const savedToken =
        localStorage.getItem(
            "tickscope_token"
        );

    window.TICKSCOPE_CONFIG = {

        apiBase:
            savedApi || "",

        token:
            savedToken || ""

    };

})();
