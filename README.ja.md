
# AndroidPods

[English](README.md)

[![Unit Test](https://github.com/ai-kurou/AndroidPods/actions/workflows/on-main-merge.yml/badge.svg)](https://github.com/ai-kurou/AndroidPods/actions/workflows/on-main-merge.yml)
[![codecov](https://codecov.io/github/ai-kurou/AndroidPods/graph/badge.svg?token=0B62HU1W46)](https://codecov.io/github/ai-kurou/AndroidPods)
[![Maintainability](https://qlty.sh/gh/ai-kurou/projects/AndroidPods/maintainability.svg)](https://qlty.sh/gh/ai-kurou/projects/AndroidPods)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a05213d7b70f4578a9fc43cab1b4190c)](https://app.codacy.com/gh/ai-kurou/AndroidPods/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ai-kurou_AndroidPods&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ai-kurou_AndroidPods)
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/ai-kurou/AndroidPods?utm_source=oss&utm_medium=github&utm_campaign=ai-kurou%2FAndroidPods&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)
[![License](https://img.shields.io/github/license/ai-kurou/AndroidPods)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/ai-kurou/AndroidPods)](https://github.com/ai-kurou/AndroidPods/releases)
![Android](https://img.shields.io/badge/Android-API%2028%2B-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin)

AirPodsなどのApple製Bluetoothイヤホンの電池残量を、Androidのシステムオーバーレイでリアルタイム表示するアプリです。

## Screenshots

<img width="300" src="https://github.com/user-attachments/assets/f3771fc9-1e4a-42a7-bc4d-5b1fc887360e" /><img width="300" src="https://github.com/user-attachments/assets/a3cb83ea-e09f-43f0-80c5-ee907df0dab0" />

## Features

- BLEスキャンによる互換デバイスの自動検出
- システムオーバーレイによるリアルタイム電池残量表示
- Foreground Serviceでのバックグラウンド監視
- テーマ（Light / Dark / System）とオーバーレイ位置のカスタマイズ
- 初回セットアップウィザード

## Requirements

- Android 9 (API 28) 以上
- Bluetooth LE対応端末
- Apple製Bluetoothイヤホン（AirPodsなど）

## Architecture

Kotlin + Jetpack Composeによるマルチモジュール構成のClean Architecture。

```
:app → :navigation → :feature:* → :core:domain ← :core:data
```

| モジュール | 役割 |
|---|---|
| `:app` | エントリーポイント |
| `:navigation` | 全ナビゲーションルートの一元管理 |
| `:core:domain` | リポジトリインターフェース・UseCase |
| `:core:data` | リポジトリ実装・Hilt DIモジュール |
| `:core:service` | BLEスキャンとオーバーレイ通知（Foreground Service） |
| `:core:designsystem` | テーマ・カラー・タイポグラフィ |
| `:feature:devices` | デバイス一覧画面 |
| `:feature:settings` | 設定画面 |
| `:feature:onboarding` | 初回セットアップウィザード |
| `:feature:licenses` | OSSライセンス一覧 |

## Contributing

このプロジェクトはプルリクエストを受け付けていません。  
[GPL-3.0 ライセンス](LICENSE)の条件のもと、自由にフォーク・改変・再配布できます。

## License

[GPL-3.0](LICENSE)

<!-- MODULE-GRAPH-START -->
## Module Graph

![Module Graph](docs/graphs/full-graph.svg)
<!-- MODULE-GRAPH-END -->
