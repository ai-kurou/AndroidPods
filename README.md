
# AndroidPods

[日本語版はこちら](README.ja.md)

[![Unit Test](https://github.com/ai-kurou/AndroidPods/actions/workflows/on-main-merge.yml/badge.svg)](https://github.com/ai-kurou/AndroidPods/actions/workflows/on-main-merge.yml)
[![codecov](https://codecov.io/github/ai-kurou/AndroidPods/graph/badge.svg?token=0B62HU1W46)](https://codecov.io/github/ai-kurou/AndroidPods)
[![Maintainability](https://qlty.sh/gh/ai-kurou/projects/AndroidPods/maintainability.svg)](https://qlty.sh/gh/ai-kurou/projects/AndroidPods)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a05213d7b70f4578a9fc43cab1b4190c)](https://app.codacy.com/gh/ai-kurou/AndroidPods/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ai-kurou_AndroidPods&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ai-kurou_AndroidPods)
[![License](https://img.shields.io/github/license/ai-kurou/AndroidPods)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/ai-kurou/AndroidPods)](https://github.com/ai-kurou/AndroidPods/releases)
![Android](https://img.shields.io/badge/Android-API%2028%2B-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin)

An Android app that displays real-time battery levels of Apple Bluetooth earphones (AirPods, etc.) via a system overlay.

## Screenshots

<img width="400" src="https://github.com/user-attachments/assets/a3cb83ea-e09f-43f0-80c5-ee907df0dab0" />

## Features

- Automatic detection of compatible devices via BLE scan
- Real-time battery level display via system overlay
- Background monitoring with Foreground Service
- Customizable theme (Light / Dark / System) and overlay position
- First-time setup wizard

## Requirements

- Android 9 (API 28) or later
- Bluetooth LE compatible device
- Apple Bluetooth earphones (AirPods, etc.)

## Architecture

Clean Architecture with multi-module structure using Kotlin + Jetpack Compose.

```
:app → :navigation → :feature:* → :core:domain ← :core:data
```

| Module | Role |
|---|---|
| `:app` | Entry point |
| `:navigation` | Centralized navigation route management |
| `:core:domain` | Repository interfaces & UseCases |
| `:core:data` | Repository implementations & Hilt DI module |
| `:core:service` | BLE scan & overlay notification (Foreground Service) |
| `:core:designsystem` | Theme, colors & typography |
| `:feature:devices` | Device list screen |
| `:feature:settings` | Settings screen |
| `:feature:onboarding` | First-time setup wizard |
| `:feature:licenses` | OSS license list |

## Contributing

This project does not accept pull requests.  
You are free to fork, modify, and redistribute this project under the terms of the [GPL-3.0 license](LICENSE).

## License

[GPL-3.0](LICENSE)

<!-- MODULE-GRAPH-START -->
## Module Graph

![Module Graph](docs/graphs/full-graph.svg)
<!-- MODULE-GRAPH-END -->
