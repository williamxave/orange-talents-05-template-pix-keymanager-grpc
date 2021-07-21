package br.com.zup.william.registra

import br.com.zup.william.exception.ChavePixException
import br.com.zup.william.registrabcb.*
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class ValidaChavePix(
        @Inject val chavePixRepository: ChavePixRepository,
        @Inject val validaDadosApiItau: ValidaDadosApiItau,
        @Inject val registraChaveBCB: RegistraChaveBCB
) {

    @Transactional
    fun valida(@Valid novaChavePixDto: NovaChavePixDto): ChavePix {
        val log: Logger = LoggerFactory.getLogger(ValidaChavePix::class.java)

        log.info("Verficando se chave já existe")
        //Verifica se a chave já existe no banco de dados
        if (chavePixRepository.existsByValorDaChave(novaChavePixDto.valorDaChave)) {
            throw ChavePixException("Chave já existente!")
        }
        log.info("Chave aprovada")

        //Busca os dados no api do itau
        // Retorna uma ContaResponse
        log.info("Buscando dados da api ITAU")
        val response = validaDadosApiItau.busca(novaChavePixDto.clienteId,
                novaChavePixDto.tipoDeConta.toString())
                ?: throw IllegalStateException("Nenhum dado encontrado")

        // Transforma a response que veio da api do itau em um obj de dominio
        //Temos em mãos uma Conta
        val conta = response.body().toModel()


        //Cria a chave PIX, relancionando com a conta
        val chavePix = novaChavePixDto.toModel(conta)
        log.info("Dados prontos")


        //Método statico que cria uma obj de request atraves da chave pix
        val requestBcb = CreateKeyPixRequest.of(chavePix)

        //Chamada a api do BCB
        log.info("Salvando dados no BCB")
        val responseChamaBcb = registraChaveBCB.registra(requestBcb)
        if(responseChamaBcb.status != HttpStatus.CREATED){
            throw IllegalStateException("Erro ao registrar chave pix no BCB")
        }


        //INSERE DATA DE CRIACAO NA CHAVE, DATA ESSA VINDO DA BCB
        val data = responseChamaBcb.body().createdAt
        chavePix.criadaEm = data

        //Verifica se o tipo de chave é random se for vai atualizar e utilizar a chave,
        //random que veio de response o BCB
        chavePix.atualizaChave(responseChamaBcb.body().key)


        chavePixRepository.save(chavePix)
        log.info("Chave cadastrada no BCB")


        //Retorna a chave para o endPoint gRPC
        return chavePix
    }
}