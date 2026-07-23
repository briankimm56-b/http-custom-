# HTTP Custom Clone

An Android VPN service application that tunnels HTTP requests through a custom SSH connection with support for custom headers and payloads.

## Features

- **Custom Host & Port**: Configure target server details
- **Custom Headers**: Add custom HTTP headers to requests
- **Custom Payload**: Send custom JSON/binary payloads
- **SSH Tunnel**: Establishes secure tunnel connection
- **Background Service**: Runs as an Android VPN service
- **Real-time Logging**: Monitor tunnel activity

## Project Structure

```
http-custom-clone/
├── app/
│   ├── src/main/
│   │   ├── java/com/yourname/httpcustomclone/
│   │   │   ├── MainActivity.kt              // UI: host, port, payload, headers
│   │   │   ├── HttpClient.kt               // OkHttp with custom headers/payload
│   │   │   └── TunnelVpnService.kt         // VPN service tunnel handler
│   │   ├── res/layout/
│   │   │   └── activity_main.xml           // Main activity layout
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle (root)
├── .gitignore
└── README.md
```

## Architecture

### MainActivity
Provides UI for:
- Host input
- Port configuration
- Custom payload entry
- Custom headers definition
- Start/Stop tunnel buttons
- Real-time log display

### HttpClient
OkHttp wrapper that:
- Sends HTTP requests with custom headers
- Supports binary payloads (application/octet-stream)
- Parses header strings into key-value maps
- Handles async callbacks with onSuccess/onError

### TunnelVpnService
Android VPN service that:
- Runs as system service
- Receives tunnel configuration from MainActivity
- Manages HTTP tunnel connections
- Handles errors and cleanup

## Getting Started

### Prerequisites
- Android Studio (latest)
- JDK 8+
- Android API 24 or higher

### Setup

1. Update package name from `com.yourname.httpcustomclone` to your desired package:
   ```bash
   git clone https://github.com/yourusername/http-custom-clone.git
   cd http-custom-clone
   ```

2. Open in Android Studio and sync Gradle

3. Update AndroidManifest.xml package name if needed

4. Build and run on Android device or emulator

## Configuration

### Input Fields
- **Host**: Target server hostname (e.g., example.com)
- **Port**: Target port number (e.g., 443)
- **Payload**: Custom request body or data to send
- **Headers**: Custom HTTP headers in format `key:value, key2:value2`

## Dependencies

- **Kotlin 1.9.0** - Modern Android development
- **OkHttp 4.12.0** - HTTP client with interceptors
- **Jetpack Compose 1.5.2** - Modern UI toolkit
- **Coroutines** - Async operations
- **Android API 24-34** - Wide device compatibility

## Permissions

Required permissions (auto-declared in AndroidManifest.xml):
- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.BIND_VPN_SERVICE`
- `android.permission.CHANGE_NETWORK_STATE`

## Usage

1. Launch the app
2. Enter target server details
3. Add custom headers if needed
4. Enter payload data
5. Tap "Start Tunnel" to begin
6. Monitor logs for activity
7. Tap "Stop Tunnel" to disconnect

## Future Enhancements

- [ ] SSH key authentication
- [ ] SSL/TLS certificate pinning
- [ ] Request/response logging to file
- [ ] Connection persistence
- [ ] Error recovery mechanisms
- [ ] SOCKS5 proxy support
- [ ] Data encryption

## License

MIT License - See LICENSE file for details

## Contributing

Pull requests are welcome. For major changes, please open an issue first.
