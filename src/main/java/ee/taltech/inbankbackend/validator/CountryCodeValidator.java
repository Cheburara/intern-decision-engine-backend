package ee.taltech.inbankbackend.validator;

import static ee.taltech.inbankbackend.config.DecisionEngineConstants.COUNTRY_LIFE_EXPECTANCY;

import ee.taltech.inbankbackend.exceptions.InvalidCountryCodeException;
import org.springframework.stereotype.Component;

@Component
public class CountryCodeValidator implements Validator<String> {

    @Override
    public void validate(String countryCode) throws InvalidCountryCodeException {
        if (!COUNTRY_LIFE_EXPECTANCY.containsKey(countryCode)) {
            throw new InvalidCountryCodeException("Service is currently not available in your country!");
        }
    }
}
