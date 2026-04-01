---
name: add-vector-drawable
description: "SVGファイルをAndroid Vector Drawableに変換して追加するとき（例: 'SVGをdrawableに変換して'、'アイコンを追加して'）に、SVGをAndroid Vector Drawable XMLに変換してプロジェクトに配置する"
---

# Add Vector Drawable

SVGファイルをAndroid Vector Drawable XMLに変換してプロジェクトに追加する。

## 引数

$ARGUMENTS にSVGファイルのパスと配置先モジュールを指定する（例: "~/Downloads/icon.svg core:service"）。

## 手順

### 1. SVGファイルの読み取り

指定されたSVGファイルを読み取り、以下の要素を解析する:
- `viewBox` → `android:viewportWidth` / `android:viewportHeight`
- `width` / `height` → `android:width` / `android:height`（dp単位）
- `<path d="...">` → `android:pathData`
- `fill` → `android:fillColor`
- `stroke` → `android:strokeColor`
- `stroke-width` → `android:strokeWidth`

### 2. Vector Drawable XMLの生成

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{width}dp"
    android:height="{height}dp"
    android:viewportWidth="{viewportWidth}"
    android:viewportHeight="{viewportHeight}">

    <path
        android:pathData="{pathData}"
        android:fillColor="{fillColor}" />
</vector>
```

注意点:
- SVGの`transform`属性がある場合はpathDataに変換を適用する
- `<g>`グループ内の`fill`/`stroke`は子要素に継承する
- `<circle>`, `<rect>`, `<line>`等はpath表現に変換する
- 色指定が`none`の場合は該当属性を省略する

### 3. ファイルの配置

パス: `src/main/res/drawable/{filename}.xml`

- ファイル名はスネークケース（例: `icon_battery_null.xml`）
- PNGではなくVector Drawableなので`drawable`ディレクトリに配置（`mipmap`や`drawable-nodpi`ではない）

### 4. ビルド確認

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :{module}:assembleDebug
```
