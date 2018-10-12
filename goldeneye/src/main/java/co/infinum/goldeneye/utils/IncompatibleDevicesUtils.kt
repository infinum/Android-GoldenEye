package co.infinum.goldeneye.utils

internal object IncompatibleDevicesUtils {

    fun isIncompatibleDevice(model: String) = Device.values().flatMap { it.models }.find { it.equals(model, false) } != null

    enum class Device(
        val models: List<String>
    ) {
        LG_G6(listOf("lg-h870", "lg-h870ds", "lg-g6+", "lg-h871", "lg-h872", "lg-h873", "lg-h870k", "lg-vs998", "lg-ls993", "lg-us997")),
        ONE_PLUS_6(listOf("oneplus a6000", "oneplus a6003"))
    }
}