package br.com.zup.william.registrabcb

import br.com.zup.william.registra.TipoDeChave

enum class KeyType(val tipo: TipoDeChave) {
    CPF(TipoDeChave.CPF),
    RANDOM(TipoDeChave.CHAVE_ALEATORIA),
    EMAIL(TipoDeChave.EMAIL),
    PHONE(TipoDeChave.TELEFONE_CELULAR);

    companion object{
        private val mapping = KeyType.values().associateBy(KeyType::tipo)

        fun by(tipo: TipoDeChave): KeyType{
            return mapping[tipo] ?: throw IllegalArgumentException("Chave não existente")
        }
    }
}