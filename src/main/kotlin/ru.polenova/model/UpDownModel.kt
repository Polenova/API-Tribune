package ru.polenova.model

import java.time.LocalDateTime

data class UpDownModel (
    val date: LocalDateTime, val user: AuthUserModel, val upDown: UpDown
        )
enum class UpDown {
    UP,
    DOWN
}