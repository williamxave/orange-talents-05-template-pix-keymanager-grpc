package br.com.zup.william.remove

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class DeletePixKeyResponse(
        val key: String?,
        val participant: String?,
        val deletedAt: LocalDateTime?
)
