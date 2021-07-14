package br.com.zup.william.annotation

import br.com.zup.william.registra.NovaChavePixDto
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS,AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidaTipoChavePixValidator::class])
annotation class ValidaTipoChavePix(
        val message: String = "Chave Pix inválida",
        val groups: Array<KClass<Any>> = [],
        val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidaTipoChavePixValidator : ConstraintValidator<ValidaTipoChavePix, NovaChavePixDto> {
    override fun isValid(value: NovaChavePixDto?,
                         annotationMetadata: AnnotationValue<ValidaTipoChavePix>,
                         context: ConstraintValidatorContext): Boolean {

        if (value?.tipoDeChave == null) {
            return false
        }

        return value.tipoDeChave.valida(value.valorDaChave)

    }
}