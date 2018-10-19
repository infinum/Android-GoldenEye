package co.infinum.goldeneye.utils

internal object IncompatibleDevicesUtils {

    fun isIncompatibleDevice(model: String) = Device.values().flatMap { it.models }.find { it.equals(model, false) } != null

    enum class Device(
        val models: List<String>
    ) {
        ONE_PLUS_6(listOf("oneplus a6000", "oneplus a6003"))
    }
}