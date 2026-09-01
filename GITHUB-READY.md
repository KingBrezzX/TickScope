# TickScope — GitHub Ready

## What this package provides

- Java 25 Maven build for Paper 26.2
- JAR artifact upload from GitHub Actions
- Paper 26.2 runtime smoke test
- Static web deployment to GitHub Pages
- Public-web client architecture using API URL + single TickScope key
- Local API and public API are separate concerns

## Important networking rule

GitHub Pages is a static frontend. It does not run the Minecraft plugin.

The plugin must be running on the Minecraft/Paper server. The public web connects to the plugin through an HTTPS-reachable API endpoint.

Example:

`GitHub Pages -> https://api.example.com -> TickScope -> Minecraft`

Do not put a production API key into a public repository.

## GitHub setup

1. Create a repository.
2. Upload the contents of this package to the repository root.
3. Push to `main`.
4. Open **Actions**.
5. Wait for **TickScope CI** and **Deploy Web to GitHub Pages**.
6. Only treat the release as production-ready when both workflows are green.
7. Download the JAR from the successful **TickScope-java25-paper26.2** artifact and install it into `plugins/`.

## Release gate

The CI intentionally fails if:

- Java 25 compilation/package fails
- no JAR is produced
- `plugin.yml` is missing
- Paper 26.2 cannot start
- TickScope cannot be enabled
- TickScope causes a startup exception

The runtime test is performed on a temporary test server, not the production server.
