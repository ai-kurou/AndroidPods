package kurou.androidpods.core.domain

interface UpdateRepository {
    /** GitHub Releases の最新 tag_name を返す。失敗時は null。 */
    suspend fun fetchLatestTagName(): String?
}
