"""Create and verify the exact native candidate directory, without publishing it."""

import argparse
import hashlib
import json
import re
import tarfile
import zipfile
from pathlib import Path

VERSION = re.compile(r"(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)")
TARGET = re.compile(r"(?:windows|macos|linux)-(?:x64|arm64)")


def payload_names(version, target):
    if not VERSION.fullmatch(version) or not TARGET.fullmatch(target):
        raise ValueError("Expected numeric SemVer and a supported OS-architecture target")
    suffix = "zip" if target.startswith("windows-") else "tar.gz"
    return sorted([f"bullbear-{version}-{target}.{suffix}", "bom.cdx.json", "candidate.json"])


def read_digests(folder, names):
    digests = []
    for name in names:
        item = folder / name
        if item.is_symlink() or not item.is_file():
            raise ValueError(f"Missing or unsafe candidate file: {name}")
        with item.open("rb") as data:
            digests.append(f"{hashlib.file_digest(data, 'sha256').hexdigest()}  {name}")
    return "\n".join(digests) + "\n"


def runtime_inventory(folder, archive_name):
    bom = json.loads((folder / "bom.cdx.json").read_text(encoding="utf-8"))
    components = [bom["metadata"]["component"], *bom["components"]]
    coordinates = {
        f"{component['name']}-{component['version']}": component["purl"]
        for component in components
    }

    def collect(paths, open_entry):
        result = []
        for path in sorted(paths):
            if not path.endswith(".jar") or "/runtime/" in path:
                continue
            name = path.rsplit("/", 1)[-1]
            stem = name.removesuffix(".jar")
            matching = [prefix for prefix in coordinates
                        if stem == prefix or stem.startswith(prefix + "-")]
            if len(matching) != 1:
                raise ValueError(f"Runtime archive JAR has no unique BOM coordinate: {name}")
            prefix = matching[0]
            entry = {"file": name, "purl": coordinates[prefix]}
            if stem != prefix:
                entry["classifier"] = stem[len(prefix) + 1:]
            with open_entry(path) as data:
                entry["sha256"] = hashlib.file_digest(data, "sha256").hexdigest()
            result.append(entry)
        if not result or len({entry["file"] for entry in result}) != len(result):
            raise ValueError("The runtime classpath is empty or has duplicate names")
        if not any("/runtime/" in name and name.endswith("/release") for name in paths):
            raise ValueError("The candidate does not contain a bundled runtime release record")
        if not any(name.endswith("/THIRD_PARTY_NOTICES.md") for name in paths):
            raise ValueError("The candidate does not contain dependency notices")
        return result

    if archive_name.endswith(".zip"):
        with zipfile.ZipFile(folder / archive_name) as archive:
            return collect(archive.namelist(), archive.open)
    with tarfile.open(folder / archive_name, "r:gz") as archive:
        return collect(archive.getnames(), archive.extractfile)


def verify(folder, version, target):
    names = payload_names(version, target)
    if {entry.name for entry in folder.iterdir()} != {*names, "SHA256SUMS"}:
        raise ValueError("Candidate directory has missing or unexpected files")
    checksums = folder / "SHA256SUMS"
    if checksums.is_symlink() or not checksums.is_file() or checksums.stat().st_size > 4096:
        raise ValueError("Invalid candidate checksum file")
    if checksums.read_text(encoding="ascii") != read_digests(folder, names):
        raise ValueError("Candidate checksum mismatch")
    metadata = json.loads((folder / "candidate.json").read_text(encoding="utf-8"))
    if metadata.get("version") != version or metadata.get("target") != target:
        raise ValueError("Candidate version or target mismatch")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("mode", choices=["create", "verify"])
    parser.add_argument("folder", type=Path)
    parser.add_argument("--version", required=True)
    parser.add_argument("--target", required=True)
    parser.add_argument("--revision")
    parser.add_argument("--jdk")
    parser.add_argument("--gradle")
    parser.add_argument("--run")
    parser.add_argument("--working-tree", action="store_true")
    options = parser.parse_args()

    try:
        folder = options.folder
        if folder.is_symlink() or not folder.is_dir():
            raise ValueError("Candidate must be a real directory")
        names = payload_names(options.version, options.target)
        if options.mode == "create":
            if not options.revision or not re.fullmatch(r"[0-9a-f]{40}", options.revision):
                raise ValueError("An exact Git revision is required")
            if not options.jdk or not options.gradle:
                raise ValueError("JDK and Gradle versions are required")
            if any((folder / name).is_symlink() for name in [*names, "SHA256SUMS"]):
                raise ValueError("Candidate must contain regular files")
            metadata = {
                "version": options.version, "target": options.target,
                "revision": options.revision, "source": "working-tree" if options.working_tree else "commit",
                "jdk": options.jdk, "gradle": options.gradle, "workflowRun": options.run,
                "runtimeClasspath": runtime_inventory(folder, next(
                    name for name in names if name.endswith((".zip", ".tar.gz")))),
            }
            (folder / "candidate.json").write_text(
                json.dumps(metadata, indent=2, sort_keys=True) + "\n", encoding="utf-8")
            (folder / "SHA256SUMS").write_text(read_digests(folder, names), encoding="ascii", newline="\n")
        verify(folder, options.version, options.target)
    except (OSError, ValueError, KeyError, TypeError, tarfile.TarError, zipfile.BadZipFile) as failure:
        parser.exit(1, f"Candidate verification failed: {failure}\n")
    print(f"Verified {options.version} {options.target} candidate.")


if __name__ == "__main__":
    main()
