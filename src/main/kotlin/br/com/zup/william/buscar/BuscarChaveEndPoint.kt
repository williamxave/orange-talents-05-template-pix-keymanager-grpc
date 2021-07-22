package br.com.zup.william.buscar

import br.com.zup.william.BuscarChavePixRequest
import br.com.zup.william.BuscarChavePixResponse
import br.com.zup.william.BuscarKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.handler.ErrorHandler
import br.com.zup.william.registra.ChavePixRepository
import br.com.zup.william.registrabcb.RegistraChaveBCB
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class BuscarChaveEndPoint(
        @Inject private val chavePixRepository: ChavePixRepository,
        @Inject private val bcbClient: RegistraChaveBCB,
        @Inject private val validator: Validator
) : BuscarKeyManagerPixGRPCServiceGrpc.BuscarKeyManagerPixGRPCServiceImplBase() {

    override fun buscar(request: BuscarChavePixRequest,
                        responseObserver: StreamObserver<BuscarChavePixResponse>) {

        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = chavePixRepository, bcbClient = bcbClient)

        responseObserver.onNext(BuscaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}