# Contributing to Bullbear

Use a two-letter work code and three digits for feature branches, such as `BE001` for domain/application, `FE001` for presentation, `DE001` for desktop, `CI001` for CI and delivery, `SE001` for security, or `DO001` for documentation. Increment each prefix independently and choose the code that describes the work.

Use Conventional Commit subjects for every commit. Examples include `feat(navigation): add the bank route`, `fix(desktop): handle invalid options`, and `docs(readme): clarify Java prerequisites`. The contribution check reads each subject in the PR and validates its branch code.

## Protected main

Open a pull request against `main`. GitHub uses rebase merges to keep a linear history and preserve each typed subject. Required checks must pass on a branch that is current with `main`. Review conversations must be resolved. Direct pushes, force pushes, and deletion are restricted by the active rulesets.

The CI gate includes the four-platform build and tests, Linux graphics smoke, PR contribution policy, CodeQL Java analysis, and PR dependency review. Repository rules pin the required status to GitHub Actions. Workflow actions use full release commit SHAs. Pull request workflow tokens have read access, with the CodeQL job allowed to upload security events.

`@ParkPawapon` owns all paths. The review ruleset grants only that account the pull-request review exception. The independent integrity ruleset applies to every account. Turn on auto-merge for a ready PR and let GitHub enforce the requirements. If GitHub still asks for Code Owner review on a PR from the sole Code Owner, the owner can merge using the PR review exception after the required checks succeed.

## Delivery and security reports

A successful protected `main` pipeline saves a per-platform ZIP and its SHA-256 file in the [Actions run](https://github.com/ParkPawapon/project-bullbear/actions/workflows/ci.yml). Extract the artifact bundle, compare the ZIP checksum, and run the launcher for the selected OS. The ZIP requires a Java 21 runtime.

Follow the [security policy](SECURITY.md) to report a vulnerability privately. See the [active rulesets](https://github.com/ParkPawapon/project-bullbear/rules) for enforcement details.
