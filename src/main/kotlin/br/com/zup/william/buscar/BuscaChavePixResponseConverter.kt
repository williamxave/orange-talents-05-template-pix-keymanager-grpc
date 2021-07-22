package br.com.zup.william.buscar

import br.com.zup.william.BuscarChavePixResponse
import br.com.zup.william.TipoDeChave
import br.com.zup.william.TipoDeConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class BuscaChavePixResponseConverter {
    fun convert(chaveInfo: ChavePixInfo?): BuscarChavePixResponse {
        return BuscarChavePixResponse.newBuilder()
                .setClienteId(chaveInfo?.clienteId ?: "") // Protobuf usa "" como default value para String
                .setPixId(chaveInfo?.pixId ?: "") // Protobuf usa "" como default value para String
                .setChave(BuscarChavePixResponse.ChavePix
                        .newBuilder()
                        .setTipoDaChave(TipoDeChave.valueOf(chaveInfo?.tipo!!.name))
                        .setChave(chaveInfo?.chave)
                        .setConta(BuscarChavePixResponse.ChavePix.ContaInfo.newBuilder()
                                .setTipo(TipoDeConta.valueOf(chaveInfo.tipoDeConta!!.name))
                                .setInstituicao(chaveInfo.conta!!.nomeInstituicao)
                                .setNomeDoTitular(chaveInfo.conta.nome)
                                .setCpfDoTitular(chaveInfo.conta.cpf)
                                .setAgencia(chaveInfo.conta.agencia)
                                .setNumeroDaConta(chaveInfo.conta.numero)
                                .build()
                        )
                        .setCriadaEm(chaveInfo.registradaEm.let {
                            val createdAt = it?.atZone(ZoneId.of("UTC"))?.toInstant()
                            Timestamp.newBuilder()
                                    .setSeconds(createdAt!!.epochSecond)
                                    .setNanos(createdAt.nano)
                                    .build()
                        })
                )
                .build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }


}