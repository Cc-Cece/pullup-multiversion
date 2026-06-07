# PullUp Multiversion

`PullUp` 的持续维护多版本分支，目标是在同一套 `Stonecutter + Stonecraft` 工程里覆盖 Fabric、Forge、NeoForge 的多条 Minecraft 发布线。

## Status

- Mod version: `2.1.1`
- Modeled build targets: `26`
- Covered exact Minecraft versions: `28`
- Matrix metadata lives in:
  - `versions/targets.json`
  - `versions/release-lines.json`
  - `versions/quirks.json`
  - `versions/dependencies/*.properties`

Current D1 progress is split into two layers:

- Matrix/modeling is in place for `1.18.2` through `26.1.2`.
- Grouped local compile evidence is now green for the legacy anchors, new Fabric lines, new Forge lines, new NeoForge lines, and `26.1` on JDK `25`.
- Grouped local packaging/artifact evidence is also green across Fabric, Forge, NeoForge, and `26.1`.
- Internal test jars can now be produced locally; per-release-line runtime validation records are the main unfinished D1 item before a public-ready release claim.

## Release Lines

The repository now models these anchor targets:

- Fabric: `1.18.2`, `1.19.2`, `1.19.4`, `1.20.1`, `1.20.2`, `1.20.4`, `1.20.6`, `1.21.1`, `1.21.3`, `1.21.5`, `1.21.8`, `1.21.11`, `26.1`
- Forge: `1.18.2`, `1.19.2`, `1.19.4`, `1.20.1`
- NeoForge: `1.20.2`, `1.20.4`, `1.20.6`, `1.21.1`, `1.21.3`, `1.21.5`, `1.21.8`, `1.21.11`, `26.1`

Exact supported-version declarations are derived from `versions/release-lines.json` and expanded into mod metadata during the build.

## Validation

Local validation entry points:

```powershell
./scripts/validate-version-matrix.ps1
./gradlew.bat projects --no-daemon "-PtargetProjects=1.20.1-fabric,1.19.2-fabric,1.21.11-neoforge"
./gradlew.bat :1.19.2-fabric:compileJava --no-daemon "-PtargetProjects=1.19.2-fabric"
./gradlew.bat :1.20.1-fabric:compileJava --no-daemon "-PtargetProjects=1.20.1-fabric"
./gradlew.bat :1.20.2-neoforge:compileJava --no-daemon "-PtargetProjects=1.20.2-neoforge"
./gradlew.bat :1.21.11-neoforge:compileJava --no-daemon "-PtargetProjects=1.21.11-neoforge"
./gradlew.bat :1.20.1-forge:remapJar --no-daemon "-PtargetProjects=1.20.1-forge"
./gradlew.bat :1.21.1-fabric:remapJar --no-daemon "-PtargetProjects=1.21.1-fabric"
./gradlew.bat :1.21.1-neoforge:remapJar --no-daemon "-PtargetProjects=1.21.1-neoforge"
```

For the `26.1` targets, switch to JDK `25` first:

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.3"
./gradlew.bat :26.1-neoforge:compileJava --no-daemon "-PtargetProjects=26.1-neoforge"
./gradlew.bat :26.1-neoforge:jar --no-daemon "-PtargetProjects=26.1-neoforge"
```

GitHub Actions workflows:

- `.github/workflows/ci-fast-matrix.yml`
- `.github/workflows/ci-full-matrix.yml`

The fast workflow validates metadata and a smoke compile subset. The full workflow is wired for manual matrix compilation across all release-line groups.

Local grouped compile logs captured under `.docs/D1/compile-logs/`:

- `20260607-145831-legacy-core-jdk21.log`
- `20260607-145958-legacy-26.1-jdk25.log`
- `20260607-152538-forge-new-lines-jdk21-green.log`
- `20260607-152726-neoforge-new-lines-jdk21-green2.log`
- `20260607-154259-fabric-new-lines-jdk21-green2.log`

Local grouped packaging logs captured under `.docs/D1/compile-logs/`:

- `20260607-155431-package-legacy-core-jdk21-retry.log`
- `20260607-155250-package-forge-new-lines-jdk21.log`
- `20260607-160308-package-neoforge-new-lines-jdk21-green.log`
- `20260607-160615-package-fabric-new-lines-jdk21-green2.log`
- `20260607-155250-package-v26-jdk25.log`

## Java Requirements

- Most current anchor targets build under JDK `21`.
- `26.1` targets require JDK `25`.

## Upstream and License

- Upstream project: `MUYU_Twilighter/PullUp`
- See `LICENSE`, `LICENSE.upstream-MIT`, and `NOTICE.md`
