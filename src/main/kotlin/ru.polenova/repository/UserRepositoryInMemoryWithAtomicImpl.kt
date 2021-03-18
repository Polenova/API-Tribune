package ru.polenova.repository

import io.ktor.util.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.polenova.model.*
import ru.polenova.service.ServicePost

class UserRepositoryInMemoryWithAtomicImpl : UserRepository {
    private var nextId = atomic(0L)
    private val items = mutableListOf<AuthUserModel>()
    private val mutex = Mutex()

    override suspend fun getAllPostsUser(): List<AuthUserModel> = items.toList()

    override suspend fun getByIdUser(idUser: Long): AuthUserModel? = items.find { it.idUser == idUser }

    override suspend fun getByIdPassword(idUser: Long, password: String): AuthUserModel? {
        val item = items.find { it.idUser == idUser }
        return if (password == item?.password) {
            item
        } else {
            null
        }
    }

    override suspend fun getByUsername(username: String): AuthUserModel? = items.find { it.username == username }

    override suspend fun getByUserStatus(user: AuthUserModel): StatusUser {
        TODO("Not yet implemented")
    }


    override suspend fun getByIds(ids: Collection<Long>): List<AuthUserModel> {
        TODO("Not yet implemented")
    }

    override suspend fun addUp(idUser: Long): AuthUserModel? {
        return when (val index = items.indexOfFirst { it.idUser == idUser }) {
            -1 -> {
                null
            }
            else -> {
                mutex.withLock {
                    items[index].up++
                }
                items[index]
            }
        }
    }

    override suspend fun addDown(idUser: Long): AuthUserModel? {
        return when (val index = items.indexOfFirst { it.idUser == idUser}) {
            -1 -> {
                null
            }
            else -> {
                mutex.withLock {
                    items[index].down++
                }
                items[index]
            }
        }
    }

    override suspend fun save(item: AuthUserModel): AuthUserModel {
        return when (val index = items.indexOfFirst { it.idUser== item.idUser }) {
            -1 -> {
                val copy = item.copy(idUser = nextId.incrementAndGet())
                mutex.withLock {
                    items.add(copy)
                }
                copy
            }
            else -> {
                val copy = items[index].copy(username = item.username, password = item.password)
                mutex.withLock {
                    items[index] = copy
                }
                copy
            }
        }
    }
    @KtorExperimentalAPI
    override suspend fun checkReadOnly(idUser: Long, postService: ServicePost): Boolean {
        val index = items.indexOfFirst { it.idUser == idUser }
        items[index].userPostsId.forEach {
            val post = postService.getByIdPost(it)
            if (post.downUserIdMap.size >= 5 && post.upUserIdMap.isEmpty()) {         // > 100
                if (!items[index].readOnly) {
                    mutex.withLock {
                        items[index].readOnly = true
                    }
                }
                return true
            } else {
                if (items[index].readOnly) {
                    mutex.withLock {
                        items[index].readOnly = false
                    }
                }
            }
        }
        return items[index].readOnly
    }

    override suspend fun addPostId(user: AuthUserModel, idPost: Long) {
        val index = items.indexOfFirst { it.idUser == user.idUser }
        mutex.withLock {
            items[index].userPostsId.add(idPost)
        }
    }

    override suspend fun listUsersReaction(post: PostModel): List<ReactionModel> {
        val listUsers = mutableListOf<ReactionModel>()
        post.upUserIdMap.forEach {
            val user = getByIdUser(it.key)
            if (user != null) {
                listUsers.add(ReactionModel(it.value, user, Reaction.UP))
            }
        }
        post.downUserIdMap.forEach {
            val user = getByIdUser(it.key)
            if (user != null) {
                listUsers.add(ReactionModel(it.value, user, Reaction.DOWN))
            }
        }
        return listUsers.sortedWith(compareBy { it.date }).reversed()    }
}