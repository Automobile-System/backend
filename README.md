# Backend

## Testing

This module now ships with dedicated unit and integration tests plus JaCoCo coverage reporting.

### Run the full suite

```bash
mvn clean test
```

### Generate a coverage report

Coverage is produced automatically during the `verify` phase:

```bash
mvn clean verify
```

Open `backend/target/site/jacoco/index.html` for the HTML report. Raw test results are located in `backend/target/surefire-reports/`.