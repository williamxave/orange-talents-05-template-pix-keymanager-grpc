package br.com.zup.william.registra

import java.time.LocalDateTime
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

    var criadaEm: LocalDateTime? = null


    fun pertenceAo(clienteId: String) = this.clienteId.equals(clienteId)

    //Quando registrar no bcb e a chave for random o outro sistema vai gerar a chave
    fun chaveAleatoria(): Boolean{
        return tipoDeChave == TipoDeChave.CHAVE_ALEATORIA
    }

    fun atualizaChave(chave: String): Boolean {
        if(chaveAleatoria()){
            this.valorDaChave = chave
            return true
        }
        return false
    }

}