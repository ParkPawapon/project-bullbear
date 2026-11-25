"""Check branch names and the subjects of commits in a pull request."""

import json
import os
import re
import subprocess
import sys
from pathlib import Path

BRANCH = re.compile(r"[A-Z]{2}[0-9]{3}")
SHA = re.compile(r"[0-9a-f]{40}")
SUBJECT = re.compile(
    r"(build|chore|ci|docs|feat|fix|perf|refactor|revert|style|test)"
    r"(\([a-z0-9][a-z0-9._/-]*\))?!?: [^\s].*"
)


def main() -> int:
    event = json.loads(Path(os.environ["GITHUB_EVENT_PATH"]).read_text(encoding="utf-8"))
    pull_request = event["pull_request"]
    branch = pull_request["head"]["ref"]
    base_sha = pull_request["base"]["sha"]
    head_sha = pull_request["head"]["sha"]

    if not BRANCH.fullmatch(branch):
        print("Use two uppercase letters followed by three digits, such as CI001.")
        return 1
    if not SHA.fullmatch(base_sha) or not SHA.fullmatch(head_sha):
        print("The pull request event contains an invalid Git revision.")
        return 1

    subjects = subprocess.run(
        ["git", "log", "--format=%s", f"{base_sha}..{head_sha}"],
        check=True,
        capture_output=True,
        text=True,
        encoding="utf-8",
    ).stdout.splitlines()
    invalid_count = sum(not SUBJECT.fullmatch(subject) for subject in subjects)
    if not subjects or invalid_count:
        print(f"Checked {len(subjects)} commit(s). Invalid subjects: {invalid_count}.")
        print("Use a Conventional Commit subject such as fix(desktop): correct startup.")
        return 1

    print(f"Validated the coded branch name and {len(subjects)} commit subject(s).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
