package br.com.zup.william.registra

data class ContaResponse(
        val id: String,
        val nome: String,
        val cpf: String,
        val instituicao: InstituicaoResponse
) {

    fun toModel(): Conta {
        return Conta(
                nome = this.nome,
                cpf = this.cpf,
                nomeInstituicao = this.instituicao.nome,
                ispb = this.instituicao.ispb
        )
    }
}
data class InstituicaoResponse(val nome: String, val ispb:String)


