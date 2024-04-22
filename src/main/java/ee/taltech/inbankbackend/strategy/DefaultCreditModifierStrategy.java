package ee.taltech.inbankbackend.strategy;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;

public class DefaultCreditModifierStrategy implements CreditModifierStrategy {
    @Override
    public int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < 2500) {
            return 0;
        } else if (segment < 5000) {
            return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }
}
