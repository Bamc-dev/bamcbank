# BamcBank

Portfolio project: event-driven bank (Kafka), Java 21, Spring Boot, Maven monorepo.
Current state: phase 1, single module `corebanking/`. Owner is a junior dev who is learning: explain the *why* in answers and PR descriptions.

## Commands
- Build + all tests: `cd corebanking && ./mvnw -B verify`
- One test class: `./mvnw -B test -Dtest=ClassName`

## Architecture (hexagonal)
- The domain package never imports `org.springframework.*`, `jakarta.persistence.*` or Kafka classes. Adapters depend on the domain, never the reverse.
- The domain defines ports (interfaces); adapters (REST, JPA, Kafka) implement or call them.
- Business rules live in the domain only, never in controllers or adapters.
- Only `core-banking-service` changes an account balance.

## Business rules (transfers)
- Available balance = balance - active reservations. Transfers are checked against the available balance.
- Insufficient funds: reject immediately, no transfer is created.
- Creating a transfer reserves the amount. Fraud is checked before any debit.
- The receiver is credited and the ledger is written only on execution.
- Rejected for fraud: the reservation is released.

## Workflow
- TDD: write a failing test first, then the code.
- Never edit, delete or weaken an existing test to make it pass. If a test looks wrong, say so and ask.
- AI proposes (PR or comment). Never merge, never push to `main`.
- Keep changes small and in scope. No new service, topic or framework without being asked.

## Git
- Branch: `<type>/<issue#>-short-description`, lowercase with hyphens, e.g. `feat/6-amount-value-object`.
- Types: feat, fix, core, ci, docs, test, refactor.
- Commits: Conventional Commits with the branch type, e.g. `feat(domain): reject negative amount`.
- One branch = one issue = one PR. PR title uses the same format; body contains `Closes #n`.

## Glossary (code is in English, never mix languages)
Transfer (virement), Account (compte), Amount (montant), Reservation (réservation), balance (solde), availableBalance (solde disponible), FraudVerdict (verdict de fraude), ledger (grand livre).
States: PENDING_FRAUD_CHECK, EXECUTED, REJECTED_FRAUD.
Events: TransferCreated, FraudVerdictIssued, TransferExecuted, TransferRejected.
Topics: `transfers` (key = debited accountId), `fraud.verdicts`.
