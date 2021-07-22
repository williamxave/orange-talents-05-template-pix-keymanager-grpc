package br.com.zup.william.buscar

import br.com.zup.william.exception.ChavePixNaoEcontradaException
import br.com.zup.william.registra.ChavePixRepository
import br.com.zup.william.registrabcb.RegistraChaveBCB
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: RegistraChaveBCB): ChavePixInfo

    @Introspected
    data class PorPixId(
            @field:NotBlank val clienteId: String,
            @field:NotBlank val pixId: String
    ) : Filtro() {

        override fun filtra(repository: ChavePixRepository, bcbClient: RegistraChaveBCB): ChavePixInfo {
            return repository.findById(pixId)
                    .filter { it.pertenceAo(clienteId) }
                    .map(ChavePixInfo::of)
                    .orElseThrow { ChavePixNaoEcontradaException("Chave Pix não encontrada") }
        }

    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String) : Filtro() {

        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, bcbClient: RegistraChaveBCB): ChavePixInfo {
            return repository.findByValorDaChave(chave)
                    .map(ChavePixInfo::of)
                    .orElseGet {
                        LOGGER.info("Consultando chave Pix '$chave' no Banco Central do Brasil (BCB)")

                        val response = bcbClient.buscar(chave)

                        when (response.status) {
                            HttpStatus.OK -> response?.body().toModel()
                            else -> throw ChavePixNaoEcontradaException("Chave Pix não encontrada")
                        }
                    }
        }
    }

    @Introspected
    class Invalido() : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: RegistraChaveBCB): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }
}

