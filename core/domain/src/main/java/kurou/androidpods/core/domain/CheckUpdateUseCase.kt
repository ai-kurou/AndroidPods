package kurou.androidpods.core.domain

import javax.inject.Inject

class CheckUpdateUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    /**
     * currentVersion より新しいバージョンが存在すれば true を返す。
     * 取得失敗やパース不能の場合は false を返す（サイレント失敗）。
     *
     * @param currentVersion "0.1.0" 形式の文字列
     */
    suspend operator fun invoke(currentVersion: String): Boolean {
        val tagName = repository.fetchLatestTagName() ?: return false
        val latestVersion = tagName.trimStart('v')
        return isNewerVersion(latestVersion, currentVersion)
    }

    private fun isNewerVersion(latest: String, current: String): Boolean = try {
        val l = latest.split(".").map { it.toInt() }
        val c = current.split(".").map { it.toInt() }
        val size = maxOf(l.size, c.size)
        for (i in 0 until size) {
            val diff = l.getOrElse(i) { 0 } - c.getOrElse(i) { 0 }
            if (diff != 0) return diff > 0
        }
        false
    } catch (e: NumberFormatException) {
        false
    }
}
