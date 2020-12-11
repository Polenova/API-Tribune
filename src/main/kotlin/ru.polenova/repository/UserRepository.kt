package ru.polenova.repository

import ru.polenova.model.StatusUser
import ru.polenova.model.AuthUserModel

interface UserRepository {
    suspend fun getAllPostsUser(): List<AuthUserModel>
    suspend fun getByIdUser(idUser: Long): AuthUserModel?
    suspend fun getByIdPassword(idUser: Long, password: String): AuthUserModel?
    suspend fun getByUsername(username: String): AuthUserModel?
    suspend fun getByUserStatus(useStatusUser: StatusUser): AuthUserModel?
    suspend fun getByIds(ids: Collection<Long>): List<AuthUserModel>
    suspend fun save(item: AuthUserModel): AuthUserModel
    //suspend fun saveFirebaseToken(idUser: Long, firebaseToken: String): AuthUserModel?
}