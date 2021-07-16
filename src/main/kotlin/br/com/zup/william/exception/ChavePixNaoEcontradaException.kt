package br.com.zup.william.exception

import java.lang.RuntimeException

class ChavePixNaoEcontradaException(val msg: String): RuntimeException(msg)