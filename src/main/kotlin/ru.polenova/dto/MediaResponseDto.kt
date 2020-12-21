package ru.polenova.dto

import ru.polenova.model.MediaModel

data class MediaResponseDto(val id: String) {
    companion object {
        fun fromModel(model: MediaModel) = MediaResponseDto(
            id = model.id
        )
    }
}