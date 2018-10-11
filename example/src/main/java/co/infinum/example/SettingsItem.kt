package co.infinum.example

data class SettingsItem(
    val name: String,
    val value: String? = null,
    val type: Int = 0,
    val onClick: (() -> Unit)? = null
)