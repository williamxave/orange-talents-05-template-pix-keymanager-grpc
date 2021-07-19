package br.com.zup.william.registrabcb

import br.com.zup.william.registra.ChavePix

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
                            accountType = TipoDaContaBCB.by(tipo = chavePix.tipoDeConta)
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
        val accountType: TipoDaContaBCB
)

data class Owner(
        val type: TipoDePessoa,
        val name: String,
        val taxIdNumber: String
)