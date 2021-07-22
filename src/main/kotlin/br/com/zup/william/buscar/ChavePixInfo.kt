package br.com.zup.william.buscar

import br.com.zup.william.registra.ChavePix
import br.com.zup.william.registra.Conta
import br.com.zup.william.registra.TipoDeChave
import br.com.zup.william.registra.TipoDeConta
import java.time.LocalDateTime

data class ChavePixInfo(
        val pixId: String?= null,
        val clienteId: String? = null,
        val tipo: TipoDeChave?,
        val chave: String?,
        val tipoDeConta: TipoDeConta?,
        val conta: Conta?,
        val registradaEm: LocalDateTime?
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                    pixId = chave.id,
                    clienteId = chave.clienteId,
                    tipo = chave.tipoDeChave,
                    chave = chave.valorDaChave,
                    tipoDeConta = chave.tipoDeConta,
                    conta = chave.conta,
                    registradaEm = chave.criadaEm
            )
        }
    }
}