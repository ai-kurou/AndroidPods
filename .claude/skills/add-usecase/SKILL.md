---
name: add-usecase
description: "UseCaseの追加を依頼されたとき（例: '〇〇UseCaseを作って'、'UseCase追加して'）に、core:domainにUseCaseクラスを作成してViewModelに接続する"
---

# Add UseCase

新しいUseCaseを `core:domain` に追加し、必要に応じてViewModelに接続する。

## 引数

$ARGUMENTS に UseCase名と概要を指定する（例: "SaveOverlaySettingsUseCase オーバーレイ設定の保存"）。

## 手順

以下の手順をすべて実行すること:

### 1. 既存の Repository インターフェースを確認

`core/domain/src/main/java/kurou/androidpods/core/domain/` を読んで、UseCaseが使うRepositoryが存在するか確認する。

存在しない場合は `/add-repository` スキルで先に Repository を作成することをユーザーに案内する。

### 2. UseCaseクラスを作成

パス: `core/domain/src/main/java/kurou/androidpods/core/domain/{UseCaseName}.kt`

#### パターンA: データ観測系（Flowを返す）

```kotlin
package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class {UseCaseName} @Inject constructor(
    private val repository: {Name}Repository,
) {
    fun observe(): Flow<{Type}> = repository.observe{Data}()
}
```

#### パターンB: 単発操作系（suspend fun）

```kotlin
package kurou.androidpods.core.domain

import javax.inject.Inject

class {UseCaseName} @Inject constructor(
    private val repository: {Name}Repository,
) {
    suspend fun execute(/* 引数 */): {ReturnType} = repository.{method}(/* 引数 */)
}
```

#### パターンC: 混合系（観測 + アクション）

```kotlin
package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class {UseCaseName} @Inject constructor(
    private val repository: {Name}Repository,
) {
    fun observe(): Flow<{Type}> = repository.observe{Data}()
    fun {action}() = repository.{action}()
    suspend fun {suspendAction}() = repository.{suspendAction}()
}
```

ルール:
- `@Inject constructor` でHilt DIに参加させる
- `@Singleton` アノテーションは**不要**（UseCaseはスコープなし）
- Repositoryのメソッドに委譲するだけでよい。ロジックが必要な場合のみUseCasに書く
- Android Framework への依存は禁止

### 3. ViewModelへの接続（指示がある場合）

対象ViewModelのコンストラクタにUseCaseを追加し、StateFlowで公開する:

```kotlin
@HiltViewModel
class {Name}ViewModel @Inject constructor(
    private val {useCaseName}: {UseCaseName},
    // 既存の依存...
) : ViewModel() {

    private val _{stateField} = MutableStateFlow<{Type}?>(null)
    val {stateField}: StateFlow<{Type}?> = _{stateField}.asStateFlow()

    init {
        viewModelScope.launch {
            {useCaseName}.observe().collect { value ->
                _{stateField}.value = value
            }
        }
    }
}
```

### 4. ビルド確認

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :core:domain:compileDebugKotlin
```

ViewModelを変更した場合は該当featureモジュールも確認:

```bash
./gradlew :feature:{name}:compileDebugKotlin
```
