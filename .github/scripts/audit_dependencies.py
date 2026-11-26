"""Check the verified Maven inventory against the public OSV advisory database."""

import json
import re
import sys
from pathlib import Path
from urllib.request import Request, urlopen
from xml.etree import ElementTree


def main():
    root = ElementTree.parse("gradle/verification-metadata.xml").getroot()
    inventory = set()
    for component in root.findall(".//{*}component"):
        group, artifact, version = (component.attrib[key] for key in ("group", "name", "version"))
        inventory.add((f"{group}:{artifact}", version))
    if not inventory:
        raise ValueError("The Maven inventory is empty")

    coordinates = sorted(inventory)
    queries = [{"package": {"ecosystem": "Maven", "name": name}, "version": version}
               for name, version in coordinates]
    request = Request("https://api.osv.dev/v1/querybatch",
                      data=json.dumps({"queries": queries}).encode("utf-8"),
                      headers={"Content-Type": "application/json"})
    with urlopen(request, timeout=40) as response:
        matches = json.load(response)["results"]
    if not isinstance(matches, list) or len(matches) != len(coordinates):
        raise ValueError("OSV returned incomplete results")

    lines = [f"OSV returned results for {len(coordinates)} Maven coordinates."]
    findings = 0
    for (name, version), match in zip(coordinates, matches, strict=True):
        # An empty object represents no matches. A per-query error is not a clean result.
        if not isinstance(match, dict) or set(match) - {"vulns"}:
            raise ValueError("OSV returned an unrecognized query result")
        for advisory in match.get("vulns", []):
            identifier = advisory["id"]
            if not re.fullmatch(r"[A-Za-z0-9-]+", identifier):
                raise ValueError("OSV returned an invalid advisory identifier")
            lines.append(f"{identifier} {name} {version}")
            findings += 1
    lines.append(f"Known advisory matches: {findings}")
    report = Path("build/reports/security/osv.txt")
    report.parent.mkdir(parents=True, exist_ok=True)
    report.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))
    return int(findings != 0)


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, KeyError, TypeError) as error:
        sys.exit(f"Dependency advisory check did not complete: {type(error).__name__}")
