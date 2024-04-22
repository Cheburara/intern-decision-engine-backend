package ee.taltech.inbankbackend.validator;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import org.springframework.stereotype.Component;

@Component
public class PersonalCodeValidator implements Validator<String> {

    private EstonianPersonalCodeValidator estonianPersonalCodeValidator;

    public PersonalCodeValidator() {
        this.estonianPersonalCodeValidator = new EstonianPersonalCodeValidator();
    }

    @Override
    public void validate(String personalCode) throws InvalidPersonalCodeException {

        if (!estonianPersonalCodeValidator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
    }
}
