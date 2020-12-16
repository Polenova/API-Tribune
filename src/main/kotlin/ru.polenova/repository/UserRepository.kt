package ru.polenova.repository

import ru.polenova.model.StatusUser
import ru.polenova.model.AuthUserModel
import ru.polenova.service.ServicePost

interface UserRepository {
    suspend fun getAllPostsUser(): List<AuthUserModel>
    suspend fun getByIdUser(idUser: Long): AuthUserModel?
    suspend fun getByIdPassword(idUser: Long, password: String): AuthUserModel?
    suspend fun getByUsername(username: String): AuthUserModel?
    suspend fun getByUserStatus(user: AuthUserModel): StatusUser
    suspend fun getByIds(ids: Collection<Long>): List<AuthUserModel>
    suspend fun save(item: AuthUserModel): AuthUserModel
    suspend fun checkReadOnly(userId: Long, postService: ServicePost): Boolean
}