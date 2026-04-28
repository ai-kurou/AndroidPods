package kurou.androidpods.core.domain

interface UpdateRepository {
    /** GitHub Releases の最新 tag_name を返す。ネットワーク失敗時は Result.failure。 */
    suspend fun fetchLatestTagName(): Result<String>
}
