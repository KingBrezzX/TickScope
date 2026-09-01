const API = "";

function setText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

function updateMetrics(data) {

    setText("tps", Number(data.tps).toFixed(2));
    setText("mspt", Number(data.mspt).toFixed(1));

    setText("players", data.players);
    setText("chunks", data.loadedChunks);
    setText("entities", data.entities);
    setText("tiles", data.tileEntities);

    const tps =
        Math.max(
            0,
            Math.min(
                100,
                Number(data.tps) / 20 * 100
            )
        );

    const mspt =
        Math.max(
            0,
            Math.min(
                100,
                Number(data.mspt) / 50 * 100
            )
        );

    document.getElementById(
        "tpsBar"
    ).style.width = tps + "%";

    document.getElementById(
        "msptBar"
    ).style.width = mspt + "%";

    let severity = "HEALTHY";

    if (data.mspt >= 100 || data.tps < 15) {
        severity = "CRITICAL";
    } else if (data.mspt >= 50 || data.tps < 18) {
        severity = "WARNING";
    }

    setText("severity", severity);
}

function connectRealtime() {

    const stream =
        new EventSource(
            API + "/api/stream"
        );

    stream.onopen = () => {

        setText(
            "connection",
            "Connected"
        );

        document.getElementById(
            "statusDot"
        ).style.background = "#ffffff";
    };

    stream.onmessage = event => {

        try {

            const data =
                JSON.parse(event.data);

            if (data.type === "metrics") {
                updateMetrics(data);
            }

        } catch (error) {

            console.error(
                "Invalid realtime data",
                error
            );
        }
    };

    stream.onerror = () => {

        setText(
            "connection",
            "Disconnected"
        );

        document.getElementById(
            "statusDot"
        ).style.background = "#777";
    };
}

async function getJson(endpoint) {

    const response =
        await fetch(
            API + endpoint
        );

    if (!response.ok) {
        throw new Error(
            "HTTP " + response.status
        );
    }

    return response.json();
}

async function loadHotspots() {

    const container =
        document.getElementById(
            "hotspots"
        );

    try {

        const data =
            await getJson(
                "/api/hotspots"
            );

        if (!data.length) {
            container.textContent =
                "No lag hotspots detected.";
            return;
        }

        container.innerHTML =
            data.slice(0, 20)
                .map(item => `
                    <div class="item">
                        <strong>
                            ${item.type}
                        </strong>

                        <small>
                            World: ${item.world}
                            <br>
                            Location:
                            ${item.x},
                            ${item.y},
                            ${item.z}
                            <br>
                            Score:
                            ${Number(item.score).toFixed(1)}
                        </small>
                    </div>
                `)
                .join("");

    } catch (error) {

        container.textContent =
            "Unable to load hotspots.";
    }
}

async function loadRedstone() {

    const container =
        document.getElementById(
            "redstone"
        );

    try {

        const data =
            await getJson(
                "/api/redstone"
            );

        if (!data.length) {
            container.textContent =
                "No redstone hotspots detected.";
            return;
        }

        container.innerHTML =
            data.slice(0, 20)
                .map(item => `
                    <div class="item">
                        <strong>
                            Redstone Activity
                        </strong>

                        <small>
                            World: ${item.world}
                            <br>
                            Location:
                            ${item.x},
                            ${item.y},
                            ${item.z}
                            <br>
                            Player:
                            ${item.player ?? "Unknown"}
                            <br>
                            Activity:
                            ${item.activity}
                        </small>
                    </div>
                `)
                .join("");

    } catch (error) {

        container.textContent =
            "Unable to load redstone data.";
    }
}

async function loadRecommendations() {

    const container =
        document.getElementById(
            "recommendations"
        );

    try {

        const data =
            await getJson(
                "/api/recommendations"
            );

        if (!data.length) {

            container.textContent =
                "No problems detected.";
            return;
        }

        container.innerHTML =
            data.map(item => `
                <div class="item">
                    <strong>
                        ${item.severity}
                        — ${item.cause}
                    </strong>

                    <small>
                        ${item.recommendation}
                        <br>
                        Confidence:
                        ${(item.confidence * 100)
                            .toFixed(0)}%
                    </small>
                </div>
            `).join("");

    } catch (error) {

        container.textContent =
            "Unable to analyze server.";
    }
}

connectRealtime();

loadHotspots();

loadRedstone();

loadRecommendations();
