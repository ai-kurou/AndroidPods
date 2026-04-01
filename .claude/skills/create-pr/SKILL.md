---
name: create-pr
description: "PRの作成を依頼されたとき（例: 'PRを作って'、'PRにして'）に、現在のブランチの変更をコミット・プッシュしてGitHub PRを作成する"
---

# Create Pull Request

現在のブランチの変更からPull Requestを作成する。

## 引数

$ARGUMENTS にPRの補足情報があれば使用する（省略可）。

## 手順

### 1. 状態確認

```bash
git status --short
git diff --stat
git diff --cached --stat
git log --oneline -5
```

### 2. コミット（未コミットの変更がある場合）

- 変更内容を分析し、日本語で簡潔なコミットメッセージを作成
- .DS_Store、.env、credentials等の秘匿ファイルはステージしない

### 3. プッシュ

```bash
git push -u origin <current-branch>
```

### 4. PR作成

`gh` CLIが利用可能な場合:
```bash
gh pr create --title "<タイトル>" --body "<本文>"
```

`gh` が利用不可の場合:
- `https://github.com/ai-kurou/AndroidPods/pull/new/<branch>` のURLを提示
- タイトルとBody案をMarkdownで提示

### PRフォーマット

```markdown
## Summary
- 変更内容を箇条書き

## Test plan
- [ ] テスト項目をチェックリストで記載
```
