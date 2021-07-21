package br.com.zup.william.registra

import br.com.zup.william.RegistraChavePixRequest
import br.com.zup.william.RegistraChavePixResponse
import br.com.zup.william.RegistraKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.handler.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@ErrorHandler // Interceptor para tratar as exceptions <3
class RegistaChavePixEndPoint(@Inject val validaChavePix: ValidaChavePix) :
        RegistraKeyManagerPixGRPCServiceGrpc
        .RegistraKeyManagerPixGRPCServiceImplBase() {

    override fun regista(request: RegistraChavePixRequest,
                         responseObserver: StreamObserver<RegistraChavePixResponse>) {
        val log: Logger = LoggerFactory.getLogger(RegistaChavePixEndPoint::class.java)

        //Transforma a request em dto
        log.info("Request em dto")
        val novaChavePix = request.toModel()

        // Valida a chave, e transaforma em model

        log.info("dto em model")
        val chaveValidad = validaChavePix.valida(novaChavePix)
        log.info("chave validada")

        responseObserver.onNext(RegistraChavePixResponse.newBuilder()
                .setIdDoCliente(chaveValidad.clienteId)
                .setPixId(chaveValidad.id)
                .build())
        responseObserver.onCompleted()

    }


}


