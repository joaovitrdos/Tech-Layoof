package com.layoof.layoof.uploadFile;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageConstraintValidator.class)
public @interface ValidImage {

    String message() default "Arquivo enviado nao e uma imagem valida";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
