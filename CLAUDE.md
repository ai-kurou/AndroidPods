# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

Android モバイルアプリケーションのプロジェクト。**パブリックリポジトリ**のため、コミット・プッシュされた内容はすべて公開される。

## ビルド・テスト

システムにJavaがインストールされていない場合、Android Studio同梱のJBRを使用する:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

```bash
# 全モジュールビルド
./gradlew assembleDebug

# 特定モジュールのビルド
./gradlew :core:data:compileDebugKotlin

# 全ユニットテスト実行
./gradlew testDebugUnitTest

# 特定モジュールのテスト
./gradlew :feature:devices:testDebugUnitTest

# 特定テストクラスの実行
./gradlew :app:testDebugUnitTest --tests "kurou.androidpods.MainViewModelTest"
```

## アーキテクチャ

マルチモジュール構成のクリーンアーキテクチャ。依存方向: `app` → `feature:*` → `core:domain` ← `core:data`

- **`:core:domain`** — リポジトリのインターフェースとUseCase。Android Framework非依存。
- **`:core:data`** — リポジトリの実装とHilt DIモジュール(`DataModule`)。`@Binds`でインターフェースと実装をバインド。
- **`:core:service`** — `DeviceScanService`（Foreground Service）でBLEスキャンとカスタム通知を管理。`:core:domain`に依存。通知はRemoteViewsで構築（Composeは使用不可）。
- **`:feature:*`** — 各画面のViewModel, Composable, テスト。`:core:domain`(`:core:data`含む)に依存。
- **`:app`** — `MainActivity`でNavigation Composeによるルーティング、`MainViewModel`で初回起動判定。`:core:domain`(UseCase利用)と`:core:data`(Hilt DIグラフ構築)の両方に依存。

### DI パターン

Hiltを使用。新しいRepositoryを追加する場合:
1. `core:domain` にインターフェースを定義
2. `core:data` に `@Singleton` 実装を作成
3. `core:data/DataModule.kt` に `@Binds` メソッドを追加

### テストパターン

- **ViewModel テスト**: MockKでUseCaseをモック、`UnconfinedTestDispatcher`で`Dispatchers.Main`を差し替え
- **Repository テスト**: Robolectric (`@Config(sdk = [34])`) でAndroid APIをシミュレート
- **Service テスト**: Hilt + Robolectric。`@UninstallModules(DataModule::class)`でFakeモジュールに差し替え、`Robolectric.buildService()`でServiceControllerを取得。ロジックは`internal fun`として抽出しユニットテスト可能にする
- **Compose UIテスト**: `createComposeRule()` + Robolectricでユニットテストとして実行
- テスト名は日本語のバッククォート記法 (`` `初期状態はnullを返す`() ``)

## Security Rules

### 自動スキャン

`git commit` / `git push` 実行前に、Claude Code Hook（`.claude/scripts/scan-secrets.sh`）が差分を自動スキャンし、秘匿情報のパターンを検出した場合はブロックする。

### 対応方針

- 秘匿情報は環境変数または `local.properties` 経由で注入し、コードにハードコードしない
- `.gitignore` に秘匿ファイルが含まれていることを確認する
- 自動スキャンで検出できないケース（個人情報、社内URL、内部IPなど）は目視確認すること
- 万が一秘匿情報がコミットされた場合は、履歴からの除去（`git filter-branch` 等）と該当キーの無効化・ローテーションを行う
