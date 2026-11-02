package org.example.walkingrecommender.user.db

import org.example.walkingrecommender.user.User

interface UserRepository {

    fun find(username: String): User?

    fun save(user: User): User
}