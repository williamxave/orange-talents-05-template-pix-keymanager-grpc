package br.com.zup.william.remove

import br.com.zup.william.RemoveChavePixRequest
import br.com.zup.william.RemoveChavePixResponse
import br.com.zup.william.RemoveKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.exception.ChavePixNaoEcontradaException
import br.com.zup.william.handler.ErrorHandler
import br.com.zup.william.registra.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RemoveChavePixEndPoint(val chavePixRepository: ChavePixRepository) :
        RemoveKeyManagerPixGRPCServiceGrpc
        .RemoveKeyManagerPixGRPCServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest,
                        responseObserver: StreamObserver<RemoveChavePixResponse>?) {

        if (!chavePixRepository.existsByIdAndClienteId(request.pixId, request.idDoCliente)) {
            throw ChavePixNaoEcontradaException("Chave Pix não encontrada ou Cliente está inválido")
        }

        chavePixRepository.deleteById(request.pixId)

        responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
                .setMensagem("Chave pix deletada com sucesso -- ${request.pixId}").build())
        responseObserver?.onCompleted()
    }
}