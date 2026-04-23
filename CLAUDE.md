# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

Android モバイルアプリケーションのプロジェクト。**パブリックリポジトリ**のため、コミット・プッシュされた内容はすべて公開される。

## ライブラリバージョン管理

`libs.versions.toml` にライブラリを追加するときは、**致命的なバグや互換性問題がない限り、その時点の最新安定版を使用すること**。追加前に必ず公式リリースページで最新バージョンを確認する。

## Git 操作ルール

**コミット・プッシュ・PRの作成はユーザーが明示的に指示した場合のみ実行すること。** 自発的にコミット・プッシュ・PRの作成を行うことは禁止。

## 作業完了前のルール

コードを変更・追加したら、**完了報告の前に必ず該当モジュールのユニットテストを実行すること**。

```bash
# 変更したモジュールのテストを実行（例: feature:settings を変更した場合）
./gradlew :feature:settings:testDebugUnitTest
```

複数モジュールを変更・追加した場合はそれぞれのテストを実行する。テストが失敗した場合は修正してからレポートする。

また、ファイルを新規追加した場合は、**そのファイルに対応するユニットテストも必要であれば追加すること**。

さらに、**Detektの静的解析も実行して指摘がないことを確認すること**。

```bash
# 変更したモジュールのDetektを実行（例: feature:settings を変更した場合）
./gradlew :feature:settings:detekt

# 複数モジュールを変更した場合は全体で実行
./gradlew detekt
```

Detektで指摘がある場合は修正してからレポートする。

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
./gradlew :feature:settings:testDebugUnitTest

# 特定テストクラスの実行
./gradlew :app:testDebugUnitTest --tests "kurou.androidpods.MainViewModelTest"

# スクリーンショットを記録（初回・UI変更後に実行してgitにコミット）
./gradlew recordRoborazziDebug

# スクリーンショットを検証（CIと同等）
./gradlew verifyRoborazziDebug

# モジュール間の依存ルールを検証
./gradlew assertModuleGraph

# 各モジュールのREADME.mdにSVG依存グラフを生成・更新（Graphviz要）
./gradlew generateModuleGraphImages
```

## アーキテクチャ

マルチモジュール構成のクリーンアーキテクチャ。依存方向: `app` → `navigation` → `feature:*` → `core:domain` ← `core:data`

- **`:core:domain`** — リポジトリのインターフェースとUseCase。Android Framework非依存。
- **`:core:data`** — リポジトリの実装とHilt DIモジュール(`DataModule`)。`@Binds`でインターフェースと実装をバインド。
- **`:core:service`** — `DeviceScanService`（Foreground Service）でBLEスキャンとカスタム通知を管理。`:core:domain`に依存。通知はRemoteViewsで構築（Composeは使用不可）。
- **`:navigation`** — `AppScaffold.kt`が`NavHost`の全ルートを管理。ルート定数は`private object Route`として同ファイル内に定義。新しい画面追加時はこのファイルのみ編集する。
- **`:feature:*`** — 各画面のViewModel, Composable, テスト。`:core:domain`(`:core:data`含む)に依存。
- **`:app`** — `MainActivity`でNavigation Composeによるルーティング、`MainViewModel`で初回起動判定。`:core:domain`(UseCase利用)と`:core:data`(Hilt DIグラフ構築)の両方に依存。

### ナビゲーションフロー

```
初回起動: OnboardingScreen → (完了後) SettingsScreen
2回目以降: SettingsScreen (直接表示)
```

### ナビゲーション追加パターン

新しい画面を追加する場合、`navigation/src/main/java/kurou/androidpods/navigation/AppScaffold.kt` を編集する:
1. `private object Route` に定数を追加
2. `NavHost` に `composable(Route.{NAME}) { ... }` ブロックを追加
3. `navigation/build.gradle.kts` の依存に `implementation(project(":feature:{name}"))` を追加

### DI パターン

Hiltを使用。新しいRepositoryを追加する場合:
1. `core:domain` にインターフェースを定義
2. `core:data` に `@Singleton` 実装を作成
3. `core:data/DataModule.kt` に `@Binds` メソッドを追加

### テストパターン

- **ViewModel テスト**: MockKでUseCaseをモック、`UnconfinedTestDispatcher`で`Dispatchers.Main`を差し替え
- **Repository テスト**: Robolectric (`@Config(sdk = [35])`) でAndroid APIをシミュレート
- **Service テスト**: Hilt + Robolectric。`@UninstallModules(DataModule::class)`でFakeモジュールに差し替え、`Robolectric.buildService()`でServiceControllerを取得。ロジックは`internal fun`として抽出しユニットテスト可能にする
- **Compose UIテスト**: `createAndroidComposeRule<ComponentActivity>()` + Robolectricでユニットテストとして実行。Activityへのアクセス（`activityRule.scenario`、`onBackPressedDispatcher`など）が不要な場合は`createComposeRule()`でも可
- **スクリーンショットテスト（Roborazzi）**: `@GraphicsMode(GraphicsMode.Mode.NATIVE)` を付けて `captureRoboImage()` でスクリーンショットを取得。クラスに `@Config(qualifiers = "w360dp-h640dp-port-xxhdpi")` でデバイスサイズを固定する。横向きは `@Config(qualifiers = "w640dp-h360dp-land-xxhdpi")` をメソッドに付与。アニメーション・`HorizontalPager`など描画が不安定なComposableは `RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.05f))` で差分しきい値を設定する
- テストケース数は最小限に絞ること。カバレッジを確保しつつ、冗長なケースは省く
- テスト名は日本語のバッククォート記法 (`` `初期状態はnullを返す`() ``)
- 画面の向きは`@Config(qualifiers = "port")`または`@Config(qualifiers = "land")`で指定

#### Robolectric + Compose テストの注意点

- `HorizontalPager`内のノードは`assertIsDisplayed()`が失敗することがある（boundsチェックの問題）。代わりに`assertExists()`を使う
- 戻るボタンのシミュレーションは`composeTestRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }`で行う
- `RequestMultiplePermissions`ランチャーはRobolectricで自動的にコールバックを呼ばないため、権限拒否のシミュレーションは複雑になる

## カバレッジ

Koverでカバレッジを計測し、CIでCodecovにアップロードする。新しいモジュールを追加した場合、ルートの `build.gradle.kts` の `kover { }` ブロックに `kover(project(":module:name"))` を追加しないとカバレッジ集計から除外される。

```bash
# ローカルでカバレッジレポート生成（build/reports/kover/report.xml）
./gradlew koverXmlReport
```

## CI

PR時に `.github/workflows/pull-request.yml` が実行される:
- **module-graph-assert ジョブ**: `assertModuleGraph` でモジュール間の依存ルールを検証
- **unit-test ジョブ**: `koverXmlReport` → Codecov へアップロード
- **screenshot-test ジョブ**: `verifyRoborazziDebug` でスクリーンショットを比較。差分が出た場合は `**/build/outputs/roborazzi/*.png` をアーティファクトとしてアップロード
- **instrumented-test ジョブ**: Android エミュレータ（API 36）で `connectedDebugAndroidTest`

main へのマージ時に `.github/workflows/on-main-merge.yml` が実行される:
- **module-graph-assert ジョブ**: `assertModuleGraph` でモジュール間の依存ルールを検証
- **unit-test ジョブ**: `koverXmlReport` → Codecov へアップロード（`koverXmlReport` がコンパイル・テスト・カバレッジ計測を包含するため `assembleDebug` は不要）
- **instrumented-test ジョブ**: Android エミュレータ（API 36）で `connectedDebugAndroidTest`

スクリーンショットのベースラインは各モジュールの `src/test/snapshots/` に格納してgit管理する。UI変更後は `./gradlew recordRoborazziDebug` で再記録してコミットすること。

## 文字列リソース

`strings.xml` に文字列を追加・変更する場合、**必ず `values/`（英語）と `values-ja/`（日本語）の両方を同時に更新すること**。片方だけの追加は禁止。

- `values/strings.xml` — 英語（デフォルト）
- `values-ja/strings.xml` — 日本語

## Security Rules

### 自動スキャン

`git commit` / `git push` 実行前に、Claude Code Hook（`.claude/scripts/scan-secrets.sh`）が差分を自動スキャンし、秘匿情報のパターンを検出した場合はブロックする。

### 対応方針

- 秘匿情報は環境変数または `local.properties` 経由で注入し、コードにハードコードしない
- `.gitignore` に秘匿ファイルが含まれていることを確認する
- 自動スキャンで検出できないケース（個人情報、社内URL、内部IPなど）は目視確認すること
- 万が一秘匿情報がコミットされた場合は、履歴からの除去（`git filter-branch` 等）と該当キーの無効化・ローテーションを行う
