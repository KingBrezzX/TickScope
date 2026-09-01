# Release checklist

- [ ] TickScope CI — green
- [ ] Java 25 build — green
- [ ] Paper 26.2 runtime smoke — green
- [ ] JAR artifact downloaded
- [ ] Test server API health — green
- [ ] API key authentication — green
- [ ] SSE realtime — green
- [ ] Local web — green
- [ ] GitHub Pages — green
- [ ] Public HTTPS API endpoint configured
- [ ] Production key is NOT committed to Git


## API route fix
- [x] `/api/v1/all` explicitly registered in the HTTP router
- [x] `/api/v1/mspt` compatibility endpoint added
- [x] `/api/v1/*` remains compatible with the existing API implementation
