package br.com.zup.william.handler

import br.com.zup.william.exception.ChavePixException
import br.com.zup.william.exception.ChavePixNaoEcontradaException
import com.google.rpc.BadRequest
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
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

                is ChavePixNaoEcontradaException -> Status.NOT_FOUND
                        .withDescription(e.message)

                is IllegalArgumentException -> Status.INVALID_ARGUMENT
                        .withDescription(e.message)

                else -> Status.INTERNAL
                        .withDescription("Erro Desconhecido!")
            }
            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }

    private fun handlerConstraintValidationException(e: ConstraintViolationException): StatusRuntimeException {
        val badRequest = BadRequest.newBuilder()
                .addAllFieldViolations(e.constraintViolations.map {
                    BadRequest.FieldViolation.newBuilder()
                            .setField(it.propertyPath.last().name)
                            .setDescription(it.message)
                            .build()
                }).build()

        val  statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT_VALUE)
                .setMessage("Parametros inválidos")
                .addDetails(com.google.protobuf.Any.pack(badRequest))
                .build()
        return StatusProto.toStatusRuntimeException(statusProto)
    }
}