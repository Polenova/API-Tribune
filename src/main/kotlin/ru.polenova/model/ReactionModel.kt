package ru.polenova.model

import java.time.LocalDateTime

data class ReactionModel (
    val date: LocalDateTime, val user: AuthUserModel, val reaction: Reaction
        )
enum class Reaction {
    UP,
    DOWN
}