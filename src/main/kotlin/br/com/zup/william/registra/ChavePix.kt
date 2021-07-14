package br.com.zup.william.registra

import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
        @field:NotBlank val clienteId: String,
        @field:NotBlank @field:Size(max = 77) @field:Column(unique = true) val valorDaChave: String,
        @field:NotNull @field:Enumerated(EnumType.STRING) val tipoDeChave: TipoDeChave,
        @field:NotNull @field:Enumerated(EnumType.STRING) val tipoDeConta: TipoDeConta,
        @Embedded @field:NotNull val conta: Conta
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

}