package br.com.zup.william.registra

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoDeChave {
    CPF {
        override fun valida(valorDaChave: String?): Boolean {
            if (valorDaChave.isNullOrBlank()) {
                return false
            }
            if (!valorDaChave.matches("^[0-9]{11}$".toRegex())) {
                return false
            }
            return CPFValidator().run {
                initialize(null)
                isValid(valorDaChave, null)
            }
        }

    },
    TELEFONE_CELULAR {
        override fun valida(valorDaChave: String?): Boolean {
            if (valorDaChave.isNullOrBlank()) {
                return false
            }
            return valorDaChave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun valida(valorDaChave: String?): Boolean {
            if (valorDaChave.isNullOrBlank()) {
                return false
            }
            return EmailValidator().run {
                initialize(null)
                isValid(valorDaChave, null)
            }
        }

    },
    CHAVE_ALEATORIA {
        override fun valida(valorDaChave: String?): Boolean {
            return valorDaChave.isNullOrBlank()
        }
    };

    abstract fun valida(valorDaChave: String?): Boolean
}