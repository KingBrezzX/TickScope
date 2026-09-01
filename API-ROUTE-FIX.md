# TickScope API route fix

The previous web server rewrote `/api/v1/*` to `/api/*`, but `/api/v1/all`
was not handled by the switch, causing HTTP 404.

This build explicitly supports:
- GET /api/v1/all
- GET /api/v1/mspt
- GET /api/v1/health
- GET /api/v1/server
- GET /api/v1/uptime
- GET /api/v1/spikes
- GET /api/v1/hotspots
- GET /api/v1/redstone
- GET /api/v1/entities
- GET /api/v1/tile-entities
- GET /api/v1/recommendations
- GET /api/v1/stream

Authentication remains the existing TickScope token.
The token is not committed in this repository.
