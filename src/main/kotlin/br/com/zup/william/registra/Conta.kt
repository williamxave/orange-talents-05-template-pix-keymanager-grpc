package br.com.zup.william.registra

import javax.persistence.*
import javax.validation.constraints.NotBlank

@Embeddable
class Conta(
        @field:NotBlank val nome: String,
        @field:NotBlank val cpf: String,
        @field:NotBlank val nomeInstituicao: String,
        @field:NotBlank val ispb: String
) {
}