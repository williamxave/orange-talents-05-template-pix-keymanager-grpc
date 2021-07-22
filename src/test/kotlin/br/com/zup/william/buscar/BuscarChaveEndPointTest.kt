package br.com.zup.william.buscar

import br.com.zup.william.BuscarChavePixRequest
import br.com.zup.william.BuscarChavePixRequest.FiltroPorPixId
import br.com.zup.william.BuscarKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.registra.*
import br.com.zup.william.registrabcb.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
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

@MicronautTest(transactional = false)
internal class BuscarChaveEndPointTest(
        val chavePixRepository: ChavePixRepository,
        val grpcClinte: BuscarKeyManagerPixGRPCServiceGrpc
        .BuscarKeyManagerPixGRPCServiceBlockingStub
) {
    @Inject
    lateinit var bcbClient: RegistraChaveBCB

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setUp() {
        chavePixRepository.save(chave(TipoDeChave.EMAIL, "william@email.com", CLIENTE_ID.toString()))
        chavePixRepository.save(chave(TipoDeChave.CPF, "02467781054", CLIENTE_ID.toString()))
        chavePixRepository.save(chave(TipoDeChave.CHAVE_ALEATORIA, "vrau", CLIENTE_ID.toString()))
        chavePixRepository.save(chave(TipoDeChave.TELEFONE_CELULAR, "+5551980637193", CLIENTE_ID.toString()))
    }


    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve buscar chave por pixId e clienteId`() {
        val possivelChave = chavePixRepository.findByValorDaChave("william@email.com").get()
        println(possivelChave.toString())
        //acao
        val response = grpcClinte.buscar(
                BuscarChavePixRequest.newBuilder()
                        .setPixId(
                                FiltroPorPixId.newBuilder()
                                        .setClientId(possivelChave.clienteId)
                                        .setPixId(possivelChave.id)
                                        .build()
                        )
                        .build())
//        validao
        with(response) {
            assertEquals(possivelChave.id, response.pixId)
        }

    }


    @Test
    fun `nao deve buscar chave por pixId e clienteId quando filtro for invalido`() {
        val erros = assertThrows<StatusRuntimeException> {
            grpcClinte.buscar(
                    BuscarChavePixRequest.newBuilder()
                            .build()
            )
        }

        with(erros) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }

    @Test
    fun `nao deve buscar chave por pixId e clienteId se chave pix não existir`() {

        val pixIdInvalido = UUID.randomUUID().toString()
        val clienteIdInvalido = UUID.randomUUID().toString()

        val erros = assertThrows<StatusRuntimeException> {
            grpcClinte.buscar(
                    BuscarChavePixRequest.newBuilder()
                            .setPixId(
                                    FiltroPorPixId.newBuilder()
                                            .setClientId(clienteIdInvalido)
                                            .setPixId(pixIdInvalido)
                                            .build()
                            ).build()
            )
        }

        with(erros) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }

    }

    @Test
    fun `deve carregar chave por tipo de chave quando o registro estiver localmente`() {
        //cenario
        val chaveExistente = chavePixRepository.findByValorDaChave("william@email.com").get()
        //acao
        val response = grpcClinte.buscar(
                BuscarChavePixRequest.newBuilder()
                        .setChave("william@email.com")
                        .build())
        //validacao
        with(response) {
            assertEquals(chaveExistente.valorDaChave, chave.chave)
            assertEquals(chaveExistente.id, pixId)
            assertEquals(chaveExistente.clienteId, clienteId)
            assertEquals(chaveExistente.tipoDeChave.name, chave.tipoDaChave.name)
        }
    }

    @Test
    fun `deve buscar chave por valor quando registro nao existir localmente mas existir no BCB`() {
        val bcbResponse = BuscarChavePixResponse(
                keyType = KeyType.EMAIL,
                key = "william@email.com",
                bankAccount = BankAccount(
                        participant = Conta.ITAU_UNIBANCO_ISPB,
                        branch = "3306",
                        accountNumber = "0001",
                        accountType = BankAccount.AccountType.CACC
                ),
                owner = Owner(
                        type = TipoDePessoa.NATURAL_PERSON,
                        name = "william",
                        taxIdNumber = "02467781054"
                ),
                createdAt = LocalDateTime.now()
        )

        Mockito.`when`(bcbClient.buscar("william@email.com"))
                .thenReturn(HttpResponse.ok(bcbResponse))

        val response = grpcClinte.buscar(BuscarChavePixRequest
                .newBuilder()
                .setChave("william@email.com")
                .build())

        with(response) {
            assertEquals(bcbResponse.keyType?.name, chave.tipoDaChave.name)
        }

    }


    @Test
    fun `nao deve buscar chave por valor quando registro nao existir localmente mas existir no BCB`() {
        Mockito.`when`(bcbClient.buscar("naodeveencontrar@email.com"))
                .thenReturn(HttpResponse.notFound())

        val erros = assertThrows<StatusRuntimeException> {

            grpcClinte.buscar(BuscarChavePixRequest
                    .newBuilder()
                    .setChave("naodeveencontrar@email.com")
                    .build())
        }
        with(erros) {
            assertEquals(Status.NOT_FOUND.code, erros.status.code)
            assertEquals("Chave Pix não encontrada", erros.status.description)
        }

    }


    @MockBean(RegistraChaveBCB::class)
    fun bcbClient(): RegistraChaveBCB {
        return Mockito.mock(RegistraChaveBCB::class.java)
    }

    @Factory
    class ClientsBuscar {
        @Bean
        fun blockStubBuscar(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                BuscarKeyManagerPixGRPCServiceGrpc.BuscarKeyManagerPixGRPCServiceBlockingStub {
            return BuscarKeyManagerPixGRPCServiceGrpc.newBlockingStub(channel)
        }
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

}