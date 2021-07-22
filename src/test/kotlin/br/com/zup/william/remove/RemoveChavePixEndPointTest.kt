package br.com.zup.william.remove

import br.com.zup.william.RemoveChavePixRequest
import br.com.zup.william.RemoveKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.buscar.BuscarChaveEndPointTest
import br.com.zup.william.registra.*
import br.com.zup.william.registrabcb.RegistraChaveBCB
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
internal class RemoveChavePixEndPointTest(
        val chavePixRepository: ChavePixRepository,
        val grpcClientezinho: RemoveKeyManagerPixGRPCServiceGrpc
        .RemoveKeyManagerPixGRPCServiceBlockingStub
) {
    @Inject
    lateinit var bcb: RegistraChaveBCB

    lateinit var CHAVE_EXISTENTE: ChavePix
    var CLIENTE_ID: String = UUID.randomUUID().toString()

    @BeforeEach
    internal fun setUp() {
        CHAVE_EXISTENTE = chavePixRepository.save(chave(TipoDeChave.EMAIL, "william@email.com", CLIENTE_ID.toString()))

    }

    @AfterEach
    internal fun tearDown() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve remover uma chave pix existente`() {
        Mockito.`when`(bcb.deletar("william@email.com",
                DeletePixKeyRequest("william@email.com")))
                .thenReturn(HttpResponse.ok(
                        DeletePixKeyResponse(
                                "william@email.com",
                                Conta.ITAU_UNIBANCO_ISPB,
                                LocalDateTime.now())))

        val response = grpcClientezinho.remove(
                RemoveChavePixRequest
                        .newBuilder()
                        .setPixId(CHAVE_EXISTENTE.id)
                        .setIdDoCliente(CHAVE_EXISTENTE.clienteId)
                        .build())
        println(response).toString()

        with(response) {
            assertEquals("Chave pix deletada com sucesso -- ${CHAVE_EXISTENTE.id}", response.mensagem)
        }

    }

    @Test
    fun `nao deve remover uma chave pix existente quando ocorre algum erro no bcb`() {
        //cenario

        Mockito.`when`(bcb.deletar("william@email.com",
                DeletePixKeyRequest("william@email.com")))
                .thenReturn(HttpResponse.unprocessableEntity())
        //acao
        val erros = assertThrows<StatusRuntimeException> {
            grpcClientezinho.remove(RemoveChavePixRequest.newBuilder()
                    .setPixId("36273623273627")
                    .setIdDoCliente("837283263").build())
        }
        //validacao

        with(erros) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou Cliente está inválido", status.description)
        }
    }


    @Test
    fun `NAO deve deletar chave Pix se o cliente nao for o dono`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClientezinho.remove(RemoveChavePixRequest
                    .newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id)
                    .setIdDoCliente("2387232873")
                    .build())

        }

        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("Chave Pix não encontrada ou Cliente está inválido", error.status.description)

    }

    @Test
    fun `NAO deve deletar chave Pix se a chave nao existe`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClientezinho.remove(RemoveChavePixRequest
                    .newBuilder()
                    .setPixId("3726327323823")
                    .setIdDoCliente(CHAVE_EXISTENTE.clienteId)
                    .build())

        }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("Chave Pix não encontrada ou Cliente está inválido", error.status.description)

    }

    fun chave(
            tipoDeChave: TipoDeChave,
            chave: String = UUID.randomUUID().toString(),
            clienteId: String = UUID.randomUUID().toString()
    ): ChavePix {
        return ChavePix(
                clienteId,
                chave,
                tipoDeChave,
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = Conta(
                        agencia = "3306",
                        nome = "william",
                        cpf = "02467781054",
                        ispb = Conta.ITAU_UNIBANCO_ISPB,
                        numero = "0001",
                        nomeInstituicao = "ITAU S.A.",

                        )
        )
    }



    @MockBean(RegistraChaveBCB::class)
    fun `mockBcb2`(): RegistraChaveBCB {
        return Mockito.mock(RegistraChaveBCB::class.java)
    }

    @Factory
    class fabricaParaRemover {
        @Singleton
        fun fabricaRemovedora(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                RemoveKeyManagerPixGRPCServiceGrpc.RemoveKeyManagerPixGRPCServiceBlockingStub {
            return RemoveKeyManagerPixGRPCServiceGrpc.newBlockingStub(channel)
        }
    }
}
