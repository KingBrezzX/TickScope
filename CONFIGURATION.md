# TickScope Configuration

## Minecraft address

```yaml
server:
  ip: "business3.astrixhost.web.id"
  port: 5066
```

This identifies the Minecraft Java server shown in the dashboard.

## TickScope API address

```yaml
web:
  host: "0.0.0.0"
  port: 19132
```

This is independent from the Minecraft port.

For the current AstrixHost allocation shown by the user:

- Minecraft: `business3.astrixhost.web.id:5066`
- Candidate TickScope API allocation: `business3.astrixhost.web.id:19132`

Only use `19132` if no other service (for example Geyser) is already bound to it.

## Public HTTPS URL

```yaml
web:
  public-url: "https://api.example.com"
```

GitHub Pages needs an HTTPS-reachable API endpoint. `127.0.0.1` is only local to the device/server and is not a public API address.

## Single TickScope key

```yaml
web:
  authentication:
    enabled: true
    auto-generate-token: true
```

The generated `TS_...` key is stored in the server data directory and should never be committed to a public Git repository.

## CORS

For testing:

```yaml
cors:
  enabled: true
  allowed-origins:
    - "*"
```

For production, restrict this to the actual web origins.

## Everything is configurable

Thresholds, monitoring intervals, hotspot scan settings, history retention, recommendations, realtime interval, admin break radius, sign text, ban duration, ban reason, language, API host/port, public URL, CORS and authentication are all exposed through configuration.

Do not change a value to a port that another service already uses.
