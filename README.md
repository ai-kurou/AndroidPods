# AndroidPods

[日本語版はこちら](README.ja.md)

[![On Main Merge](https://github.com/ai-kurou/AndroidPods/actions/workflows/on-main-merge.yml/badge.svg?branch=main)](https://github.com/ai-kurou/AndroidPods/actions/workflows/on-main-merge.yml)
[![codecov](https://codecov.io/github/ai-kurou/AndroidPods/graph/badge.svg?token=0B62HU1W46)](https://codecov.io/github/ai-kurou/AndroidPods)
[![Maintainability](https://qlty.sh/gh/ai-kurou/projects/AndroidPods/maintainability.svg)](https://qlty.sh/gh/ai-kurou/projects/AndroidPods)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a05213d7b70f4578a9fc43cab1b4190c)](https://app.codacy.com/gh/ai-kurou/AndroidPods/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ai-kurou_AndroidPods&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ai-kurou_AndroidPods)
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/ai-kurou/AndroidPods?utm_source=oss&utm_medium=github&utm_campaign=ai-kurou%2FAndroidPods&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)
[![License](https://img.shields.io/github/license/ai-kurou/AndroidPods)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/ai-kurou/AndroidPods)](https://github.com/ai-kurou/AndroidPods/releases)
![Android](https://img.shields.io/badge/Android-API%2028%2B-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin)

An Android app that displays the battery levels of Apple Bluetooth earphones such as AirPods in real time using a system overlay.

<img width="300" src="https://github.com/user-attachments/assets/f3771fc9-1e4a-42a7-bc4d-5b1fc887360e" /><img width="300" src="https://github.com/user-attachments/assets/a3cb83ea-e09f-43f0-80c5-ee907df0dab0" />

## Features

- Automatically detects compatible devices using BLE scanning
- Displays battery levels in real time using a system overlay
- Monitors devices in the background with a Foreground Service
- Customizes the theme (Light / Dark / System) and overlay position
- Shows a list of detected devices
- Guides first-time setup with an onboarding wizard
- Automatically sends crash reports to Firebase Crashlytics to help improve the app

## Requirements

- Android 9.0 or later
- Bluetooth LE compatible Android device
- Apple Bluetooth earphones, such as AirPods
- Bluetooth, location, notification, and display-over-other-apps permissions

## Installation

Download and install the latest APK from [Releases](https://github.com/ai-kurou/AndroidPods/releases).

On first launch, AndroidPods guides you through the permissions required for Bluetooth scanning, notifications, and overlay display. To monitor battery levels in the background, exclude the app from battery optimization if needed.

## Contributing

This project does not accept pull requests.
You are free to fork, modify, and redistribute this project under the terms of the [GPL-3.0 license](LICENSE).

## License

[GPL-3.0](LICENSE)

<!-- MODULE-GRAPH-START -->
## Module Graph

![Module Graph](docs/graphs/full-graph.svg)
<!-- MODULE-GRAPH-END -->

## Test Coverage Graph

![Test Coverage Graph](https://codecov.io/github/ai-kurou/AndroidPods/graphs/icicle.svg?token=0B62HU1W46)

## Architecture

Kotlin + Jetpack Compose multi-module Clean Architecture.

```
:app -> :navigation -> :feature:* -> :core:domain <- :core:data
```

| Module | Role |
|---|---|
| `:app` | App entry point, theme application, and Hilt DI graph setup |
| `:navigation` | Centralized screen route management with Navigation Compose |
| `:core:domain` | Repository interfaces, UseCases, and notification channel definitions |
| `:core:data` | Repository implementations, DataStore, and Hilt DI module |
| `:core:service` | BLE scanning, Foreground Service, and overlay notification |
| `:core:designsystem` | Theme, colors, and typography |
| `:feature:settings` | Settings screen |
| `:feature:onboarding` | First-time setup wizard |
| `:feature:devices` | Detected devices screen |
| `:feature:licenses` | OSS licenses screen |
| `:feature:widget` | Widget-related features |
