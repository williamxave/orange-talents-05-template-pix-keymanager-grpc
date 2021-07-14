package br.com.zup.william.registra

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client("\${url.itau.erp}")
interface ValidaDadosApiItau {

    @Get("/api/v1/clientes/{clienteId}")
    fun busca(@PathVariable clienteId: String):
            HttpResponse<ContaResponse>
}