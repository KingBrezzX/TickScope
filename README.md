# TickScope

**Advanced Minecraft Server Performance Monitor & Lag Analyzer**

Monitor your Minecraft server in real time, analyze performance issues, detect lag hotspots, track history, and get intelligent recommendations.

## Features

- Real-time TPS monitoring
- Real-time MSPT monitoring
- Tick monitoring and spike detection
- Player count monitoring
- Loaded chunk monitoring
- Entity analysis
- Tile entity analysis
- Redstone activity analysis
- Lag chunk detection
- Accurate hotspot coordinates
- Nearby player detection
- World information
- Server uptime
- Performance history
- Automatic history cleanup
- Intelligent performance recommendations
- Real-time web dashboard
- Localhost dashboard
- Secure web API
- Random server authentication token
- GitHub Pages dashboard support

## Requirements

- Paper 26.2
- Java 25

## Author

**KingBrezz**

## License

License information will be added before the first public release.


## Release build

TickScope targets Java 25 and Paper 26.2. The repository includes GitHub Actions workflows that install Java 25, resolve the Paper API, build the shaded JAR, validate the web assets, and upload the JAR as an artifact. GitHub's `setup-java` supports Java 25 and Maven caching, and Paper's current developer setup documents Java 25 with the 26.2 API.

The server generates a unique `plugins/TickScope/server.token` on first startup. Use `/token` to display it. Do not commit that file or paste the real token into GitHub. The GitHub Pages dashboard stores the token only in browser local storage.
