# Bullbear engineering runbook

## Local gates

Use Temurin Java 21 and the repository wrapper. Run each command from the repo root. On Windows PowerShell replace `./gradlew` with `.\gradlew.bat`.

```sh
./gradlew clean check build :desktop:installDist
./gradlew check --configuration-cache
./gradlew :desktop:run --args=--smoke-test
./gradlew :desktop:run --args=--thai-preview
./gradlew :desktop:run --args=--diagnostics
python .github/scripts/audit_dependencies.py
```

`check` runs the original JUnit suite, new deterministic and temporary-directory integration cases, Spotless, architecture, JaCoCo and high-confidence SpotBugs checks. Reports live in each module under `build/test-results/test`, `build/reports/jacoco/test` and `build/reports/spotbugs`. Line floors are 90% for domain/application, 10% for presentation, and 65% for desktop. Branch floors are 8% for presentation and 55% for desktop. Presentation's GL-dependent code is covered by separate native smoke and visual checks.

The normal build enables the local task output cache. Configuration cache remains opt-in with `--configuration-cache` on verified graphs. Use the ordinary wrapper command if a specialized task is incompatible. Keep untrusted PR cache writes disabled.

## Runtime settings and logs

Windows uses `%LOCALAPPDATA%/Bullbear` (home AppData fallback). macOS uses `~/Library/Application Support/Bullbear`. Linux uses `$XDG_CONFIG_HOME/bullbear` and `$XDG_STATE_HOME/bullbear`, falling back to `~/.config/bullbear` and `~/.local/state/bullbear`. Relative environment overrides fall back to the platform location.

The first launch creates `settings.properties` if missing. Use UTF-8, keep all seven fields, and restart after editing. Version 1 supports `width` from 800 to 7680, `height` from 450 to 4320, `locale` as `en` or `th`, `audio` and `diagnostics` as booleans, and finite `volume` from 0 to 1. The default is 1280 × 720, English, silent, and no diagnostics. `--diagnostics` takes precedence for that session. Thai remains a separate preview and audio preferences are retained for a later asset-backed release.

Validation errors use safe defaults for the session and preserve the original settings. Unknown formats or keys are rejected. Writes use an atomic sibling replacement. On a filesystem that cannot commit that operation, the last good target remains and the write reports failure. Direct links at the app-managed directory or settings file are rejected. Parent directories supplied by the OS are trusted. The settings file is the versioning fixture for future format readers.

The runtime owns one stage and the skin it borrows. The preview owns its stage and one immutable texture. The shared font, generated solid texture and private batch each have one disposal path. The logical viewport stays 1280 × 720. libGDX maps logical size to the framebuffer. The diagnostics flag samples dimensions and timing once per second.

Local `logs/` contains rotating UTF-8 logs and at most one `last-crash.txt` per state directory. Reports record exception types and bounded cause frames. Application-captured logs stay local. Review manually before sharing. Driver and JVM fatal errors can use separate platform logs.

### Thai technical preview

The packaged PNG exercises ten fixed examples through a Noto Sans Thai Looped v2.000, libraqm and HarfBuzz pipeline. The English workspace uses its original font. The preview establishes rasterized label rendering and mark placement on checked hosts. Dynamic shaping, wrapping or Thai input require separate validation.

To regenerate, use `tools/render_thai_preview.py FONT_TTF OUTPUT_PNG` with the full regular TTF from the official release and Pillow with RAQM. The generator verifies the pinned TTF SHA-256. The font itself is a generation input. [Official Noto release](https://github.com/notofonts/thai/releases/tag/NotoSansThaiLooped-v2.000)

## Security and dependency changes

1. Review the version catalog, original repositories and selected native platform classifiers. Regenerate affected lockfiles and verification metadata with `--write-locks --write-verification-metadata sha256` only for an intentional maintenance build. Resolve `check`, `:desktop:build` and `:desktop:cyclonedxDirectBom` for the changed graph.
2. Cross-check added hashes against upstream checksums or trusted repository bytes. Review the diff and rerun the build in strict mode with neither write flag. Preserve original license notices.
3. Run the OSV check and review GitHub Dependency Review and CodeQL on the PR. The OSV gate fails on any matching advisory or incomplete response. It covers the pinned Maven inventory. Native code bundled inside JARs, the JDK, the Gradle distribution and Actions require vendor advisory review.
4. Run Gitleaks on scoped files and reachable history with redaction. Revoke any leak before remediation. Apply the existing branch-review rules. The stable required status is `CI gate`. Keep bypass limited to the existing review ruleset.

The Dependabot configuration proposes Gradle and Actions upgrades weekly. A maintainer reviews updates; the coded PR check remains active. Check the GitHub Security settings for dependency graph and push protection. Gradle transitive graph ingestion requires a separate trusted dependency submission configuration. The Maven inventory scan covers local verified coordinates independently.

## Unsigned native candidates

Build on the target OS and architecture with the full Java 21 JDK, `jpackage`, `jlink`, and system `tar`.

```sh
./gradlew clean check :desktop:archiveAppImage :desktop:cyclonedxDirectBom
```

The app-image is under `desktop/build/native-image/`, candidate archive under `desktop/build/candidate/`, and the CycloneDX 1.6 runtime SBOM under `desktop/build/reports/cyclonedx-direct/bom.json`. Copy the BOM to `desktop/build/candidate/bom.cdx.json`, then create and verify the manifest with `.github/scripts/candidate_manifest.py`. Supply `--version` from `:desktop:printAppVersion`, the actual `--target`, Git `--revision` and JDK/Gradle versions. Add `--working-tree` for a modified checkout. The manifest includes per-JAR hashes and native classifiers. Windows emits ZIP, Unix emits tar.gz to keep file mode and symlinks. A candidate includes Java; the conventional Gradle ZIP requires system Java.

The manual **Native release candidate** workflow uses the same numeric version as Gradle. It builds candidates on the four named runners and checks the bundled launcher. Optional GitHub attestations run only on protected `main`, after all targets succeed, using OIDC. Verify with `gh attestation verify FILE --repo ParkPawapon/project-bullbear --signer-workflow ParkPawapon/project-bullbear/.github/workflows/candidate.yml --source-ref refs/heads/main`. Check the expected source revision and digest. Verify local checksums with `sha256sum -c SHA256SUMS`, macOS `shasum -a 256 -c SHA256SUMS`, or Windows `Get-FileHash -Algorithm SHA256`.

Owner review precedes any publication. Confirm redistribution notices, native GUI tests, signing/notarization and installer prerequisites (WiX, signing tools, or Linux package tooling as applicable). Use a release tag matching Gradle's version and the verified protected revision. Withdraw a faulty release and fix forward under a new version. Do not replace existing assets silently.
