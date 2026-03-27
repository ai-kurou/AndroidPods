---
name: add-repository
description: "Repositoryの追加を依頼されたとき（例: '〇〇Repositoryを作って'、'データ永続化の層を追加して'）に、クリーンアーキテクチャのDIパターンに従ってインターフェース・実装・Hiltバインディング・UseCaseを一括で作成する"
---

# Add Repository

新しいRepositoryをクリーンアーキテクチャのDIパターンに従って追加する。

## 引数

$ARGUMENTS に Repository名と概要を指定する（例: "WeatherRepository 天気情報の取得"）。

## 手順

以下の手順をすべて実行すること:

### 1. core:domain にインターフェースを作成

パス: `core/domain/src/main/java/kurou/androidpods/core/domain/{Name}Repository.kt`

```kotlin
package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface {Name}Repository {
    // ユーザーの要件に応じてメソッドを定義
}
```

- データの観測には `Flow<T>` を使用する
- 単発の操作には `suspend fun` を使用する
- Android Framework への依存は禁止

### 2. core:data に実装クラスを作成

パス: `core/data/src/main/java/kurou/androidpods/core/data/{Name}RepositoryImpl.kt`

```kotlin
package kurou.androidpods.core.data

import kurou.androidpods.core.domain.{Name}Repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class {Name}RepositoryImpl @Inject constructor(
    // 必要な依存を注入
) : {Name}Repository {
    // インターフェースの実装
}
```

- `@Singleton` スコープで作成
- `internal` 可視性にする
- `@Inject constructor` でコンストラクタインジェクション
- `MutableStateFlow` を private に持ち、公開は `StateFlow` / `Flow` で行う

### 3. DataModule に @Binds メソッドを追加

パス: `core/data/src/main/java/kurou/androidpods/core/data/DataModule.kt`

既存の `DataModule` に以下を追加:

```kotlin
@Binds
internal abstract fun bind{Name}Repository(
    impl: {Name}RepositoryImpl,
): {Name}Repository
```

import も忘れずに追加する。

### 4. 必要に応じて UseCase を作成

Repository を直接 ViewModel に注入せず、UseCase を経由するのがこのプロジェクトのパターン。

パス: `core/domain/src/main/java/kurou/androidpods/core/domain/{Action}{Name}UseCase.kt`

```kotlin
package kurou.androidpods.core.domain

import javax.inject.Inject

class {Action}{Name}UseCase @Inject constructor(
    private val repository: {Name}Repository,
) {
    // repository のメソッドを呼び出す
}
```

### 5. ビルド確認

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :core:domain:compileDebugKotlin :core:data:compileDebugKotlin
```

ビルドが通ることを確認する。
