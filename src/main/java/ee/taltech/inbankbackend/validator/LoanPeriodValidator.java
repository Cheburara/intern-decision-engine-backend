package ee.taltech.inbankbackend.validator;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;

public class LoanPeriodValidator implements Validator<Integer> {

    @Override
    public void validate(Integer loanPeriod) throws InvalidLoanPeriodException {
        if (loanPeriod < DecisionEngineConstants.MINIMUM_LOAN_PERIOD || loanPeriod > DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            throw new InvalidLoanPeriodException("Loan period is out of range!");
        }
    }
}
