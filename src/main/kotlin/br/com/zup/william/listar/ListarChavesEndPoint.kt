package br.com.zup.william.listar

import br.com.zup.william.*
import br.com.zup.william.exception.ChavePixNaoEcontradaException
import br.com.zup.william.handler.ErrorHandler
import br.com.zup.william.registra.ChavePix
import br.com.zup.william.registra.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ListarChavesEndPoint(
        @Inject val chavePixRepository: ChavePixRepository
) : ListarrKeyManagerPixGRPCServiceGrpc.ListarrKeyManagerPixGRPCServiceImplBase() {

    override fun listar(request: ListarChavePixRequest,
                        responseObserver: StreamObserver<ListarChavePixResponse>) {

        if (request.idDoCliente.isNullOrBlank()) {
            throw IllegalArgumentException("Id do cliente inválido ou vazio")
        }

        val listaDeChave = chavePixRepository.findAllByClienteId(request.idDoCliente).map {
            ListarChavePixResponse.Listar.newBuilder()
                    .setPixId(it.id)
                    .setIdDoCliente(it.clienteId)
                    .setTipoDeChave(TipoDeChave.valueOf(it.tipoDeChave.toString()))
                    .setValorDaChave(it.valorDaChave)
                    .setTipoDeConta(TipoDeConta.valueOf(it.tipoDeConta.toString()))
                    .setCriadaEm(it.criadaEm.let {
                        val criadaEm = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                                .setSeconds(criadaEm.epochSecond)
                                .setNanos(criadaEm.nano)
                                .build()

                    })
                    .build()
        }.ifEmpty { throw ChavePixNaoEcontradaException("Nenhuma chave pix econtrada") }

        responseObserver.onNext(
                ListarChavePixResponse.newBuilder()
                        .setClientId(request.idDoCliente)
                        .addAllChaves(listaDeChave)
                        .build())
        responseObserver.onCompleted()

    }
}