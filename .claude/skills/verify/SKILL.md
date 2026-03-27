---
name: verify
description: "ビルドやテストの実行を依頼されたとき（例: 'ビルドして'、'テスト通るか確認して'、'動作確認して'）に、プロジェクト全体のデバッグビルドとユニットテストを実行して結果を報告する"
---

# Verify

プロジェクト全体のビルドとユニットテストを実行し、問題がないことを確認する。

## 手順

### 1. デバッグビルド

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

ビルドが成功することを確認する。失敗した場合はエラー内容を報告する。

### 2. 全ユニットテスト実行

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew testDebugUnitTest
```

テストがすべてパスすることを確認する。失敗した場合はどのテストが失敗したかを報告する。

### 3. 結果報告

以下のフォーマットで結果を報告する:

- ビルド: SUCCESS / FAILED
- テスト: SUCCESS / FAILED（失敗テストがあればリストアップ）
- 失敗がある場合は原因の分析と修正案を提示する
