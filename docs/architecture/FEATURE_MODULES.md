# VizoEdit feature modules

## Implemented source/data foundations
- `media/`: source-backed media library helpers, trim range, frame extraction.
- `timeline/TimelineModel.kt`: validated clips and basic pure timeline operations.
- `projects/`: project document and local project store.
- `render/`: Media3 export/effect factories. The main screen now uses Media3 `Composition` to concatenate an ordered video queue and optionally add a looping independent background-audio sequence; verify on devices.
- `settings/`: canvas presets, color-look metadata, export profile, keyframe track, transform, codec and transition settings.
- `audio/AudioClipSettings.kt`: validated audio clip parameters (DSP integration not implied).
- `subtitles/SrtSubtitle.kt`: SRT import/export codec.
- `overlays/OverlayLayer.kt`: timed overlay metadata.
- `tools/MaskSpec.kt`: mask parameters (rendering integration not implied).
- `transitions/TransitionSettings.kt`: transition configuration (rendering integration not implied).

## Still requiring full implementation/integration
Arbitrary overlapping multi-track compositing and rendered transitions between clips; complete audio mixing/DSP; animated keyframes in the renderer; masks and blend modes; chroma key; reverse/stabilization/flicker reduction; tracking/rotoscoping; GIF/WebM export; subtitle burn-in and SRT UI; project archive/restore of source media; crash-resumable export; and comprehensive real-device tests.

Do not advertise a feature as complete until its UI, persistence, renderer and tests are all connected.


## 0.6.0 integration details
- UI path: `MainActivity.kt` → Android multi-document video picker → reorder/remove queue → `EditedMediaItemSequence.withAudioAndVideoFrom(...)` → `Composition.Builder(...)` → `Transformer.start(composition, ...)`.
- Music path: Android audio document picker → audio-only `EditedMediaItemSequence` set to loop → `GainProcessor` for music level → same composition as video sequence.
- Not included in this step: video crossfades, overlapping video/PIP tracks, keyframes, or storage of the full queue in saved project documents.
