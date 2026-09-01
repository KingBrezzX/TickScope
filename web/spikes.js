async function loadSpikes() {

    const container =
        document.getElementById("spikes");

    if (!container) {
        return;
    }

    container.innerHTML =
        '<div class="empty">Loading spikes...</div>';

    try {

        const response =
            await fetch(
                API + "/api/spikes",
                {
                    headers: headers()
                }
            );

        if (!response.ok) {
            throw new Error(
                "HTTP " + response.status
            );
        }

        const spikes =
            await response.json();

        if (!spikes.length) {

            container.innerHTML =
                '<div class="empty">' +
                'No performance spikes detected.' +
                '</div>';

            return;
        }

        container.innerHTML =
            spikes
                .slice()
                .reverse()
                .slice(0, 50)
                .map(spike => {

                    const date =
                        new Date(
                            spike.timestamp
                        );

                    return `
                        <div class="item">

                            <strong>
                                ${spike.severity}
                            </strong>

                            <small>

                                Time:
                                ${date.toLocaleTimeString()}

                                <br>

                                TPS:
                                ${Number(
                                    spike.tps
                                ).toFixed(2)}

                                <br>

                                MSPT:
                                ${Number(
                                    spike.mspt
                                ).toFixed(1)}

                            </small>

                        </div>
                    `;

                })
                .join("");

    } catch (error) {

        console.error(error);

        container.innerHTML =
            '<div class="empty">' +
            'Unable to load spike history.' +
            '</div>';
    }
}


async function loadUptime() {

    const element =
        document.getElementById(
            "uptime"
        );

    if (!element) {
        return;
    }

    try {

        const data =
            await getJson(
                "/api/uptime"
            );

        element.textContent =
            data.formatted || "--";

    } catch (error) {

        element.textContent =
            "--";
    }
}


document.addEventListener(
    "DOMContentLoaded",
    () => {

        loadSpikes();
        loadUptime();

        setInterval(
            loadSpikes,
            5000
        );

        setInterval(
            loadUptime,
            5000
        );

    }
);
