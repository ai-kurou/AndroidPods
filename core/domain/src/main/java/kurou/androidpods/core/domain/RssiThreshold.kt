package kurou.androidpods.core.domain

enum class RssiThreshold(val minRssi: Int) {
    ALL(Int.MIN_VALUE),
    MEDIUM(-75),
    NEAR(-65),
    VERY_NEAR(-55),
}
