package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.strategy.CreditModifierStrategy;
import ee.taltech.inbankbackend.strategy.DefaultCreditModifierStrategy;
import ee.taltech.inbankbackend.validator.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DecisionEngineTest {

    private final String ELIGIBLE_DATE_OF_BIRTH = LocalDate.now().minusYears(32).toString();
    private final String ESTONIAN_COUNTRY_CODE = "EE";

    @InjectMocks
    private DecisionEngine decisionEngine;

    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String segment2PersonalCode;
    private String segment3PersonalCode;

    @BeforeEach
    void setUp() {
        initializePersonalCodes();
        initializeDecisionEngine();
    }

    private void initializePersonalCodes() {
        debtorPersonalCode = "37605030299";
        segment1PersonalCode = "50307172740";
        segment2PersonalCode = "38411266610";
        segment3PersonalCode = "35006069515";
    }

    private void initializeDecisionEngine() {
        CreditModifierStrategy creditModifierStrategy = new DefaultCreditModifierStrategy();
        Validator<String> personalCodeValidator = new PersonalCodeValidator();
        Validator<Long> loanAmountValidator = new LoanAmountValidator();
        Validator<Integer> loanPeriodValidator = new LoanPeriodValidator();
        AgeValidator ageValidator = new AgeValidator();
        Validator<String> countryCodeeValidator = new CountryCodeValidator();

        decisionEngine = new DecisionEngine(
                creditModifierStrategy,
                personalCodeValidator,
                loanAmountValidator,
                loanPeriodValidator,
                ageValidator,
                countryCodeeValidator
        );
    }

    @Test
    void testDebtorPersonalCode() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 4000L, 12,
                        ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH));
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 2000L, 60,
                        ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH));
    }

    @Test
    void testSegment1PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, AgeRestrictionException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, 12,
                ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH);
        assertEquals(2000, decision.getLoanAmount());
        assertEquals(20, decision.getLoanPeriod());
    }

    @Test
    void testSegment2PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, AgeRestrictionException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 4000L, 12,
                ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH);
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testSegment3PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, AgeRestrictionException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode, 4000L, 12,
                ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH);
        assertEquals(DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testInvalidPersonalCode() {
        String invalidPersonalCode = "12345678901";
        assertThrows(InvalidPersonalCodeException.class,
                () -> decisionEngine.calculateApprovedLoan(invalidPersonalCode, 4000L, 12,
                        ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH));
    }

    @Test
    void testInvalidLoanAmount() {
        Long tooLowLoanAmount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT - 1L;
        Long tooHighLoanAmount = DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT + 1L;

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooLowLoanAmount, 12,
                        ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH));

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooHighLoanAmount, 12,
                        ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH));
    }

    @Test
    void testInvalidLoanPeriod() {
        int tooShortLoanPeriod = DecisionEngineConstants.MINIMUM_LOAN_PERIOD - 1;
        int tooLongLoanPeriod = DecisionEngineConstants.MAXIMUM_LOAN_PERIOD + 1;

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, tooShortLoanPeriod,
                        ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH));

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, tooLongLoanPeriod,
                        ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH));
    }

    @Test
    void testFindSuitableLoanPeriod() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, AgeRestrictionException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 2000L, 12,
                ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH);
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());

        Decision decision1 = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 2000L, 12,
                ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH);
        assertEquals(2000, decision1.getLoanAmount());
        assertEquals(20, decision1.getLoanPeriod());
    }

    @Test
    void testNoValidLoanFound() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 10000L, 60,
                        ESTONIAN_COUNTRY_CODE, ELIGIBLE_DATE_OF_BIRTH));
    }

    @Test
    void testYoungIneligibleDateOfBirth_ThrowsAgeRestrictionException() {

        String YOUNG_INELIGIBLE_DATE_OF_BIRTH = LocalDate.now().minusYears(17).toString();
        assertThrows(AgeRestrictionException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, 12,
                        ESTONIAN_COUNTRY_CODE.toUpperCase(), YOUNG_INELIGIBLE_DATE_OF_BIRTH));
    }

    @Test
    void testOldIneligibleDateOfBirth_ThrowsAgeRestrictionException() {
        String OLD_INELIGIBLE_DATE_OF_BIRTH = LocalDate.now().minusYears(80).toString();
        assertThrows(AgeRestrictionException.class,
                () -> decisionEngine.calculateApprovedLoan(segment2PersonalCode, 4000L, 12,
                        ESTONIAN_COUNTRY_CODE.toUpperCase(), OLD_INELIGIBLE_DATE_OF_BIRTH));
    }

    @Test
    void testUnsupportedCountryCode_ThrowsInvalidCountryCodeException() {
        String unsupportedCountryCode = "AA";
        CountryCodeValidator validator = new CountryCodeValidator();

        InvalidCountryCodeException exception = assertThrows(InvalidCountryCodeException.class,
                () -> validator.validate(unsupportedCountryCode));
    }
    
    @Test
    void testSupportedCountryCodes_DoesNotThrowException() {
        String[] validCountryCodes = {"LV", "LT"};
        CountryCodeValidator validator = new CountryCodeValidator();

        for (String validCountryCode : validCountryCodes) {
            assertDoesNotThrow(() -> validator.validate(validCountryCode));
        }
    }


}

