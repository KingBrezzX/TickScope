# TickScope Runtime Test

The authoritative release gate is `.github/workflows/runtime.yml`. It builds with Java 25, downloads Paper 26.2 build 121 from PaperMC, starts an isolated server, verifies plugin enablement, health, authentication, status/server APIs, SSE, and clean shutdown.

For local use, run Paper 26.2 with Java 25 and install the generated JAR in `plugins/`. Paper 26.2+ requires Java 25.
