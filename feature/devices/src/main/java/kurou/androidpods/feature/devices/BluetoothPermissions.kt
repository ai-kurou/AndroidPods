package kurou.androidpods.feature.devices

import android.Manifest
import android.os.Build

internal fun requiredPermissions(): List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    else
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
