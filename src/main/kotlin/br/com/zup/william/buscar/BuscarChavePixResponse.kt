package br.com.zup.william.buscar

import br.com.zup.william.registra.Conta
import br.com.zup.william.registra.TipoDeConta.CONTA_CORRENTE
import br.com.zup.william.registra.TipoDeConta.CONTA_POUPANCA
import br.com.zup.william.registrabcb.BankAccount
import br.com.zup.william.registrabcb.KeyType
import br.com.zup.william.registrabcb.Owner
import java.time.LocalDateTime

data class BuscarChavePixResponse(
        val keyType: KeyType,
        val key: String,
        val bankAccount: BankAccount,
        val owner: Owner,
        val createdAt: LocalDateTime?
) {

    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
                tipo = keyType.tipo!!,
                chave = this.key,
                tipoDeConta = when (this.bankAccount.accountType) {
                    BankAccount.AccountType.SVGS -> CONTA_CORRENTE
                    BankAccount.AccountType.CACC -> CONTA_POUPANCA
                },
                conta = Conta(
                        agencia = bankAccount.participant,
                        nome = owner.name,
                        cpf = owner.taxIdNumber,
                        ispb = bankAccount.branch,
                        numero = bankAccount.accountNumber,
                        nomeInstituicao = ""
                ),
                registradaEm = createdAt
        )
    }
}

