package ru.polenova.repository

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.polenova.model.PostModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PostRepositoryInMemoryWithMutexImpl : PostRepository {

    private var nextId = atomic(0L)
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getAllPosts()  = items.sortedWith(compareBy { it.dateOfCreate }).reversed()

    override suspend fun getByIdPost(idPost: Long): PostModel? =
        items.find { it.idPost == idPost }

    override suspend fun savePost(item: PostModel): PostModel {
                val todayDate = LocalDateTime.now()
                val dateId = ZoneId.of("Europe/Moscow")
                val zonedDateTime = ZonedDateTime.of(todayDate, dateId)
                val copy = item.copy(idPost = nextId.incrementAndGet(), dateOfCreate = zonedDateTime)
                mutex.withLock {
                    items.add(copy)
                }
                return copy

    }

    override suspend fun removePostByIdPost(idPost: Long) {
        mutex.withLock {
            items.removeIf { it.idPost == idPost }
        }
    }

    override suspend fun upById(idPost: Long, idUser: Long): PostModel? {
        val index = items.indexOfFirst { it.idPost == idPost }
        if (index < 0) return null
        mutex.withLock {
            items[index].upUserIdMap.put(idUser, LocalDateTime.now())
        }
        return items[index]
    }

    override suspend fun disUpById(idPost: Long, idUser: Long): PostModel? {
        val index = items.indexOfFirst { it.idPost == idPost }
        if (index < 0) return null
        mutex.withLock {
            items[index].upUserIdMap.remove(idUser)
        }
        return items[index]
    }

    override suspend fun downById(idPost: Long, idUser: Long): PostModel? {
        val index = items.indexOfFirst { it.idPost == idPost }
        if (index < 0) return null
        mutex.withLock {
            items[index].downUserIdMap.put(idUser, LocalDateTime.now())
        }
        return items[index]
    }

    override suspend fun disDownById(idPost: Long, idUser: Long): PostModel? {
        val index = items.indexOfFirst { it.idPost == idPost }
        if (index < 0) return null
        mutex.withLock {
            items[index].downUserIdMap.remove(idUser)
        }
        return items[index]
    }

    override suspend fun getRecent(): List<PostModel> {
        try {
            if (items.isEmpty()) {
                return emptyList()
            }
            return getAllPosts().slice(0..4)
        } catch (e: IndexOutOfBoundsException) {
            return getAllPosts()
        }
    }

    override suspend fun getPostsAfter(idPost: Long): List<PostModel>? {
        val item = getByIdPost(idPost)
        val itemsReversed = getAllPosts()
        return when (val index = itemsReversed.indexOfFirst { it.idPost == item?.idPost }) {
            -1 -> null
            0 -> emptyList()
            else -> itemsReversed.slice(0 until index)
        }    }

    override suspend fun getPostsBefore(idPost: Long): List<PostModel>? {
        val item = getByIdPost(idPost)
        val itemsReversed = getAllPosts()
        return when (val index = itemsReversed.indexOfFirst { it.idPost == item?.idPost }) {
            -1-> null
            (items.size - 1) -> emptyList()
            else -> {
                try {
                    itemsReversed.slice((index + 1)..(index + 5))
                } catch (e: IndexOutOfBoundsException) {
                    itemsReversed.slice((index + 1) until items.size)
                }
            }
        }
    }
}