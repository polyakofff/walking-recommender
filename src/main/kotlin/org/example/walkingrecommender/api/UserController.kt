package org.example.walkingrecommender.api

import org.example.walkingrecommender.api.dto.CreateUserRequest
import org.example.walkingrecommender.api.dto.SavePreferencesRequest
import org.example.walkingrecommender.api.dto.UserDto
import org.example.walkingrecommender.api.dto.toDto
import org.example.walkingrecommender.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    val userService: UserService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun create(@RequestBody request: CreateUserRequest): UserDto {
        logger.info("create user: {}", request)

        return userService.getOrCreate(request.username).toDto()
    }

    @GetMapping("/{username}")
    fun get(@PathVariable username: String): UserDto {
        logger.info("get user: $username")

        return userService.getOrCreate(username).toDto()
    }

    @PutMapping("/{username}/preferences")
    fun savePreferences(
        @PathVariable username: String,
        @RequestBody request: SavePreferencesRequest
    ): UserDto {
        logger.info("save preferences: {} for {}", request, username)

        return userService.savePreferences(username, request.answers).toDto()
    }
}