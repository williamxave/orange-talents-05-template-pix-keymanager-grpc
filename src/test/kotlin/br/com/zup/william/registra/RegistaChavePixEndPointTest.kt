package br.com.zup.william.registra

import br.com.zup.william.RegistraChavePixRequest
import br.com.zup.william.RegistraChavePixResponse
import br.com.zup.william.RegistraKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.TipoDeChave
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.Before
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.lang.IllegalArgumentException
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistaChavePixEndPointTest(
        val chavePixRepository: ChavePixRepository,
        val grpcClient: RegistraKeyManagerPixGRPCServiceGrpc.RegistraKeyManagerPixGRPCServiceBlockingStub,
        val itau: ValidaDadosApiItau,
) {


    @Before
    fun setUp() {
        chavePixRepository.deleteAll()
    }

    @AfterEach
    internal fun tearDown() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve validar tipo EMAIL`(){
        with(br.com.zup.william.registra.TipoDeChave.EMAIL){
            assertTrue(valida("william@email.com"))
        }
    }
    @Test
    fun `Nao deve validar tipo EMAIL`(){
        with(br.com.zup.william.registra.TipoDeChave.EMAIL){
            assertFalse(valida("email errado"))
            assertFalse(valida(""))
            assertFalse(valida(null))
        }
    }

    @Test
    fun `deve validar o tipo CPF`(){
        with(br.com.zup.william.registra.TipoDeChave.CPF){
            assertTrue(valida("87664996074"))
        }
    }

    @Test
    fun `Nao deve validar tipo CPF`(){
        with(br.com.zup.william.registra.TipoDeChave.CPF){
            assertFalse(valida("8766499607A"))
            assertFalse(valida(""))
            assertFalse(valida(null))
        }
    }

    @Test
    fun `deve validar o tipo CELULAR`(){
        with(br.com.zup.william.registra.TipoDeChave.TELEFONE_CELULAR){
            assertTrue(valida("+5551980629876"))
        }
    }

    @Test
    fun `Nao deve validar tipo CELULAR`(){
        with(br.com.zup.william.registra.TipoDeChave.CPF){
            assertFalse(valida("+5544930626198"))
            assertFalse(valida(""))
            assertFalse(valida("+554598062719"))
            assertFalse(valida(null))
        }
    }

    @Test
    fun `deve validar o tipo ALEATORIA`(){
        with(br.com.zup.william.registra.TipoDeChave.CHAVE_ALEATORIA){
            assertTrue(valida(""))
        }
    }

    @Test
    fun `Nao deve validar tipo ALEATORIA`(){
        with(br.com.zup.william.registra.TipoDeChave.CHAVE_ALEATORIA){
            assertFalse(valida("qualquer valor passado"))
        }
    }

    @Test
    fun `deve verificar se chave pix ja existe no banco`(){

        val conta = Conta(
                "william",
                "87664996074",
                "ITAU",
                "1234"

        )
        val chavePix = ChavePix(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "william@email.com",
                br.com.zup.william.registra.TipoDeChave.EMAIL,
                TipoDeConta.CONTA_POUPANCA,
                conta
        )
        chavePixRepository.save(chavePix)

        assertTrue(chavePixRepository.existsByValorDaChave(chavePix.valorDaChave))
    }


    @Test
    fun `deve cadastrar um nova chave pix celular`() {
        val dadosParaRequest = pegarRequest("+5551980627198", TipoDeChave.TELEFONE_CELULAR)
        val response = registrar(dadosParaRequest)
        assertEquals(dadosParaRequest.idDoCliente, response.idDoCliente)
        assertNotNull(response.pixId)
    }

    @Test
    fun `deve cadastrar um nova chave pix cpf`() {
        val dadosParaRequest = pegarRequest("87664996074", TipoDeChave.CPF)
        val response = registrar(dadosParaRequest)
        assertEquals(dadosParaRequest.idDoCliente, response.idDoCliente)
        assertNotNull(response.pixId)
    }

    @Test
    fun `deve cadastrar um nova chave pix aleatoria`() {
        val dadosParaRequest = pegarRequest("", TipoDeChave.CHAVE_ALEATORIA)
        val response = registrar(dadosParaRequest)
        assertEquals(dadosParaRequest.idDoCliente, response.idDoCliente)
        assertNotNull(response.pixId)
    }


    @Test
    fun `deve cadastrar um nova chave pix email`() {
        val dadosParaRequest = pegarRequest("william@email.com", TipoDeChave.EMAIL)
        val response = registrar(dadosParaRequest)
        assertEquals(dadosParaRequest.idDoCliente, response.idDoCliente)
        assertNotNull(response.pixId)
    }

    @Test
    fun `NAO deve cadastrar chave pix com dados nao preenchidos`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.regista(RegistraChavePixRequest.newBuilder().build())
        }
        assertEquals(Status.INTERNAL.code, error.status.code)
        assertEquals("Erro Desconhecido!", error.status.description)
    }

    @Test
    fun `NAO deve cadastrar chave pix com dados valor de chave nao correspondendo ao tipo`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.regista(RegistraChavePixRequest.newBuilder()
                    .setIdDoCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setValorDaChave("")
                    .setTipoDeConta(br.com.zup.william.TipoDeConta.CONTA_CORRENTE)
                    .build())
        }
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Campo inválido", error.status.description)
    }

    @Test
    fun `NAO deve cadastrar chave pix com TIPO DE CHAVE INVALIDA`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.regista(RegistraChavePixRequest.newBuilder()
                    .setIdDoCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoDeChave(TipoDeChave.TIPO_DE_CHAVE_DESCONHECIDA)
                    .setValorDaChave("")
                    .setTipoDeConta(br.com.zup.william.TipoDeConta.CONTA_CORRENTE)
                    .build())
        }
        assertEquals(Status.INTERNAL.code, error.status.code)
        assertEquals("Erro Desconhecido!", error.status.description)
    }

    @Test
    fun `NAO deve cadastrar chave pix se ja existir uma igual cadastrada`() {
        chavePixRepository.deleteAll()
        val conta = Conta(
                "william",
                "87664996074",
                "ITAU",
                "1234"

        )
        val chavePix = ChavePix(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "william@email.com",
                br.com.zup.william.registra.TipoDeChave.EMAIL,
                TipoDeConta.CONTA_POUPANCA,
                conta
        )
        chavePixRepository.save(chavePix)
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


    fun registrar(dadosParaRequest: RegistraChavePixRequest): RegistraChavePixResponse {
        Mockito.`when`(itau.busca(dadosParaRequest.idDoCliente)).thenReturn(dadosDeResposta())
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

    fun dadosDeResposta(): MutableHttpResponse<ContaResponse>? {
        return HttpResponse.ok<ContaResponse>(
                ContaResponse(
                        "c56dfef4-7901-44fb-84e2-a2cefb157890",
                        "william",
                        "87664996074",
                        InstituicaoResponse(
                                "ITAU",
                                "1234")
                )

        )
    }

    @MockBean(ValidaDadosApiItau::class)
    fun `mockApiItau`(): ValidaDadosApiItau {
        return Mockito.mock(ValidaDadosApiItau::class.java)
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