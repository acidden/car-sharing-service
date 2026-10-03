package com.example.carsharingservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.RecordComponent;
import java.util.Objects;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        firstFieldName = constraintAnnotation.first();
        secondFieldName = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        if (!value.getClass().isRecord()) {
            return false;
        }
        try {
            Object firstObj = null;
            Object secondObj = null;

            for (RecordComponent component : value.getClass().getRecordComponents()) {
                if (component.getName().equals(firstFieldName)) {
                    firstObj = component.getAccessor().invoke(value);
                }
                if (component.getName().equals(secondFieldName)) {
                    secondObj = component.getAccessor().invoke(value);
                }
            }
            return Objects.equals(firstObj, secondObj);
        } catch (Exception e) {
            return false;
        }
    }
}
