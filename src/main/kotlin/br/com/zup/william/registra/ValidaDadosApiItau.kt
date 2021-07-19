package br.com.zup.william.registra

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${url.itau.erp}")
interface ValidaDadosApiItau {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun busca(@PathVariable("clienteId") clienteId: String,
              @QueryValue("tipo") tipo: String):
            HttpResponse<ContaResponse>
}