package ee.taltech.inbankbackend.validator;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.AgeRestrictionException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

import static ee.taltech.inbankbackend.config.DecisionEngineConstants.*;

@Component
public class AgeValidator {

    public void validate(LocalDate dateOfBirth, String countryCode) throws AgeRestrictionException {
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        int maxAge = COUNTRY_LIFE_EXPECTANCY.getOrDefault(countryCode.toUpperCase(), 75) - DecisionEngineConstants.MAXIMUM_LOAN_PERIOD / 12;
        if (dateOfBirth.equals(LocalDate.now())) {
            throw new AgeRestrictionException("Please, enter the date");
        }
        if (age < MIN_AGE) {
            throw new AgeRestrictionException("You are too young!"); // REFACTOR
        }
        if (age > maxAge) {
            throw new AgeRestrictionException((age + " OLDDDD " + maxAge )); // REFACTOR
        }
    }
}
