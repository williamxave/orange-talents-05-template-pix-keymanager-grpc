package br.com.zup.william.remove

import br.com.zup.william.registra.Conta
import io.micronaut.core.annotation.Introspected

@Introspected
class DeletePixKeyRequest(
        val key: String?,
        val participant: String?
) {

    constructor(chave: String?): this(chave,Conta.ITAU_UNIBANCO_ISPB)

}
