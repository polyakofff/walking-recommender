package org.example.walkingrecommender.api

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ApiErrorResponse(
    val message: String,
)

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        e: Exception,
    ): ResponseEntity<ApiErrorResponse> {
        logger.error("Unexpected error", e)

        return ResponseEntity
            .internalServerError()
            .body(
                ApiErrorResponse(
                    message = "Internal server error"
                )
            )
    }
}