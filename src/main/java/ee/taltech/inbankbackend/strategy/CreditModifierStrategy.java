package ee.taltech.inbankbackend.strategy;

public interface CreditModifierStrategy {
    int getCreditModifier(String personalCode);
}
