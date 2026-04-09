package kurou.androidpods.core.domain

interface CompatibleDeviceRepository {
    fun getCompatibleDevices(): List<CompatibleDevice>
}
