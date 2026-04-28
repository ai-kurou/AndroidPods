package kurou.androidpods.core.domain

import javax.inject.Inject

class CheckUpdateUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    /**
     * currentVersion より新しいバージョンが存在すれば Result.success(true) を返す。
     * ネットワーク失敗やバージョン文字列のパース不能の場合は Result.failure。
     *
     * @param currentVersion "0.1.0" 形式の文字列
     */
    suspend operator fun invoke(currentVersion: String): Result<Boolean> =
        repository.fetchLatestTagName().mapCatching { tagName ->
            isNewerVersion(latest = tagName.trimStart('v'), current = currentVersion)
        }

    private fun isNewerVersion(
        latest: String,
        current: String,
    ): Boolean {
        val l = latest.split(".").map { it.toInt() }
        val c = current.split(".").map { it.toInt() }
        val size = maxOf(l.size, c.size)
        for (i in 0 until size) {
            val diff = l.getOrElse(i) { 0 } - c.getOrElse(i) { 0 }
            if (diff != 0) return diff > 0
        }
        return false
    }
}
