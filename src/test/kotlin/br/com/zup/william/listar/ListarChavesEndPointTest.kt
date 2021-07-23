package br.com.zup.william.listar

import br.com.zup.william.ListarChavePixRequest
import br.com.zup.william.ListarrKeyManagerPixGRPCServiceGrpc
import br.com.zup.william.registra.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import java.util.*
import javax.inject.Inject
import org.junit.jupiter.api.Assertions.*

@MicronautTest(transactional = false)
internal class ListarChavesEndPointTest(

        @Inject val chavePixRepository: ChavePixRepository,
        @Inject val grcClient: ListarrKeyManagerPixGRPCServiceGrpc
        .ListarrKeyManagerPixGRPCServiceBlockingStub
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.save(chave(TipoDeChave.EMAIL, "william@email.com", CLIENTE_ID.toString()))


    }

    @AfterEach
    internal fun tearDown() {
        chavePixRepository.deleteAll()

    }

    @Test
    fun `deve listar todas chaves do cliente`() {
        //cenario

        //acao
        val response = grcClient.listar(
                ListarChavePixRequest.newBuilder()
                        .setIdDoCliente(CLIENTE_ID.toString())
                        .build())
        //validacao
        Assertions.assertTrue(response.chavesList.isNotEmpty())
    }

    @Test
    fun `nao deve listar se o cliente não tiver nenhuma chave pix cadastrada`() {
        //cenario

        //acao
        val erro = assertThrows<StatusRuntimeException> {
            grcClient.listar(
                    ListarChavePixRequest
                            .newBuilder()
                            .setIdDoCliente(UUID.randomUUID().toString())
                            .build())
        }
        //validacao
        with(erro){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Nenhuma chave pix econtrada", status.description)
        }
    }


    @Test
    fun `nao deve listar se o cliente passar uma chave vazia`() {
        //cenario

        //acao
        val erro = assertThrows<StatusRuntimeException> {
            grcClient.listar(
                    ListarChavePixRequest
                            .newBuilder()
                            .setIdDoCliente("")
                            .build())
        }
        //validacao
        with(erro){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Id do cliente inválido ou vazio", status.description)
        }
    }

    @Test
    fun `nao deve listar se o cliente passar uma chave nulla`() {
        //cenario

        //acao
        val erro = assertThrows<StatusRuntimeException> {
            grcClient.listar(
                    ListarChavePixRequest
                            .newBuilder()
                            .build())
        }
        //validacao
        with(erro){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Id do cliente inválido ou vazio", status.description)
        }
    }


    @Factory
    class ClientList {
        @Bean
        fun mockLista(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ListarrKeyManagerPixGRPCServiceGrpc.ListarrKeyManagerPixGRPCServiceBlockingStub {
            return ListarrKeyManagerPixGRPCServiceGrpc.newBlockingStub(channel)
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

