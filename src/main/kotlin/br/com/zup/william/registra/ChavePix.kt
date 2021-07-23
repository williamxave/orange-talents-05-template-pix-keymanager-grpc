package br.com.zup.william.registra

import br.com.zup.william.ListarChavePixResponse
import br.com.zup.william.exception.ChavePixNaoEcontradaException
import com.google.protobuf.Timestamp
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
        @field:NotBlank val clienteId: String,
        @field:NotBlank @field:Size(max = 77) @field:Column(unique = true) var valorDaChave: String,
        @field:NotNull @field:Enumerated(EnumType.STRING) val tipoDeChave: TipoDeChave,
        @field:NotNull @field:Enumerated(EnumType.STRING) val tipoDeConta: TipoDeConta,
        @Embedded @field:NotNull val conta: Conta
) {

    @Id
    var id: String = UUID.randomUUID().toString()

    var criadaEm: LocalDateTime = LocalDateTime.now()


    fun pertenceAo(clienteId: String) = this.clienteId.equals(clienteId)

    //Quando registrar no bcb e a chave for random o outro sistema vai gerar a chave
    fun chaveAleatoria(): Boolean {
        return tipoDeChave == TipoDeChave.CHAVE_ALEATORIA
    }

    fun atualizaChave(chave: String): Boolean {
        if (chaveAleatoria()) {
            this.valorDaChave = chave
            return true
        }
        return false
    }


    override fun toString(): String {
        return "ChavePix(clienteId='$clienteId', valorDaChave='$valorDaChave', tipoDeChave=$tipoDeChave, tipoDeConta=$tipoDeConta, conta=$conta, id='$id', criadaEm=$criadaEm)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChavePix

        if (clienteId != other.clienteId) return false
        if (valorDaChave != other.valorDaChave) return false
        if (tipoDeChave != other.tipoDeChave) return false
        if (tipoDeConta != other.tipoDeConta) return false
        if (conta != other.conta) return false
        if (id != other.id) return false
        if (criadaEm != other.criadaEm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clienteId.hashCode()
        result = 31 * result + valorDaChave.hashCode()
        result = 31 * result + tipoDeChave.hashCode()
        result = 31 * result + tipoDeConta.hashCode()
        result = 31 * result + conta.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (criadaEm?.hashCode() ?: 0)
        return result
    }


}