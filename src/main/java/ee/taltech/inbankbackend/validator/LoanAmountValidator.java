package ee.taltech.inbankbackend.validator;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;

public class LoanAmountValidator implements Validator<Long> {

    @Override
    public void validate(Long loanAmount) throws InvalidLoanAmountException {

        if (loanAmount < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT || loanAmount > DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT) {
            throw new InvalidLoanAmountException("Loan amount is out of range!");
        }
    }
}
