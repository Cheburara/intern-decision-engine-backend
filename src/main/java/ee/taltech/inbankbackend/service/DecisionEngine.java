package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.strategy.CreditModifierStrategy;
import ee.taltech.inbankbackend.validator.AgeValidator;
import ee.taltech.inbankbackend.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@Service
public class DecisionEngine {

    private int creditModifier = 0;

    private final CreditModifierStrategy creditModifierStrategy;
    private final Validator<String> personalCodeValidator;
    private final Validator<Long> loanAmountValidator;
    private final Validator<Integer> loanPeriodValidator;
    private final AgeValidator ageValidator;
    private final Validator<String> countryCodeValidator;

    @Autowired
    public DecisionEngine(CreditModifierStrategy creditModifierStrategy,
                          Validator<String> personalCodeValidator,
                          Validator<Long> loanAmountValidator,
                          Validator<Integer> loanPeriodValidator,
                          AgeValidator ageValidator,
                          Validator<String> countryCodeValidator) {
        this.creditModifierStrategy = creditModifierStrategy;
        this.personalCodeValidator = personalCodeValidator;
        this.loanAmountValidator = loanAmountValidator;
        this.loanPeriodValidator = loanPeriodValidator;
        this.ageValidator = ageValidator;
        this.countryCodeValidator = countryCodeValidator;
    }

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 60 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     * REFACTOR
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     *                   REFACTOR
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount (debt) and loan period (max)
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod, String countryCode, String date)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException, AgeRestrictionException {
        LocalDate dateOfBirth = LocalDate.now();
        try {
            if (!date.isEmpty() && !Objects.equals(date, "")) {
                dateOfBirth = LocalDate.parse(date);
            }
            verifyInputs(personalCode, loanAmount, loanPeriod, dateOfBirth, countryCode);
        } catch (Exception | InvalidCountryCodeException e) {
            return new Decision(null, null, null, null, e.getMessage());
        }

        int outputLoanAmount;
        creditModifier = creditModifierStrategy.getCreditModifier(personalCode);

        if (creditModifier == 0) {
            throw new NoValidLoanException("Loan disapproved: existing debt.");
        }

        int maxLoanAmount = maxLoanAmount(loanPeriod);
        while (maxLoanAmount < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
            loanPeriod++;
            maxLoanAmount += creditModifier;
        }

        if (loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            outputLoanAmount = Math.min(DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT, maxLoanAmount);
        } else {
            throw new NoValidLoanException("Loan disapproved: maximum loan period exceeded.");
        }

        return new Decision(outputLoanAmount, loanPeriod, countryCode, dateOfBirth, null);
    }

    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     *
     * @return Largest valid loan amount
     */
    private int maxLoanAmount(int loanPeriod) {
        return creditModifier * loanPeriod;
    }

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is out of range
     * @throws InvalidLoanPeriodException If the requested loan period is out of range
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod, LocalDate dateOfBirth, String countryCode)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            InvalidCountryCodeException, AgeRestrictionException {
        personalCodeValidator.validate(personalCode);
        loanAmountValidator.validate(loanAmount);
        loanPeriodValidator.validate(loanPeriod);
        countryCodeValidator.validate(countryCode);
        ageValidator.validate(dateOfBirth, countryCode);
    }
}
