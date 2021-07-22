package br.com.zup.william.registra

import javax.persistence.*
import javax.validation.constraints.NotBlank

@Embeddable
class Conta(
        @field:NotBlank val nome: String,
        @field:NotBlank val cpf: String,
        @field:NotBlank val nomeInstituicao: String,
        @field:NotBlank val ispb: String,
        @field:NotBlank val agencia: String,
        @field:NotBlank val numero: String,
) {
    companion object{
        val ITAU_UNIBANCO_ISPB: String = "60701190"
    }



    override fun toString(): String {
        return "Conta(nome='$nome', cpf='$cpf', nomeInstituicao='$nomeInstituicao', ispb='$ispb', agencia='$agencia', numero='$numero')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conta

        if (nome != other.nome) return false
        if (cpf != other.cpf) return false
        if (nomeInstituicao != other.nomeInstituicao) return false
        if (ispb != other.ispb) return false
        if (agencia != other.agencia) return false
        if (numero != other.numero) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nome.hashCode()
        result = 31 * result + cpf.hashCode()
        result = 31 * result + nomeInstituicao.hashCode()
        result = 31 * result + ispb.hashCode()
        result = 31 * result + agencia.hashCode()
        result = 31 * result + numero.hashCode()
        return result
    }


}