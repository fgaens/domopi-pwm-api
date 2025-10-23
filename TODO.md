# TODO

## Automate Hardware System Tests with GitHub Actions

### Overview
Automate the hardware system tests on a spare Raspberry Pi using GitHub Actions self-hosted runner.

### Current State
- System tests exist in `service-st` module (`PwmSystemTest.java`)
- Tests are conditionally enabled with `pwm.system-test.enabled=true`
- Tests verify PWM hardware integration on actual Raspberry Pi
- Currently run manually on hardware

### Goal
Automate the CI/CD pipeline to:
1. Run build + unit + integration tests on GitHub-hosted runners (PR/push)
2. Deploy to spare Raspberry Pi after merge to main
3. Run hardware system tests automatically on spare Pi
4. Provide confidence before manual deployment to production

### Proposed Workflow

```
PR/Push → GitHub Runner (fast)
          ├─ Build
          ├─ Unit tests (mocked hardware)
          └─ Integration tests (RestAssured)

Merge to main → Self-hosted Pi Runner
                ├─ Download artifact
                ├─ Start service
                ├─ Run system tests (real hardware)
                └─ Stop service
```

### Implementation Steps

#### 1. Set up self-hosted runner on spare Raspberry Pi
- Follow GitHub guide: Settings → Actions → Runners → New self-hosted runner
- Choose: Linux ARM64
- Label the runner: `raspberry-pi`
- Ensure Java 25 is installed on the Pi

#### 2. Create/update GitHub Actions workflow
Split into two jobs:
- `build-and-test`: Runs on `ubuntu-latest` (all PRs and pushes)
- `hardware-test`: Runs on `[self-hosted, linux, ARM64, raspberry-pi]` (only on main branch)

#### 3. Configure artifact upload/download
- Build job uploads `quarkus-app` artifact
- Hardware test job downloads and runs it

### Benefits
- Fast feedback on PRs (no hardware needed)
- Automated hardware validation before production deployment
- Catch Pi4J/GPIO issues early
- No manual testing on spare Pi required

### Considerations
- Spare Pi must remain online and connected to GitHub
- Security: Runner has access to repository secrets
- Maintenance: Java version must stay in sync across environments
- Alternative: Use `workflow_dispatch` for manual trigger if always-on Pi not desired

### References
- Current workflow: `.github/workflows/maven-verify.yml`
- System tests: `service-st/src/test/java/be/codesolutions/pwm/boundary/PwmSystemTest.java`
- GitHub Actions docs: https://docs.github.com/en/actions/hosting-your-own-runners