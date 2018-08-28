package co.infinum.example

data class ListItem<T>(
    val item: T,
    val convert: (T) -> String
)