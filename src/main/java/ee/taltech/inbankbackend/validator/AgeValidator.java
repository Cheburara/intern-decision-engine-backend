package ee.taltech.inbankbackend.validator;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.AgeRestrictionException;

import java.time.LocalDate;
import java.time.Period;

import static ee.taltech.inbankbackend.config.DecisionEngineConstants.*;

public class AgeValidator {

    public void validate(LocalDate dateOfBirth, String countryCode) throws AgeRestrictionException {
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        int maxAge = COUNTRY_LIFE_EXPECTANCY.getOrDefault(countryCode, 75) - DecisionEngineConstants.MAXIMUM_LOAN_PERIOD / 12;
        if (age < MIN_AGE) {
            throw new AgeRestrictionException("You are too young!"); // REFACTOR
        }
        if (age > maxAge) {
            throw new AgeRestrictionException((age + " OLDDDD " + maxAge )); // REFACTOR
        }
    }
}