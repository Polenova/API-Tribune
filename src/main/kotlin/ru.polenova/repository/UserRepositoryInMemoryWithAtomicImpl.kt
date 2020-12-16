package ru.polenova.repository

import io.ktor.network.selector.SelectInterest.Companion.size
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.polenova.model.StatusUser
import ru.polenova.model.AuthUserModel
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

    override suspend fun checkReadOnly(idUser: Long, postService: ServicePost): Boolean {
        val index = items.indexOfFirst { it.idUser == idUser }
        items[index].userPostsId.forEach {
            val post = postService.getByIdPost(it)
            if (post.downUserIdList.size >= 5 && post.upUserIdList.isEmpty()) {         // > 100
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
        return items[index].readOnly    }

}