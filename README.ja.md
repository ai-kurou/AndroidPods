# AndroidPods

[English](README.md)

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

AirPods などの Apple 製 Bluetooth イヤホンの電池残量を、Android のシステムオーバーレイでリアルタイムに表示するアプリ。

<img width="300" src="https://github.com/user-attachments/assets/f3771fc9-1e4a-42a7-bc4d-5b1fc887360e" /><img width="300" src="https://github.com/user-attachments/assets/a3cb83ea-e09f-43f0-80c5-ee907df0dab0" />

## 機能

- BLE スキャンによる互換デバイスの自動検出
- システムオーバーレイによるリアルタイム電池残量表示
- Foreground Service によるバックグラウンド監視
- テーマ（Light / Dark / System）とオーバーレイ表示位置のカスタマイズ
- 検出済みデバイスの一覧表示
- 初回起動時のセットアップウィザード
- クラッシュが発生した場合、改善のためにクラッシュレポートを Firebase Crashlytics に自動送信

## 動作要件

- Android 9.0 以降
- Bluetooth LE 対応端末
- Apple 製 Bluetooth イヤホン（AirPods など）
- Bluetooth / 位置情報 / 通知 / 他のアプリの上に重ねて表示する権限

## インストール

[Releases](https://github.com/ai-kurou/AndroidPods/releases) から最新の APK をダウンロードしてインストールしてください。

初回起動時に、Bluetooth スキャン、通知、オーバーレイ表示に必要な権限を案内します。バックグラウンドで電池残量を監視するには、必要に応じてバッテリー最適化の対象外にしてください。

## Contributing

このプロジェクトはプルリクエストを受け付けていません。
[GPL-3.0 ライセンス](LICENSE) の範囲内で自由にフォーク・改変・再配布できます。

## ライセンス

[GPL-3.0](LICENSE)

<!-- MODULE-GRAPH-START -->
## Module Graph

![Module Graph](docs/graphs/full-graph.svg)
<!-- MODULE-GRAPH-END -->

## Test Coverage Graph

![Test Coverage Graph](https://codecov.io/github/ai-kurou/AndroidPods/graphs/icicle.svg?token=0B62HU1W46)

## アーキテクチャ

Kotlin + Jetpack Compose による Clean Architecture のマルチモジュール構成。

```
:app -> :navigation -> :feature:* -> :core:domain <- :core:data
```

| モジュール | 役割 |
|---|---|
| `:app` | アプリのエントリーポイント、テーマ適用、Hilt DI グラフ構築 |
| `:navigation` | Navigation Compose による全画面ルートの一元管理 |
| `:core:domain` | Repository インターフェース、UseCase、通知チャンネル定義 |
| `:core:data` | Repository 実装、DataStore、Hilt DI モジュール |
| `:core:service` | BLE スキャン、Foreground Service、オーバーレイ通知 |
| `:core:designsystem` | テーマ、カラー、タイポグラフィ |
| `:feature:settings` | 設定画面 |
| `:feature:onboarding` | 初回セットアップウィザード |
| `:feature:devices` | 検出済みデバイス一覧画面 |
| `:feature:licenses` | OSS ライセンス一覧画面 |
| `:feature:widget` | ウィジェット関連機能 |
