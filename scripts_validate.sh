#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
if ! command -v kotlinc >/dev/null 2>&1; then
  echo "kotlinc is required for the pure Kotlin checks" >&2
  exit 2
fi
OUT="${TMPDIR:-/tmp}/vizoedit-domain-validation.jar"
kotlinc \
  "$ROOT/app/src/main/java/com/vizoedit/studio/settings/EditorSettings.kt" \
  "$ROOT/app/src/main/java/com/vizoedit/studio/settings/ColorLookPreset.kt" \
  "$ROOT/app/src/main/java/com/vizoedit/studio/settings/OutputCodec.kt" \
  "$ROOT/app/src/main/java/com/vizoedit/studio/settings/ExportProfile.kt" \
  "$ROOT/app/src/main/java/com/vizoedit/studio/settings/KeyframeTrack.kt" \
  "$ROOT/app/src/main/java/com/vizoedit/studio/settings/MediaTransform.kt" \
  "$ROOT/app/src/main/java/com/vizoedit/studio/settings/TransitionSpec.kt" \
  "$ROOT/app/src/main/java/com/vizoedit/studio/settings/CanvasPreset.kt" \
  "$ROOT/app/src/main/java/com/vizoedit/studio/timeline/TimelineModel.kt" \
  "$ROOT/app/src/main/java/com/vizoedit/studio/media/TrimRange.kt" \
  -d "$OUT"
echo "Pure Kotlin domain compilation passed: $OUT"
echo "Run the GitHub Actions Android CI workflow for the full Gradle build and JUnit suite."
