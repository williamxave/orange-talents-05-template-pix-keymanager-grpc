package br.com.zup.william.registra

import br.com.zup.william.RegistraChavePixRequest
import br.com.zup.william.RegistraChavePixResponse
import br.com.zup.william.RegistraKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.TipoDeChave
import br.com.zup.william.registrabcb.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.Before
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistaChavePixEndPointTest(
        val chavePixRepository1: ChavePixRepository,
        val grpcClient: RegistraKeyManagerPixGRPCServiceGrpc
        .RegistraKeyManagerPixGRPCServiceBlockingStub
) {
    @Inject
    lateinit var itau1: ValidaDadosApiItau

    @Inject
    lateinit var bcb1: RegistraChaveBCB

    companion object {
        val CLIENTE_ID: UUID = UUID.randomUUID()
    }

    @Before
    fun setUp() {
        chavePixRepository1.deleteAll()
    }

    @AfterEach
    internal fun tearDown() {
        chavePixRepository1.deleteAll()
    }

    @Test
    fun `deve validar tipo EMAIL`() {
        with(br.com.zup.william.registra.TipoDeChave.EMAIL) {
            assertTrue(valida("william@email.com"))
        }
    }

    @Test
    fun `Nao deve validar tipo EMAIL`() {
        with(br.com.zup.william.registra.TipoDeChave.EMAIL) {
            assertFalse(valida("email errado"))
            assertFalse(valida(""))
            assertFalse(valida(null))
        }
    }

    @Test
    fun `deve validar o tipo CPF`() {
        with(br.com.zup.william.registra.TipoDeChave.CPF) {
            assertTrue(valida("87664996074"))
        }
    }

    @Test
    fun `Nao deve validar tipo CPF`() {
        with(br.com.zup.william.registra.TipoDeChave.CPF) {
            assertFalse(valida("8766499607A"))
            assertFalse(valida(""))
            assertFalse(valida(null))
        }
    }

    @Test
    fun `deve validar o tipo CELULAR`() {
        with(br.com.zup.william.registra.TipoDeChave.TELEFONE_CELULAR) {
            assertTrue(valida("+5551980629876"))
        }
    }

    @Test
    fun `Nao deve validar tipo CELULAR`() {
        with(br.com.zup.william.registra.TipoDeChave.CPF) {
            assertFalse(valida("+5544930626198"))
            assertFalse(valida(""))
            assertFalse(valida("+554598062719"))
            assertFalse(valida(null))
        }
    }

    @Test
    fun `deve validar o tipo ALEATORIA`() {
        with(br.com.zup.william.registra.TipoDeChave.CHAVE_ALEATORIA) {
            assertTrue(valida(""))
        }
    }

    @Test
    fun `Nao deve validar tipo ALEATORIA`() {
        with(br.com.zup.william.registra.TipoDeChave.CHAVE_ALEATORIA) {
            assertFalse(valida("qualquer valor passado"))
        }
    }

    @Test
    fun `deve verificar se chave pix ja existe no banco`() {

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
                br.com.zup.william.registra.TipoDeChave.EMAIL,
                TipoDeConta.CONTA_POUPANCA,
                conta
        )
        chavePixRepository1.save(chavePix)

        assertTrue(chavePixRepository1.existsByValorDaChave(chavePix.valorDaChave))
    }


    @Test
    fun `nao deve regitrar chave pix quando dar algum problema ao registrar no bcb `() {
        //cenario
        Mockito.`when`(itau1.busca("c56dfef4-7901-44fb-84e2-a2cefb157890",
                TipoDeConta.CONTA_CORRENTE.toString())).thenReturn(HttpResponse.ok(dadosDeResposta()))

        Mockito.`when`(bcb1.registra(createKeyPixRequest()))
                .thenReturn(HttpResponse.badRequest())
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.regista(
                    RegistraChavePixRequest.newBuilder()
                            .setIdDoCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                            .setTipoDeChave(TipoDeChave.EMAIL)
                            .setValorDaChave("william@email.com")
                            .setTipoDeConta(br.com.zup.william.TipoDeConta.CONTA_CORRENTE)
                            .build())
        }
        //validacao

        assertEquals(Status.INTERNAL.code, error.status.code)
        assertEquals("Erro Desconhecido!", error.status.description)

    }

    @Test
    fun `nao deve registrar chave pix quando dar algum problema ao registrar no itau `() {
        //cenario
        Mockito.`when`(itau1.busca("c56dfef4-7901-44fb-84e2-a2cefb15789",
                TipoDeConta.CONTA_CORRENTE.toString())).thenReturn(HttpResponse.notFound())
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.regista(
                    RegistraChavePixRequest.newBuilder()
                            .setIdDoCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                            .setTipoDeChave(TipoDeChave.EMAIL)
                            .setValorDaChave("william@email.com")
                            .setTipoDeConta(br.com.zup.william.TipoDeConta.CONTA_CORRENTE)
                            .build())
        }
        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Nenhum dado encontrado", error.status.description)
    }

    @Test
    fun `NAO deve cadastrar chave pix com dados valor de chave nao correspondendo ao tipo`() {
        val error = assertThrows<StatusRuntimeException> {
            val request = grpcClient.regista(
                    RegistraChavePixRequest
                            .newBuilder()
                            .setIdDoCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                            .setTipoDeChave(TipoDeChave.EMAIL)
                            .setValorDaChave("")
                            .setTipoDeConta(br.com.zup.william.TipoDeConta.CONTA_CORRENTE)
                            .build())
        }

        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Campo inválido", error.status.description)
    }

//    @Test
//    fun `deve cadastrar uma chave pix`() {
//        //cenario
//        Mockito.`when`(itau1.busca(CLIENTE_ID.toString(),
//                TipoDeConta.CONTA_POUPANCA.toString()))
//                .thenReturn(HttpResponse.ok(dadosDeResposta()))
//
//        Mockito.`when`(bcb1.registra(createKeyPixRequest()))
//                .thenReturn(HttpResponse.created(createPixKeyResponse()))
//        //acao
//        val response = grpcClient.regista(
//                RegistraChavePixRequest.newBuilder()
//                        .setIdDoCliente(CLIENTE_ID.toString())
//                        .setTipoDeChave(TipoDeChave.EMAIL)
//                        .setValorDaChave("william@email.com")
//                        .setTipoDeConta(br.com.zup.william.TipoDeConta.CONTA_POUPANCA)
//
//                        .build())
//
//        //validacao
//        with(response) {
//            assertEquals(CLIENTE_ID.toString(),
//                    response.idDoCliente)
//            assertNotNull(response.pixId)
//        }
//
//    }

    @Test
    fun `deve retornar um erro quando cliente não é encontrado`() {
        val request = RegistraChavePixRequest.newBuilder()
                .setIdDoCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setValorDaChave("william@email.com")
                .setTipoDeConta(br.com.zup.william.TipoDeConta.CONTA_CORRENTE)
                .build()

        Mockito.`when`(itau1.busca(request.idDoCliente,
                TipoDeConta.CONTA_CORRENTE.toString())).thenReturn(HttpResponse.notFound())

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.regista(request)
        }

        with(response) {
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals("Erro Desconhecido!", status.description)

        }

    }

    @Test
    fun `NAO deve cadastrar chave pix se ja existir uma igual cadastrada`() {
        chavePixRepository1.deleteAll()
        val chavePix = pix()
        chavePixRepository1.save(chavePix)
        Mockito.`when`(itau1.busca(chavePix.clienteId, chavePix.tipoDeConta.toString()))
                .thenReturn(HttpResponse.ok(dadosDeResposta()))

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.regista(RegistraChavePixRequest.newBuilder()
                    .setIdDoCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setValorDaChave("william@email.com")
                    .setTipoDeConta(br.com.zup.william.TipoDeConta.CONTA_POUPANCA)
                    .build())
        }
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
        assertEquals("Chave já existente!", error.status.description)

    }


    fun pix(): ChavePix {
        val conta = Conta(
                "william",
                "02467781054",
                "ITAU",
                "60701190",
                "0001",
                "3306"

        )
        val chavePix = ChavePix(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "william@email.com",
                br.com.zup.william.registra.TipoDeChave.EMAIL,
                TipoDeConta.CONTA_POUPANCA,
                conta
        )
        return chavePix
    }

    fun createKeyPixRequest(): CreateKeyPixRequest {
        return CreateKeyPixRequest(
                keyType = KeyType.EMAIL,
                key = "william@email.com",
                bankAccount = BankAccount(
                        "60701190",
                        "0001",
                        "3306",
                        BankAccount.AccountType.SVGS
                ), owner = Owner(
                type = TipoDePessoa.NATURAL_PERSON,
                "william",
                "02467781054"))
    }

    fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
                keyType = KeyType.EMAIL.toString(),
                key = "william@email.com",
                bankAccount = BankAccountResponse(
                        "60701190",
                        "0001",
                        "3306",
                        TipoDaContaBCB.SVGS.toString()
                ), owner = OwnerResponse(
                type = TipoDePessoa.NATURAL_PERSON.toString(),
                "william",
                "02467781054"),
                createdAt = LocalDateTime.now()
        )
    }


    fun registrar(dadosParaRequest: RegistraChavePixRequest): RegistraChavePixResponse {
        Mockito.`when`(itau1.busca(dadosParaRequest.idDoCliente, dadosParaRequest.tipoDeConta.toString())).thenReturn(HttpResponse.ok(dadosDeResposta()))

        Mockito.`when`(bcb1.registra(createKeyPixRequest()))
                .thenReturn(HttpResponse.created(createPixKeyResponse()))
        return grpcClient.regista(dadosParaRequest)
    }


    fun pegarRequest(chave: String, tipoDeChave: TipoDeChave): RegistraChavePixRequest {
        return RegistraChavePixRequest.newBuilder()
                .setIdDoCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoDeChave(tipoDeChave)
                .setValorDaChave(chave)
                .setTipoDeConta(br.com.zup.william.TipoDeConta.CONTA_POUPANCA)
                .build()

    }

    fun dadosDeResposta(): ContaResponse {
        return ContaResponse(
                TipoDeConta.CONTA_POUPANCA,
                Instituicao(
                        "ITAÚ UNIBANCO S.A.",
                        "60701190"
                ),
                "0001",
                "3306",
                Titular(
                        "c56dfef4-7901-44fb-84e2-a2cefb157890",
                        "Rafael M C Ponte",
                        "02467781054"
                )
        )

    }


    @MockBean(ValidaDadosApiItau::class)
    fun `mockApiItau1`(): ValidaDadosApiItau {
        return Mockito.mock(ValidaDadosApiItau::class.java)
    }

    @MockBean(RegistraChaveBCB::class)
    fun `mockBcb1`(): RegistraChaveBCB {
        return Mockito.mock(RegistraChaveBCB::class.java)
    }


    @Factory
    class grpc {
        @Singleton
        fun blockingSetup(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                RegistraKeyManagerPixGRPCServiceGrpc.RegistraKeyManagerPixGRPCServiceBlockingStub {
            return RegistraKeyManagerPixGRPCServiceGrpc.newBlockingStub(channel)
        }
    }
}


