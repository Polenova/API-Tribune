package ru.polenova.repository

import ru.polenova.model.PostModel

interface PostRepository {
    suspend fun getAllPosts(): List<PostModel>
    suspend fun getByIdPost(idPost: Long): PostModel?
    suspend fun savePost(item: PostModel): PostModel
    suspend fun removePostByIdPost(idPost: Long)
    suspend fun upById(idPost: Long, idUser: Long): PostModel?
    suspend fun downById(idPost: Long, idUser: Long): PostModel?
    suspend fun getRecent(): List<PostModel>
    suspend fun getPostsBefore(idPost: Long): List<PostModel>?
    suspend fun getUserPosts(idUser: Long): List<PostModel>

}