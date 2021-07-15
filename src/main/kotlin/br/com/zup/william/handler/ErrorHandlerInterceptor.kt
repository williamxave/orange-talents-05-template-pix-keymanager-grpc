package br.com.zup.william.handler

import br.com.zup.william.exception.ChavePixException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import java.lang.IllegalArgumentException
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class ErrorHandlerInterceptor : MethodInterceptor<Any, Any> {
    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {
            return context.proceed()
        } catch (e: Exception) {

            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when (e) {

                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                        .withDescription("Campo inválido")

                is ChavePixException -> Status.ALREADY_EXISTS
                        .withDescription(e.message)

                else -> Status.INTERNAL
                        .withDescription("Erro Desconhecido!")
            }
            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }
}