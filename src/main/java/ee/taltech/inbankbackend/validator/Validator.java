package ee.taltech.inbankbackend.validator;

import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;

public interface Validator<T> {

    void validate(T input) throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException;
}
