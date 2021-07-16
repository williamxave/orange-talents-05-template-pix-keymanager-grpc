package br.com.zup.william.registra

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, String> {

    fun existsByValorDaChave(chave: String): Boolean

    fun existsByIdAndClienteId(pixId: String, idDoCliente: String): Boolean
}