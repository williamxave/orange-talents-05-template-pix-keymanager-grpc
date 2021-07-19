package br.com.zup.william.registra

data class ContaResponse(
        val tipo: TipoDeConta,
        val instituicao: Instituicao,
        val agencia: String,
        val numero: String,
        val titular: Titular
) {


    fun toModel(): Conta {
        return Conta(
                nome = titular.nome,
                cpf = titular.cpf,
                nomeInstituicao = this.instituicao.nome,
                ispb = this.instituicao.ispb,
                agencia,
                numero
        )
    }
}

data class Instituicao(val nome: String,
                               val ispb: String)

data class Titular(val id: String,
                   val nome: String,
                   val cpf: String)


