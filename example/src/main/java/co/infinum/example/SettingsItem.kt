package co.infinum.example

data class SettingsItem(
    val name: String,
    val value: String,
    val onClick: () -> Unit
)