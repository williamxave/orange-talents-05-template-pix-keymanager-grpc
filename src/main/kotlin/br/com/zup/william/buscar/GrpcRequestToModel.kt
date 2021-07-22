package br.com.zup.william.buscar

import br.com.zup.william.BuscarChavePixRequest
import br.com.zup.william.BuscarChavePixRequest.FiltroCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun BuscarChavePixRequest.toModel(validator: Validator): Filtro {
    val filtro = when (filtroCase) {

        PIXID -> pixId.let {
            Filtro.PorPixId(it.clientId, it.pixId)
        }
        CHAVE -> Filtro.PorChave(chave)

        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro
}