package br.com.zup.william.exception

import java.lang.RuntimeException

class FiltroInvalidoException(val msg: String): RuntimeException(msg)