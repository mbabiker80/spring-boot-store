package com.codewithmosh.store.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)//where we wanna apply this annotation (Enum)
@Retention(RetentionPolicy.RUNTIME) //where is this annotation is applied
@Constraint(validatedBy = LowercaseValidator.class) //Validation logic
public @interface Lowercase {
	String message() default "must be lowercase";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
