package br.com.zup.william.registrabcb

import br.com.zup.william.registra.TipoDeConta

enum class TipoDaContaBCB(val tipo: TipoDeConta) {
    CACC(TipoDeConta.CONTA_CORRENTE),
    SVGS(TipoDeConta.CONTA_POUPANCA);

    // Mapeamos nosso enum ERP ITAU para o enum do tipo de conta do BCB, assim tempos meio que um
    // par(CACC -> CONTA_CORRENTE), isso é feito com o associateBy, fazendo uma referencia
    // Assim quando criarmos uma conta, ela sera criada com CONTA_CORRENTE ou POUPANCA,
    // Mas para mandar os dados para BCB precisamos mandar CACC ou SVGS, é oque esse método estatico faz
    // mapeia os campos e retorna os campos validas, ou retorna uma exception se o campo nao existir
    //para essa validacao foi usada o operador elvis
    //É salvo no banco da primeira forma, só precisamos mapear pq o sistema externo só recebe dessa forma
    companion object {
        private val mapping = TipoDaContaBCB.values().associateBy(TipoDaContaBCB::tipo)
        fun by(tipo: TipoDeConta): TipoDaContaBCB {
            return mapping[tipo] ?: throw  IllegalArgumentException("Tipo de chave não existe")
        }
    }
}