package ru.polenova.service

import io.ktor.features.*
import io.ktor.util.*
import ru.polenova.dto.PostRequestDto
import ru.polenova.dto.PostResponseDto
import ru.polenova.exception.UserAccessException
import ru.polenova.model.AuthUserModel
import ru.polenova.model.PostModel
import ru.polenova.repository.PostRepository
import java.time.LocalDateTime

class ServicePost (private val repo: PostRepository) {

    suspend fun getAllPosts(idUser: Long, userService: UserService): List<PostResponseDto> {
        return repo.getAllPosts().map { PostResponseDto.fromModel(it, userService, idUser) }
    }

    @KtorExperimentalAPI
    suspend fun getRecent(idUser: Long, userService: UserService) =
        repo.getRecent().map { PostResponseDto.fromModel(it, userService, idUser) }


    @KtorExperimentalAPI
    suspend fun save(input: PostRequestDto, idUser: Long, userService: UserService) {
        val checkReadOnly = userService.checkReadOnly(idUser, this)
        if (checkReadOnly) {
            throw UserAccessException("Read Only mode")
        } else {
            val date = LocalDateTime.now()
            val model = PostModel(
                idPost = 0L,
                dateOfCreate = date,
                postName = input.postName,
                postText = input.postText,
                link = input.link,
                idUser = idUser
                /*postUpCount = 0,
            postDownCount = 0,
            pressedPostDown = false,
            pressedPostUp = false,*/
                //attachment = input.attachmentId?.let { MediaModel(id = it) }

            )
            userService.addPostId(idUser, repo.savePost(model).idPost)
        }
    }

    @KtorExperimentalAPI
    suspend fun getByIdPost(idPost: Long) = repo.getByIdPost(idPost) ?: throw NotFoundException()


    @KtorExperimentalAPI
    suspend fun getPostsBefore(idPost: Long, idUser: Long, userService: UserService): List<PostResponseDto> {
        val listPostsBefore = repo.getPostsBefore(idPost) ?: throw NotFoundException()
        return listPostsBefore.map { PostResponseDto.fromModel(it, userService, idUser) }
    }

    @KtorExperimentalAPI
    suspend fun removePostByIdPost(idPost: Long, me: AuthUserModel): Boolean {
        val model = repo.getByIdPost(idPost) ?: throw NotFoundException()
        return if (model.user == me) {
            repo.removePostByIdPost(idPost)
            true
        } else {
            false
        }
    }

    @KtorExperimentalAPI
    suspend fun upById(idPost: Long, idUser: Long, userService: UserService): PostResponseDto {
        if (getByIdPost(idPost).upUserIdMap.contains(idUser)||getByIdPost(idPost).downUserIdMap.contains(idUser)) {
            throw UserAccessException("You are have reaction of this post")
        } else {
            val post = repo.upById(idPost, idUser)?: throw NotFoundException()
            val userPost = userService.getByIdUser(post.idUser)
            val user = userService.getByIdUser(idUser)
            val postResponseDto = PostResponseDto.fromModel(post, userService, idUser)
            userService.addUp(idUser)
            return postResponseDto
        }

    }

    @KtorExperimentalAPI
    suspend fun downById(idPost: Long, idUser: Long, userService: UserService): PostResponseDto {
        if (getByIdPost(idPost).downUserIdMap.contains(idUser)||getByIdPost(idPost).upUserIdMap.contains(idUser)) {
            throw UserAccessException("You are have reaction of this post")
        }
        val post = repo.downById(idPost, idUser)?: throw NotFoundException()
        val userPost = userService.getByIdUser(post.idUser)
        val user = userService.getByIdUser(idUser)
        val postResponseDto =  PostResponseDto.fromModel(post, userService, idUser)
        userService.addDown(idUser)
        return postResponseDto
    }

    @KtorExperimentalAPI
    suspend fun getUserPosts(idUser: Long, userService: UserService) =
        repo.getUserPosts(idUser).map { PostResponseDto.fromModel(it, userService, idUser) }

}