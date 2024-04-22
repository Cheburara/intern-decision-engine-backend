package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import ee.taltech.inbankbackend.strategy.CreditModifierStrategy;
import ee.taltech.inbankbackend.validator.Validator;
import org.springframework.stereotype.Service;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@Service
public class DecisionEngine {

    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private int creditModifier = 0;

    private CreditModifierStrategy creditModifierStrategy;
    private Validator<String> personalCodeValidator;
    private Validator<Long> loanAmountValidator;
    private Validator<Integer> loanPeriodValidator;

    public DecisionEngine(CreditModifierStrategy creditModifierStrategy,Validator<String> personalCodeValidator,  Validator<Long> loanAmountValidator, Validator<Integer> loanPeriodValidator) {
        this.creditModifierStrategy = creditModifierStrategy;
        this.personalCodeValidator = personalCodeValidator;
        this.loanAmountValidator = loanAmountValidator;
        this.loanPeriodValidator = loanPeriodValidator;
    }

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 60 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount (debt) and loan period (max)
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException {
        try {
            verifyInputs(personalCode, loanAmount, loanPeriod);
        } catch (Exception e) {
            return new Decision(null, null, e.getMessage());
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

        return new Decision(outputLoanAmount, loanPeriod, null);
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
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {
        personalCodeValidator.validate(personalCode);
        loanAmountValidator.validate(loanAmount);
        loanPeriodValidator.validate(loanPeriod);
    }
}
