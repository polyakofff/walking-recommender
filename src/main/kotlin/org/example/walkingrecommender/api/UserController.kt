package org.example.walkingrecommender.api

import org.example.walkingrecommender.api.dto.CreateUserRequest
import org.example.walkingrecommender.api.dto.SavePreferencesRequest
import org.example.walkingrecommender.api.dto.UserDto
import org.example.walkingrecommender.api.dto.toDto
import org.example.walkingrecommender.user.UserService
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

    @PostMapping
    fun create(@RequestBody request: CreateUserRequest): UserDto {
        println("create user: $request")

        return userService.getOrCreate(request.username).toDto()
    }

    @GetMapping("/{username}")
    fun get(@PathVariable username: String): UserDto {
        println("get user: $username")

        return userService.getOrCreate(username).toDto()
    }

    @PutMapping("/{username}/preferences")
    fun savePreferences(
        @PathVariable username: String,
        @RequestBody request: SavePreferencesRequest
    ): UserDto {
        println("save preferences: $request for $username")

        return userService.savePreferences(username, request.answers).toDto()
    }
}