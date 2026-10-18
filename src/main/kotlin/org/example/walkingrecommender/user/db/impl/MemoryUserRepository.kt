package org.example.walkingrecommender.user.db.impl

import org.example.walkingrecommender.user.User
import org.example.walkingrecommender.user.db.UserRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class MemoryUserRepository : UserRepository {

    private val users = ConcurrentHashMap<String, User>()

    override fun find(username: String): User? =
        users[username]

    override fun save(user: User): User {
        users[user.username] = user
        return user
    }
}