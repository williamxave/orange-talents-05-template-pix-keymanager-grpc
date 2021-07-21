package br.com.zup.william.buscar

import br.com.zup.william.BuscarChavePixRequest
import br.com.zup.william.exception.FiltroInvalidoException
import io.micronaut.validation.validator.Validator

fun BuscarChavePixRequest.toModel(validator: Validator): Filtro {
    val filtro = when (filtroCase) {
        BuscarChavePixRequest.FiltroCase.PIXID -> pixId.let {
            Filtro.PorPixId(it.clientId, it.pixId)
        }
        BuscarChavePixRequest.FiltroCase.CHAVE -> Filtro.PorChave(chave)
        BuscarChavePixRequest.FiltroCase.FILTRO_NOT_SET -> Filtro.Invalido()
    }

    if (!filtro.equals(BuscarChavePixRequest.FiltroCase.PIXID)
            || !filtro.equals(BuscarChavePixRequest.FiltroCase.CHAVE)) {
        throw FiltroInvalidoException("Modo de pesquisa inválido")
    }

    return filtro
}