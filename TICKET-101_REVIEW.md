# Ticket 101 Review

## Overview
This document is a summary of implementation of TICKET-101.

### General

- The main objective is achieved.
- Overall, the architecture of a backend is well-built, with clear separation of responsibilities and modular components for
  handling loan decisions.
- Test coverage is comprehensive, with both integration testing, ensuring interaction between system components,
  and scenario testing, validating end-to-end functionality, in place.
- The program code is documented according to javadoc requirements, which in turn, enhances code readability and maintainability.

### Review & Suggestions for improvement

- **Testing**:
  - DecisionEngineControllerTest and DecisionEngineController are compatible.
  - DecisionEngineTest covers all possible scenarios.
  - Consider using more descriptive method names for tests (e.g., `testSegment1PersonalCode` -> `testSegment1_ValidRequest`).
  - Refactor tests to avoid duplicating test scenarios (e.g., `testDebtorPersonalCode` and `testNoValidLoanFound` may overlap).
  - Ensure consistent usage of constants in tests to improve maintainability and readability.

- **Coding:** <br />
  **DesicisonEngineController**<br />
  - Well-defined REST API for managing loan decision requests.
  - Deviation towards Javadoc standards. (FIXED) <br />
  
  **Exceptions**<br />
  - Well done on considering possible scenarios.<br />

  **DesicionEngine**<br />
  - Primitive scoring algorithm is implemented.
  - Error handling is implemented.
  - Adherence to Javadoc standards (However, it may be worth considering why private methods were also documented).
  - Unnecessary complex logic in some places (if (!(... <= ...)) to if (... > ...)) or namings (e.g., `highestValidLoanAmount` -> `maxLoanAmount`). (FIXED)
  - Consider provisions for system expansion (e.g., modifier calculation logic). (FIXED)
  - Refactor error messages to be more self-explanatory (e.g. `No valid loan found!` -> `Loan disapproved: existing debt`/
  `Loan disapproved: maximum loan period exceeded`) (FIXED)

### Analysis of Logic in Terms of SOLID Principles
1. **SRP (Single Responsibility Principle)**
   The *DecisionEngineController.java* handles both incoming requests and responses, suggesting a need for separate components for request and response handling.

2. **OCP (Open/Closed Principle)**
   The *DecisionEngine* class contains hardcoded logic for calculating approved loans, hindering flexibility when adapting to changing business rules.

3. **LSP (Liskov Substitution Principle)**
   N/A (no inheritance present).

4. **ISP (Interface Segregation Principle)**
   Potential for future implementation when expanding to handle more types of loans and decision strategies.

5. **DIP (Dependency Inversion Principle)**
   While *DecisionEngineController* depends on the abstracted *DecisionEngine* service, direct instantiation of *EstonianPersonalCodeValidator* within the *DecisionEngine* class suggests a need for dependency injection.

## Main shortcoming
Despite the backend meeting all requirements, the final solution was not functioning as expected due to issues in the frontend.


## Frontend
### Bugs
1. Typo in minimum months.
2. Mistake in logic of displaying the highest available loan amount, misinterpreting the backend data.

### Fixes
1. Fixed '6 months' to '12 months' as minimum available period.
2. Fixed the logics of interpreting the loan amount.

### Areas for improvement
It would be more comprehensive for the user to have the gaps in selectable loan periods smaller than 6 months.
