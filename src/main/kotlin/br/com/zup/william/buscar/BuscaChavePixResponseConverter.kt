package br.com.zup.william.buscar

import br.com.zup.william.BuscarChavePixResponse
import com.google.protobuf.Timestamp
import java.time.ZoneId

class BuscaChavePixResponseConverter {
    fun convert(chaveInfo: ChavePixInfo): BuscarChavePixResponse {
        return BuscarChavePixResponse.newBuilder()
                .setClienteId(chaveInfo.clienteId?.toString() ?: "") // Protobuf usa "" como default value para String
                .setPixId(chaveInfo.pixId?.toString() ?: "") // Protobuf usa "" como default value para String
                .setChave(BuscarChavePixResponse.ChavePix
                        .newBuilder()
                        .setTipoDaChave(br.com.zup.william.TipoDeChave.valueOf(chaveInfo.tipo.name))
                        .setChave(chaveInfo.chave)
                        .setConta(BuscarChavePixResponse.ChavePix.ContaInfo.newBuilder()
                                .setTipo(br.com.zup.william.TipoDeConta.valueOf(chaveInfo.tipoDeConta.name))
                                .setInstituicao(chaveInfo.conta.nomeInstituicao)
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
                                    .setNanos(createdAt!!.nano)
                                    .build()
                        })
                )
                .build()
    }
}