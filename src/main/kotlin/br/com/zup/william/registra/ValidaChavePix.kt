package br.com.zup.william.registra

import br.com.zup.william.exception.ChavePixException
import br.com.zup.william.handler.ErrorHandler
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class ValidaChavePix(
        @Inject val chavePixRepository: ChavePixRepository,
        @Inject val validaDadosApiItau: ValidaDadosApiItau,
) {
    @Transactional
    fun valida(@Valid novaChavePixDto: NovaChavePixDto): ChavePix {
        val log: Logger = LoggerFactory.getLogger(ValidaChavePix::class.java)

        log.info("Verficando se chave já existe")
        //Verifica se a chave já existe no banco de dados
        if (chavePixRepository.existsByValorDaChave(novaChavePixDto.valorDaChave)) {
            throw ChavePixException("Chave ${novaChavePixDto.valorDaChave} já existente!")
        }
        log.info("Chave aprovada")

        //Busca os dados no api do itau
        // Retorna uma ContaResponse
        log.info("Buscando dados da api ")
        val response = validaDadosApiItau.busca(novaChavePixDto.clienteId)

        // Transforma a response que veio da api do itau em um obj de dominio
        //Temos em mãos uma Conta
        val conta = response.body().toModel()

        //Cria a chave PIX, relancionando com a conta
        val chavePix = novaChavePixDto.toModel(conta)
        log.info("Dados prontos")

        //Salva a chave no banco
        chavePixRepository.save(chavePix)

        //Retorna a chave para o endPoint gRPC
        return chavePix

    }
}