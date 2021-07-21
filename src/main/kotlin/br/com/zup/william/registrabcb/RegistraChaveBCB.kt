package br.com.zup.william.registrabcb

import br.com.zup.william.buscar.BuscarChavePixResponse
import br.com.zup.william.remove.DeletePixKeyRequest
import br.com.zup.william.remove.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.url}")
interface RegistraChaveBCB {

    @Post("/api/v1/pix/keys",
            consumes = [MediaType.APPLICATION_XML],
            produces = [MediaType.APPLICATION_XML])
    fun registra(@Body request: CreateKeyPixRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{keys}",
            consumes = [MediaType.APPLICATION_XML],
            produces = [MediaType.APPLICATION_XML])
    fun deletar(@PathVariable("keys") keys: String, @Body deletar: DeletePixKeyRequest):
            HttpResponse<DeletePixKeyResponse>


    @Get("/api/v1/pix/keys/{keys}",
            consumes = [MediaType.APPLICATION_XML],
            produces = [MediaType.APPLICATION_XML])
    fun buscar(@PathVariable("keys") keys: String):
            HttpResponse<BuscarChavePixResponse>

}