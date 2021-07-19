package br.com.zup.william.remove

import br.com.zup.william.RemoveChavePixRequest
import br.com.zup.william.RemoveChavePixResponse
import br.com.zup.william.RemoveKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.exception.ChavePixNaoEcontradaException
import br.com.zup.william.handler.ErrorHandler
import br.com.zup.william.registra.ChavePixRepository
import br.com.zup.william.registrabcb.RegistraChaveBCB
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@ErrorHandler
class RemoveChavePixEndPoint(val chavePixRepository: ChavePixRepository,
                             val registraChaveBCB: RegistraChaveBCB) :
        RemoveKeyManagerPixGRPCServiceGrpc
        .RemoveKeyManagerPixGRPCServiceImplBase() {
    @Transactional
    override fun remove(request: RemoveChavePixRequest,
                        responseObserver: StreamObserver<RemoveChavePixResponse>?) {

        //Verifica se existe a chave no banco usando os dados da request
        if (!chavePixRepository.existsByIdAndClienteId(request.pixId, request.idDoCliente)) {
            throw ChavePixNaoEcontradaException("Chave Pix não encontrada ou Cliente está inválido")
        }

        try {
            //Tenta buscar no banco a chave usando o id vindo da request
            val chaveDoBanco = chavePixRepository.findById(request.pixId)

            //Cria o obj da request para a chamada no BCB
            val corpoRequest = DeletePixKeyRequest(chaveDoBanco.get().valorDaChave)

            //Chama ao BCB para deletar a chave
            val requestBcb = registraChaveBCB.deletar(chaveDoBanco.get().valorDaChave, corpoRequest)

            //Verifica se foi deletado no BCB, usando o status code da response
            if (requestBcb.status.equals(HttpStatus.OK)) {
                chavePixRepository.deleteById(request.pixId)
            }

            responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
                    .setMensagem("Chave pix deletada com sucesso -- ${request.pixId}").build())
            responseObserver?.onCompleted()

        } catch (e: HttpClientResponseException) {
            when (e.status) {
                HttpStatus.FORBIDDEN -> throw IllegalArgumentException("Permicão negada")
                HttpStatus.NOT_FOUND -> throw ChavePixNaoEcontradaException("Chave não encontrada no sistema")
                else -> throw IllegalArgumentException("Erro ao deletar chave! Tente novamente")
            }
        }
    }
}