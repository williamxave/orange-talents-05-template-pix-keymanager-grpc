package br.com.zup.william.remove

import br.com.zup.william.RemoveChavePixRequest
import br.com.zup.william.RemoveKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.registra.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton


@MicronautTest(transactional = false)
internal class RemoveChavePixEndPointTest(
        val chavePixRepository: ChavePixRepository,
        val grpcClient: RemoveKeyManagerPixGRPCServiceGrpc.RemoveKeyManagerPixGRPCServiceBlockingStub
) {


    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve excluir uma chave pix do banco`() {
        val chavePix = geraChave()
        chavePixRepository.save(chavePix)

        val response = grpcClient.remove(RemoveChavePixRequest
                .newBuilder()
                .setPixId(chavePix.id)
                .setIdDoCliente(chavePix.clienteId)
                .build())

        assertEquals("Chave pix deletada com sucesso -- ${chavePix.id}", response.mensagem)
    }

    @Test
    fun `NAO deve deletar chave Pix se o cliente nao for o dono`() {
        val chavePix = geraChave()
        chavePixRepository.save(chavePix)
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest
                    .newBuilder()
                    .setPixId(chavePix.id)
                    .setIdDoCliente("2387232873")
                    .build())

        }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("Chave Pix não encontrada ou Cliente está inválido", error.status.description)

    }

    @Test
    fun `NAO deve deletar chave Pix se a chave nao existe`() {
        val chavePix = geraChave()
        chavePixRepository.save(chavePix)
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest
                    .newBuilder()
                    .setPixId("3726327323823")
                    .setIdDoCliente(chavePix.clienteId)
                    .build())

        }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("Chave Pix não encontrada ou Cliente está inválido", error.status.description)

    }

    fun geraChave(): ChavePix {
        val conta = Conta(
                "william",
                "87664996074",
                "ITAU",
                "1234",
                "0001",
                "3306"


        )
        val chavePix = ChavePix(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "william@email.com",
                TipoDeChave.EMAIL,
                TipoDeConta.CONTA_POUPANCA,
                conta
        )

        return chavePix
    }

}

@Factory
class grpc {
    @Singleton
    fun blockingSetup(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            RemoveKeyManagerPixGRPCServiceGrpc.RemoveKeyManagerPixGRPCServiceBlockingStub {
        return RemoveKeyManagerPixGRPCServiceGrpc.newBlockingStub(channel)
    }
}