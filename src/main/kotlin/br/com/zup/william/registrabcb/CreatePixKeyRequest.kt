package br.com.zup.william.registrabcb

import br.com.zup.william.registra.ChavePix
import br.com.zup.william.registra.TipoDeConta

data class CreateKeyPixRequest(
        val keyType: KeyType,
        val key: String,
        val bankAccount: BankAccount,
        val owner: Owner

) {

    companion object {
        fun of(chavePix: ChavePix): CreateKeyPixRequest {
            return CreateKeyPixRequest(
                    keyType = KeyType.by(chavePix.tipoDeChave),
                    chavePix.valorDaChave,
                    bankAccount = BankAccount(
                            chavePix.conta.ispb,
                            chavePix.conta.agencia,
                            chavePix.conta.numero,
                            accountType = BankAccount.AccountType.by(chavePix.tipoDeConta)
                    ), owner = Owner(
                    type = TipoDePessoa.NATURAL_PERSON,
                    chavePix.conta.nome,
                    chavePix.conta.cpf
            )
            )
        }
    }
}

data class BankAccount(
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: AccountType
){
    enum class AccountType() {

        CACC,
        SVGS;

        companion object {
            fun by(domainType: TipoDeConta): AccountType {
                return when (domainType) {
                    TipoDeConta.CONTA_CORRENTE -> CACC
                    TipoDeConta.CONTA_POUPANCA -> SVGS
                }
            }
        }
    }
}

data class Owner(
        val type: TipoDePessoa,
        val name: String,
        val taxIdNumber: String
)