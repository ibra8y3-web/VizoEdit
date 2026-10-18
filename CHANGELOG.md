# Changelog

## 0.2.0 — editing workflow update

- Added a playhead slider, jump-to-start, and approximate 33 ms seek buttons.
- Added playhead-derived trim-in and trim-out actions, trim reset, and bounded undo/redo history.
- Added editable project names and a saved-project list with metadata restore when the media source URI remains accessible.
- Added MediaStore video-library copy to `Movies/VizoEdit` on Android 10+.
- Added secure sharing of exported MP4 files using AndroidX FileProvider; sharing no longer requires a gallery copy first.
- Added FileProvider path configuration and Unicode-aware project filenames.
- Added unit tests covering trim clamping, validation, history undo/redo, and branching after undo.
- Updated CI to execute local unit tests before building the APK.

## Known constraints

- A full Android Gradle build and physical-device validation have not been completed in the authoring environment.
- Seek-by-33-ms is approximate; exact frame stepping must read the media frame rate/time base.
- A project document stores its source URI, not a copy of source media. Reopening depends on the source URI still being readable.
- Timeline compositing, multi-clip projects, text/sticker rendering, audio mixing, effect shaders, color grading, export presets, and batch export remain unimplemented.

## 0.3.0 — نماذج تحرير إضافية
- إضافة نموذج TimelineClip مع تحقق صارم للمدد، ومصدر الوسائط، والمسار، ومستوى الصوت.
- إضافة عمليات نموذجية لتقسيم المقطع، ونقله، وحذفه، وترتيب قائمة المسارات.
- إضافة إعدادات نسب أبعاد قياسية (16:9، 9:16، 1:1، 4:5، 4:3، 21:9).
- إضافة نموذج إعدادات صوت مع التحقق من مستويات الصوت ومدد التلاشي.
- إضافة اختبارات وحدة لهذه العمليات.
- هذه إضافة لطبقة بيانات التحرير وليست ادعاءً بأن المحرك متعدد المسارات أو مزج الصوت قد اكتمل.


## 0.4.0 — ألوان وتصدير متقدم
- إضافة وصفات ألوان فعلية: أصلي، حيوي، سينمائي، دافئ، بارد، أبيض وأسود، باهت، قديم، سيبيا، ليلي، وغروب. تُحوّل الوصفات إلى مؤثرات Media3 أثناء التصدير.
- إضافة إعدادات H.264/H.265 مع إعدادات bitrate مطلوبة للفيديو وAAC للصوت، مع السماح لمحرك Media3 بالرجوع إلى ترميز متوافق إذا لم يدعم الجهاز الخيار المحدد.
- إضافة واجهة اختيار وصفات اللون والترميز ومعدل بيانات الفيديو والصوت.
- إضافة ملصقات Emoji سريعة باعتبارها طبقة نصية/رمزية ثابتة تُدمج عبر TextOverlay أثناء التصدير، مع اختيار اللون والشفافية والدوران والحجم والموضع.
- حفظ إعدادات اللون والترميز ومعدلات البيانات ضمن ملف المشروع مع قيم افتراضية رجعية للملفات القديمة.
- إضافة اختبارات تحقق لوصفات الألوان وخيارات الترميز ومعدلات البيانات وخصائص الطبقة النصية.

## Known constraints
- ما زال البناء الكامل عبر Gradle/Android SDK واختبار التصدير على جهاز فعلي مطلوبين قبل اعتبار هذه النسخة صالحة للإصدار.
- الملصقات الحالية نص/Emoji ثابت طوال مدة المقطع؛ لا يوجد بعدُ تحريك موضع أو توقيت مستقل للملصق، ولا استيراد PNG/GIF.
- وصفات اللون تُطبق أثناء التصدير ولا توفر بعد معاينة مؤثرات مباشرة على مشغل الفيديو.
- لا يوجد حتى الآن تركيب فيديو متعدد المقاطع أو مسارات متعددة في تصدير Composition واحد؛ نموذج timeline موجود كطبقة بيانات فقط.

## 0.5.0 — Editing data modules
- Added validated audio clip parameters (gain, trim, fades, loop/reverse intent and pitch range).
- Added strict SRT subtitle parser/writer with timestamp validation.
- Added overlay layer model for text/images/emoji/shapes/subtitles/logos with transform, opacity and blend-mode metadata.
- Added mask geometry model and transition configuration/easing enums.
- Added unit tests for the new data models.
- Scope note: these are real validated editing models, but not every option is yet connected to the UI or media renderer. Reverse audio, pitch processing, masks, blend modes, and transitions must not be represented as rendered features until implemented and device-tested.


## 0.6.0 — Merge queue and music sequence
- Added Android multi-document video import and a reorderable/removable clip queue.
- Connected queue export to Media3 `EditedMediaItemSequence` and `Composition`, rendering the queued clips sequentially into a single MP4.
- Added an optional, looping background-audio sequence mixed alongside clip audio, with an independent gain control.
- Applies the current trim to the queued source matching the active preview; other queue items are included in full.
- Limits are explicit: no overlapping crossfades or keyframed transitions, no multi-clip project persistence yet, and no full Gradle/device test was run in this environment.

## 0.7.0 — Additional editor core models
- Added validated piecewise-linear speed curve model (0.25x–4x) for speed-ramping UI and processing integration.
- Added validated chroma-key settings for key color, similarity, smoothness, spill suppression, and edge feather.
- Added timeline marker model and nearest-target snapping utility.
- Added nullable media metadata model so unknown codec/duration values are not fabricated.
- Added unit tests for speed curve interpolation, chroma-key validation, and timeline snapping.
- These are core models; chroma-key rendering, variable-speed export, and metadata probing must still be wired to the UI/render pipeline before those features can be called complete.
