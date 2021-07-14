package br.com.zup.william.registra

import br.com.zup.william.RegistraChavePixRequest
import br.com.zup.william.TipoDeConta
import br.com.zup.william.annotation.ValidaTipoChavePix
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.persistence.Column
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidaTipoChavePix // Anotacao que valida o valor da chave pix, usando o método que valida do enum
data class NovaChavePixDto(
        @field:NotBlank val clienteId: String,
        @field:Size(max = 77) @field:Column(unique = true) val valorDaChave: String,
        @field:NotNull @field:Enumerated(EnumType.STRING) val tipoDeChave: TipoDeChave,
        @field:NotNull @field:Enumerated(EnumType.STRING) val tipoDeConta: TipoDeConta
) {

    fun toModel(conta: Conta): ChavePix {
        return ChavePix(
                clienteId = clienteId,
                valorDaChave = if (tipoDeChave == TipoDeChave.CHAVE_ALEATORIA) UUID.randomUUID().toString() else valorDaChave!!,
                tipoDeChave = tipoDeChave,
                tipoDeConta = br.com.zup.william.registra.TipoDeConta.valueOf(this.tipoDeConta.toString()),
                conta = conta
        )
    }
}

// Extends function, transforma a request em um dto
fun RegistraChavePixRequest.toModel(): NovaChavePixDto {
    return NovaChavePixDto(
            idDoCliente,
            valorDaChave,
            tipoDeChave = TipoDeChave.valueOf(this.tipoDeChave.name),
            tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta.name)
    )
}