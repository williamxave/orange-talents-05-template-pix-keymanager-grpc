package br.com.zup.william.registrabcb

import java.time.LocalDateTime

class CreatePixKeyResponse(
        val keyType: String,
        val key: String,
        val bankAccount: BankAccountResponse,
        val owner: OwnerResponse,
        val createdAt: LocalDateTime
)

data class BankAccountResponse(
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: String
)

data class OwnerResponse(
        val type: String,
        val name: String,
        val taxIdNumber: String
)