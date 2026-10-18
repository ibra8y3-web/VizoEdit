package com.vizoedit.studio

import android.net.Uri
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.media.MediaMetadataRetriever
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.audio.DefaultGainProvider
import androidx.media3.common.audio.GainProcessor
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.Effects
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItemSequence
import java.io.File
import java.io.FileOutputStream
import com.vizoedit.studio.media.FrameExtractor
import com.vizoedit.studio.media.MediaLibrary
import com.vizoedit.studio.media.TrimRange
import com.vizoedit.studio.media.TrimHistory
import com.vizoedit.studio.projects.ProjectDocument
import com.vizoedit.studio.projects.ProjectStore
import com.vizoedit.studio.settings.EditorSettings
import com.vizoedit.studio.settings.OutputCanvas
import com.vizoedit.studio.settings.OverlayAnchor
import com.vizoedit.studio.render.Media3EffectFactory
import com.vizoedit.studio.render.Media3ExportFactory
import com.vizoedit.studio.settings.ColorLookPreset
import com.vizoedit.studio.settings.OutputCodec
import com.vizoedit.studio.settings.OverlayColor
import java.util.Locale
import kotlinx.coroutines.delay

private val Night = Color(0xFF080A14)
private val Panel = Color(0xFF121629)
private val Purple = Color(0xFFA855F7)
private val Cyan = Color(0xFF22D3EE)
private val TextMain = Color(0xFFF4F4FA)
private val TextMuted = Color(0xFFA8AEC4)

private data class ImportedVideo(
    val id: String,
    val uri: Uri,
    val durationMs: Long,
    val displayName: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VizoEditApp() }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VizoEditApp() {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var startMs by remember { mutableLongStateOf(0L) }
    var endMs by remember { mutableLongStateOf(0L) }
    var exportBusy by remember { mutableStateOf(false) }
    var exportStatus by remember { mutableStateOf("استورد فيديو للبدء") }
    var exportedPath by remember { mutableStateOf<String?>(null) }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var previewSpeed by remember { mutableFloatStateOf(1f) }
    var editorSettings by remember { mutableStateOf(EditorSettings()) }
    var playheadMs by remember { mutableLongStateOf(0L) }
    var projectName by remember { mutableStateOf("مشروعي") }
    var showProjects by remember { mutableStateOf(false) }
    var availableProjects by remember { mutableStateOf(emptyList<File>()) }
    var galleryUri by remember { mutableStateOf<Uri?>(null) }
    val importedClips = remember { mutableStateListOf<ImportedVideo>() }
    var backgroundAudioUri by remember { mutableStateOf<Uri?>(null) }
    var backgroundAudioVolume by remember { mutableFloatStateOf(0.8f) }
    val trimHistory = remember { TrimHistory() }
    var historyRevision by remember { mutableIntStateOf(0) }

    fun applyTrim(range: TrimRange) {
        if (durationMs <= 0L) return
        val safeRange = runCatching { range.validate(durationMs) }.getOrElse { return }
        trimHistory.record(TrimRange(startMs, endMs))
        trimHistory.record(safeRange)
        startMs = safeRange.startMs
        endMs = safeRange.endMs
        historyRevision++
        exportedPath = null
        galleryUri = null
    }

    DisposableEffect(selectedUri) {
        val uri = selectedUri
        val createdPlayer = if (uri != null) ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_OFF
        } else null
        player = createdPlayer
        onDispose {
            createdPlayer?.release()
            if (player === createdPlayer) player = null
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            importedClips.clear()
            selectedUri = uri
            exportedPath = null
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                retriever.release()
                startMs = 0L
                endMs = durationMs
                playheadMs = 0L
                if (durationMs > 500L) trimHistory.reset(TrimRange(0L, durationMs))
                historyRevision++
                galleryUri = null
                exportStatus = if (durationMs > 0) "الفيديو جاهز للتحرير" else "تعذر قراءة مدة الفيديو؛ جرّب ملفًا آخر"
            } catch (e: Exception) {
                durationMs = 0L; startMs = 0L; endMs = 0L
                exportStatus = "تعذر قراءة معلومات الفيديو: ${e.localizedMessage ?: "خطأ غير معروف"}"
            }
        }
    }

    val multiClipPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            val accepted = mutableListOf<ImportedVideo>()
            var rejected = 0
            uris.forEach { uri ->
                try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
                val duration = readVideoDurationMs(context, uri)
                if (duration != null && duration > 500L) {
                    val label = runCatching {
                        context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) cursor.getString(0) else null
                        }
                    }.getOrNull().orEmpty().ifBlank { "مقطع ${accepted.size + 1}" }
                    accepted += ImportedVideo(java.util.UUID.randomUUID().toString(), uri, duration, label)
                } else rejected++
            }
            val queueWasEmpty = importedClips.isEmpty()
            importedClips.addAll(accepted)
            if (queueWasEmpty && accepted.isNotEmpty()) {
                val first = accepted.first()
                selectedUri = first.uri
                durationMs = first.durationMs
                startMs = 0L
                endMs = first.durationMs
                playheadMs = 0L
                trimHistory.reset(TrimRange(0L, first.durationMs))
                historyRevision++
            }
            exportedPath = null
            galleryUri = null
            exportStatus = buildString {
                append("تمت إضافة ${accepted.size} مقطعًا إلى قائمة الدمج")
                if (rejected > 0) append("؛ تم تجاهل $rejected ملف/ملفات تعذر قراءتها كفيديو")
            }
        }
    }

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                backgroundAudioUri = uri
                backgroundAudioVolume = 0.8f
                exportStatus = "تم اختيار الموسيقى؛ ستُضاف كمسار صوت متزامن أثناء التصدير"
                exportedPath = null
                galleryUri = null
            } catch (e: Exception) {
                exportStatus = "تعذر فتح ملف الصوت: ${e.localizedMessage ?: "خطأ غير معروف"}"
            }
        }
    }

    LaunchedEffect(player, previewSpeed) {
        player?.setPlaybackSpeed(previewSpeed)
    }

    LaunchedEffect(player, editorSettings.sourceVolume, editorSettings.muteOriginalAudio) {
        player?.volume = if (editorSettings.muteOriginalAudio) 0f else editorSettings.sourceVolume
    }

    LaunchedEffect(player) {
        val activePlayer = player ?: return@LaunchedEffect
        while (true) {
            playheadMs = activePlayer.currentPosition.coerceAtLeast(0L)
            delay(150L)
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Purple, secondary = Cyan, background = Night, surface = Panel,
        onPrimary = Color.White, onBackground = TextMain, onSurface = TextMain
    )) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF11142A), Night, Color(0xFF080A14))))) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("VizoEdit", fontSize = 29.sp, fontWeight = FontWeight.Black, color = TextMain)
                        Text("CREATE  •  EDIT  •  INSPIRE", fontSize = 10.sp, letterSpacing = 2.sp, color = Cyan)
                    }
                    Surface(color = Color(0xFF25163B), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Rounded.Movie, null, tint = Purple, modifier = Modifier.padding(13.dp).size(28.dp))
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("مشروعك الإبداعي", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("ابدأ بملف فيديو حقيقي من جهازك.", color = TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Surface(color = Panel, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Box(Modifier.fillMaxWidth().height(220.dp).background(Color(0xFF05060B), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            if (selectedUri != null && player != null) {
                                AndroidView(
                                    factory = { ctx -> PlayerView(ctx).apply { useController = true; this.player = player } },
                                    update = { it.player = player },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.Movie, null, tint = Purple, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("معاينة الفيديو", color = TextMuted)
                                }
                            }
                            if (editorSettings.overlayText.isNotBlank()) {
                                val overlayAlignment = when (editorSettings.overlayAnchor) {
                                    OverlayAnchor.TOP -> Alignment.TopCenter
                                    OverlayAnchor.CENTER -> Alignment.Center
                                    OverlayAnchor.BOTTOM -> Alignment.BottomCenter
                                }
                                Text(
                                    text = editorSettings.overlayText,
                                    color = Color(OverlayColor.fromId(editorSettings.overlayColorId).argb).copy(alpha = editorSettings.overlayOpacity),
                                    fontWeight = FontWeight.Black,
                                    fontSize = (18f * editorSettings.overlayScale).sp,
                                    modifier = Modifier.align(overlayAlignment).padding(14.dp).graphicsLayer { rotationZ = editorSettings.overlayRotationDegrees }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { picker.launch(arrayOf("video/*")) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(8.dp)); Text(if (selectedUri == null) "استيراد فيديو" else "اختيار فيديو آخر", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { multiClipPicker.launch(arrayOf("video/*")) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.PlaylistAdd, null); Spacer(Modifier.width(8.dp))
                            Text("إضافة عدة مقاطع للدمج (${importedClips.size})")
                        }
                        if (importedClips.isNotEmpty()) {
                            Surface(color = Color(0xFF0D1020), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Text("ترتيب المقاطع في الفيديو النهائي", fontWeight = FontWeight.SemiBold, color = Cyan)
                                    importedClips.forEachIndexed { index, clip ->
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Column(Modifier.weight(1f)) {
                                                Text("${index + 1}. ${clip.displayName}", maxLines = 1, color = TextMain, fontSize = 12.sp)
                                                Text("${formatTime(clip.durationMs)}${if (clip.uri == selectedUri) " • القص الحالي يطبق على هذا المصدر" else ""}", color = TextMuted, fontSize = 10.sp)
                                            }
                                            IconButton(onClick = {
                                                if (index > 0) {
                                                    val item = importedClips.removeAt(index)
                                                    importedClips.add(index - 1, item)
                                                    exportedPath = null
                                                }
                                            }, enabled = index > 0) { Icon(Icons.Rounded.KeyboardArrowUp, "تحريك لأعلى", tint = Cyan) }
                                            IconButton(onClick = {
                                                if (index < importedClips.lastIndex) {
                                                    val item = importedClips.removeAt(index)
                                                    importedClips.add(index + 1, item)
                                                    exportedPath = null
                                                }
                                            }, enabled = index < importedClips.lastIndex) { Icon(Icons.Rounded.KeyboardArrowDown, "تحريك لأسفل", tint = Cyan) }
                                            IconButton(onClick = {
                                                importedClips.removeAt(index)
                                                exportedPath = null
                                                if (importedClips.isEmpty()) exportStatus = "تمت إزالة قائمة الدمج؛ سيُصدّر المقطع المحدد وحده"
                                            }) { Icon(Icons.Rounded.Close, "إزالة المقطع", tint = Color(0xFFFF7895)) }
                                        }
                                    }
                                    Text("يتم دمج المقاطع بالترتيب الظاهر أعلاه. لا تُدعم انتقالات التداخل بين المقاطع في محرك الدمج الحالي.", color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { audioPicker.launch(arrayOf("audio/*")) },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.MusicNote, null); Spacer(Modifier.width(5.dp))
                                Text(if (backgroundAudioUri == null) "إضافة موسيقى" else "تغيير الموسيقى")
                            }
                            if (backgroundAudioUri != null) {
                                OutlinedButton(onClick = {
                                    backgroundAudioUri = null
                                    exportedPath = null
                                    exportStatus = "تمت إزالة مسار الموسيقى الإضافي"
                                }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                    Icon(Icons.Rounded.Close, null); Text("إزالة الموسيقى")
                                }
                            }
                        }
                        if (backgroundAudioUri != null) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("مستوى الموسيقى", color = TextMuted, fontSize = 12.sp)
                                Spacer(Modifier.weight(1f))
                                Text("${(backgroundAudioVolume * 100).toInt()}%", color = Cyan, fontSize = 12.sp)
                            }
                            Slider(
                                value = backgroundAudioVolume,
                                onValueChange = { backgroundAudioVolume = it; exportedPath = null; galleryUri = null },
                                valueRange = 0f..1f
                            )
                            Text("سيُكرّر مسار الموسيقى المختار حتى نهاية تسلسل الفيديو. مستوى الصوت منفصل عن صوت الفيديو الأصلي.", color = TextMuted, fontSize = 10.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = projectName,
                            onValueChange = { projectName = it.take(80) },
                            label = { Text("اسم المشروع") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = { availableProjects = ProjectStore(context).list(); showProjects = true },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                        ) { Text("فتح مشروع محفوظ (${ProjectStore(context).list().size})") }
                        if (selectedUri != null) {
                            Spacer(Modifier.height(8.dp))
                            Text("المدة: ${formatTime(durationMs)}", color = TextMuted, fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Speed, null, tint = Cyan)
                                Spacer(Modifier.width(8.dp))
                                Text("سرعة المعاينة", color = TextMuted, fontSize = 13.sp)
                                Spacer(Modifier.weight(1f))
                                Text("${String.format(Locale.US, "%.2f", previewSpeed)}×", color = Cyan, fontWeight = FontWeight.Bold)
                            }
                            Slider(value = previewSpeed, onValueChange = { previewSpeed = it }, valueRange = 0.25f..2f)
                            Text("موضع التشغيل: ${formatTime(playheadMs)}", color = Cyan, fontSize = 12.sp)
                            Slider(
                                value = playheadMs.toFloat().coerceIn(0f, durationMs.coerceAtLeast(1L).toFloat()),
                                onValueChange = { value ->
                                    val target = value.toLong().coerceIn(0L, durationMs)
                                    playheadMs = target
                                    player?.seekTo(target)
                                },
                                valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
                                enabled = durationMs > 0L
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { player?.pause(); player?.seekTo(startMs); playheadMs = startMs }, modifier = Modifier.weight(1f)) { Text("إلى البداية") }
                                OutlinedButton(onClick = { player?.pause(); player?.seekTo((playheadMs - 33L).coerceAtLeast(0L)); playheadMs = (playheadMs - 33L).coerceAtLeast(0L) }, modifier = Modifier.weight(1f)) { Text("إطار −") }
                                OutlinedButton(onClick = { player?.pause(); player?.seekTo((playheadMs + 33L).coerceAtMost(durationMs)); playheadMs = (playheadMs + 33L).coerceAtMost(durationMs) }, modifier = Modifier.weight(1f)) { Text("إطار +") }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { applyTrim(TrimRange(playheadMs.coerceAtMost((endMs - 500L).coerceAtLeast(0L)), endMs)) }, modifier = Modifier.weight(1f), enabled = playheadMs < endMs - 500L) { Text("تعيين البداية") }
                                Button(onClick = { applyTrim(TrimRange(startMs, playheadMs.coerceAtLeast(startMs + 500L).coerceAtMost(durationMs))) }, modifier = Modifier.weight(1f), enabled = playheadMs > startMs + 500L) { Text("تعيين النهاية") }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { val restored = trimHistory.undo(); if (restored != null) { startMs = restored.startMs; endMs = restored.endMs; historyRevision++; exportedPath = null } }, enabled = run { historyRevision; trimHistory.canUndo }, modifier = Modifier.weight(1f)) { Text("تراجع القص") }
                                OutlinedButton(onClick = { val restored = trimHistory.redo(); if (restored != null) { startMs = restored.startMs; endMs = restored.endMs; historyRevision++; exportedPath = null } }, enabled = run { historyRevision; trimHistory.canRedo }, modifier = Modifier.weight(1f)) { Text("إعادة القص") }
                                OutlinedButton(onClick = { applyTrim(TrimRange(0L, durationMs)) }, modifier = Modifier.weight(1f), enabled = durationMs > 500L) { Text("إلغاء القص") }
                            }
                            OutlinedButton(
                                onClick = {
                                    val uri = selectedUri ?: return@OutlinedButton
                                    try {
                                        val directory = File(context.filesDir, "frames").apply { if (!exists() && !mkdirs()) throw java.io.IOException("تعذر إنشاء مجلد الصور") }
                                        val output = File(directory, "frame_${System.currentTimeMillis()}.png")
                                        FileOutputStream(output).use { stream -> FrameExtractor.writePng(context.contentResolver, uri, (player?.currentPosition ?: startMs).coerceIn(0L, durationMs), stream) }
                                        exportStatus = "تم استخراج إطار PNG: ${output.name}"
                                        Toast.makeText(context, "تم حفظ الإطار داخل مجلد التطبيق", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        exportStatus = "تعذر استخراج الإطار: ${e.localizedMessage ?: "خطأ غير معروف"}"
                                        Toast.makeText(context, exportStatus, Toast.LENGTH_LONG).show()
                                    }
                                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Image, null); Spacer(Modifier.width(8.dp)); Text("استخراج الإطار الحالي PNG")
                            }
                            OutlinedButton(
                                onClick = {
                                    val uri = selectedUri ?: return@OutlinedButton
                                    try {
                                        val project = ProjectDocument(
                                            projectName.trim().ifBlank { "مشروعي" }, uri.toString(), durationMs,
                                            startMs, endMs, previewSpeed, editorSettings = editorSettings.validate()
                                        )
                                        val file = ProjectStore(context).save(project)
                                        exportStatus = "تم حفظ ملف المشروع: ${file.name}"
                                        Toast.makeText(context, "تم حفظ بيانات المشروع", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        exportStatus = "تعذر حفظ المشروع: ${e.localizedMessage ?: "خطأ غير معروف"}"
                                    }
                                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                            ) { Text("حفظ بيانات المشروع") }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ContentCut, null, tint = Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("قص المقطع", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Surface(color = Panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("البداية", color = TextMuted, fontSize = 13.sp)
                            Text(formatTime(startMs), color = Cyan, fontWeight = FontWeight.SemiBold)
                        }
                        Slider(value = if (durationMs > 0) startMs.toFloat().coerceIn(0f, durationMs.toFloat()) else 0f,
                            onValueChange = { v -> applyTrim(TrimRange(v.toLong().coerceAtMost((endMs - 500L).coerceAtLeast(0L)), endMs)) },
                            valueRange = 0f..(durationMs.coerceAtLeast(1L).toFloat()), enabled = selectedUri != null && durationMs > 500L)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("النهاية", color = TextMuted, fontSize = 13.sp)
                            Text(formatTime(endMs), color = Cyan, fontWeight = FontWeight.SemiBold)
                        }
                        Slider(value = if (durationMs > 0) endMs.toFloat().coerceIn(0f, durationMs.toFloat()) else 0f,
                            onValueChange = { v -> applyTrim(TrimRange(startMs, v.toLong().coerceAtLeast(startMs + 500L).coerceAtMost(durationMs))) },
                            valueRange = 0f..(durationMs.coerceAtLeast(1L).toFloat()), enabled = selectedUri != null && durationMs > 500L)
                        Text("يتم تطبيق القص فعليًا أثناء التصدير، ولا يُعدّل الملف الأصلي.", color = TextMuted, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                EditorSettingsPanel(
                    settings = editorSettings,
                    onSettingsChange = { next ->
                        runCatching { next.validate() }.onSuccess {
                            editorSettings = it
                            exportedPath = null
                            galleryUri = null
                        }
                    }
                )
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Tune, null, tint = Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("التصدير", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Surface(color = Panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(exportStatus, color = if (exportedPath != null) Cyan else TextMuted, fontSize = 13.sp)
                        if (exportBusy) { Spacer(Modifier.height(12.dp)); LinearProgressIndicator(Modifier.fillMaxWidth(), color = Purple) }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            enabled = selectedUri != null && !exportBusy && durationMs > 500L && endMs > startMs,
                            onClick = {
                                val uri = selectedUri ?: return@Button
                                val outDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: File(context.filesDir, "Movies")
                                if (!outDir.exists() && !outDir.mkdirs()) {
                                    exportStatus = "تعذر إنشاء مجلد التصدير داخل التطبيق."
                                    return@Button
                                }
                                player?.pause()
                                val output = File(outDir, "VizoEdit_${System.currentTimeMillis()}.mp4")
                                exportBusy = true; exportedPath = null; exportStatus = "جارٍ معالجة الفيديو…"
                                try {
                                    val activeSettings = editorSettings.validate()
                                    val queueSnapshot = importedClips.toList().ifEmpty {
                                        listOf(ImportedVideo("single-source", uri, durationMs, "المقطع المحدد"))
                                    }
                                    require(queueSnapshot.isNotEmpty()) { "أضف مقطع فيديو واحدًا على الأقل" }
                                    var appliedCurrentTrim = false
                                    val editedItems = queueSnapshot.map { clip ->
                                        val useCurrentTrim = !appliedCurrentTrim && clip.uri == uri
                                        if (useCurrentTrim) appliedCurrentTrim = true
                                        val clipStart = if (useCurrentTrim) startMs.coerceIn(0L, (clip.durationMs - 500L).coerceAtLeast(0L)) else 0L
                                        val clipEnd = if (useCurrentTrim) endMs.coerceIn((clipStart + 500L).coerceAtMost(clip.durationMs), clip.durationMs) else clip.durationMs
                                        val clipping = MediaItem.ClippingConfiguration.Builder()
                                            .setStartPositionMs(clipStart)
                                            .setEndPositionMs(clipEnd)
                                            .build()
                                        val itemDuration = (clipEnd - clipStart).coerceAtLeast(1L)
                                        EditedMediaItem.Builder(
                                            MediaItem.Builder().setUri(clip.uri).setClippingConfiguration(clipping).build()
                                        )
                                            .setFrameRate(activeSettings.outputFrameRate)
                                            .setRemoveAudio(activeSettings.muteOriginalAudio)
                                            .setEffects(Effects(
                                                Media3EffectFactory.createAudioProcessors(activeSettings, itemDuration),
                                                Media3EffectFactory.createVideoEffects(activeSettings)
                                            ))
                                            .build()
                                    }
                                    val videoSequence = EditedMediaItemSequence.withAudioAndVideoFrom(editedItems)
                                    val composition = if (backgroundAudioUri != null) {
                                        val musicGain = GainProcessor(DefaultGainProvider.Builder(backgroundAudioVolume).build())
                                        val backgroundTrack = EditedMediaItem.Builder(MediaItem.fromUri(backgroundAudioUri!!))
                                            .setEffects(Effects(listOf(musicGain), emptyList()))
                                            .build()
                                        val loopingAudioSequence = EditedMediaItemSequence.withAudioFrom(listOf(backgroundTrack))
                                            .buildUpon().setIsLooping(true).build()
                                        Composition.Builder(videoSequence, loopingAudioSequence).build()
                                    } else {
                                        Composition.Builder(videoSequence).build()
                                    }
                                    val transformer = Media3ExportFactory.createBuilder(context, activeSettings)
                                        .addListener(object : Transformer.Listener {
                                            override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: ExportResult) {
                                                exportBusy = false
                                                exportedPath = output.absolutePath
                                                galleryUri = null
                                                exportStatus = "اكتمل التصدير بنجاح: ${output.name}"
                                                Toast.makeText(context, "تم تصدير الفيديو", Toast.LENGTH_LONG).show()
                                            }
                                            override fun onError(composition: androidx.media3.transformer.Composition, exportResult: ExportResult, exportException: ExportException) {
                                                exportBusy = false
                                                exportStatus = "فشل التصدير: ${exportException.localizedMessage ?: "خطأ في المعالجة"}"
                                                if (output.exists()) output.delete()
                                            }
                                        }).build()
                                    transformer.start(composition, output.absolutePath)
                                } catch (e: Exception) {
                                    exportBusy = false
                                    exportStatus = "تعذر بدء التصدير: ${e.localizedMessage ?: "خطأ غير معروف"}"
                                    if (output.exists()) output.delete()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.FileDownload, null); Spacer(Modifier.width(8.dp)); Text(if (importedClips.size > 1) "دمج ${importedClips.size} مقاطع وتصدير MP4" else "تصدير MP4", fontWeight = FontWeight.Bold)
                        }
                        if (exportedPath != null) {
                            Spacer(Modifier.height(8.dp))
                            Text("مكان الملف: $exportedPath", color = TextMuted, fontSize = 11.sp)
                            Text("النسخة الأصلية محفوظة داخل مساحة التطبيق الخاصة.", color = TextMuted, fontSize = 11.sp)
                            Button(
                                onClick = {
                                    try {
                                        val target = File(exportedPath!!)
                                        galleryUri = MediaLibrary.saveVideoToGallery(context, target)
                                        exportStatus = "تم حفظ الفيديو في Movies/VizoEdit داخل المعرض"
                                        Toast.makeText(context, exportStatus, Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        exportStatus = "تعذر حفظ الفيديو في المعرض: ${e.localizedMessage ?: "خطأ غير معروف"}"
                                        Toast.makeText(context, exportStatus, Toast.LENGTH_LONG).show()
                                    }
                                }, modifier = Modifier.fillMaxWidth(), enabled = galleryUri == null, colors = ButtonDefaults.buttonColors(containerColor = Purple)
                            ) { Text(if (galleryUri == null) "حفظ نسخة في المعرض" else "تم الحفظ في المعرض") }
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val uri = galleryUri ?: FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            File(exportedPath!!)
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            clipData = android.content.ClipData.newUri(context.contentResolver, "VizoEdit video", uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "مشاركة فيديو VizoEdit"))
                                    } catch (e: Exception) {
                                        exportStatus = "تعذرت مشاركة الفيديو: ${e.localizedMessage ?: "لا يوجد تطبيق مناسب"}"
                                    }
                                }, modifier = Modifier.fillMaxWidth()
                            ) { Text("مشاركة الفيديو المحفوظ") }
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("VizoEdit • إصدار تأسيسي مفتوح المصدر", color = TextMuted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(12.dp))
            }
        }
        if (showProjects) {
            AlertDialog(
                onDismissRequest = { showProjects = false },
                title = { Text("المشاريع المحفوظة") },
                text = {
                    if (availableProjects.isEmpty()) Text("لا توجد مشاريع محفوظة بعد.")
                    else Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                        availableProjects.forEach { file ->
                            TextButton(
                                onClick = {
                                    try {
                                        val document = ProjectStore(context).load(file)
                                        val restoredUri = Uri.parse(document.sourceUri)
                                        val retriever = MediaMetadataRetriever()
                                        try {
                                            retriever.setDataSource(context, restoredUri)
                                            val actualDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                                                ?: throw IllegalStateException("تعذر قراءة مدة مصدر الفيديو")
                                            selectedUri = restoredUri
                                            durationMs = actualDuration
                                            val restoredStart = document.trimStartMs.coerceIn(0L, (actualDuration - 500L).coerceAtLeast(0L))
                                            val restoredEnd = document.trimEndMs.coerceIn((restoredStart + 500L).coerceAtMost(actualDuration), actualDuration)
                                            startMs = restoredStart
                                            endMs = restoredEnd
                                            previewSpeed = document.previewSpeed.coerceIn(0.25f, 2f)
                                            editorSettings = document.editorSettings
                                            playheadMs = startMs
                                            projectName = document.name
                                            trimHistory.reset(TrimRange(startMs, endMs))
                                            historyRevision++
                                            exportedPath = null
                                            galleryUri = null
                                            exportStatus = "تم فتح المشروع ${document.name}"
                                        } finally { retriever.release() }
                                    } catch (e: Exception) {
                                        exportStatus = "تعذر فتح المشروع؛ قد يكون مصدر الفيديو غير متاح: ${e.localizedMessage ?: "خطأ"}"
                                    }
                                    showProjects = false
                                }, modifier = Modifier.fillMaxWidth()
                            ) { Text(file.nameWithoutExtension, maxLines = 2) }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showProjects = false }) { Text("إغلاق") } }
            )
        }
    }
}


private fun readVideoDurationMs(context: android.content.Context, uri: Uri): Long? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
    } catch (_: Exception) {
        null
    } finally {
        runCatching { retriever.release() }
    }
}

@Composable
private fun EditorSettingsPanel(
    settings: EditorSettings,
    onSettingsChange: (EditorSettings) -> Unit
) {
    Surface(color = Panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("التحويلات والمؤثرات التي تُطبّق على ملف التصدير", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("التغييرات التالية تُعالَج داخل محرك Media3 عند التصدير؛ تغييرها لا يعدّل ملف المصدر.", color = TextMuted, fontSize = 11.sp)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("الدوران", color = TextMuted, fontSize = 13.sp)
                Text("${settings.rotationDegrees.toInt()}°", color = Cyan, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = settings.rotationDegrees,
                onValueChange = { onSettingsChange(settings.copy(rotationDegrees = it).validatedOrCurrent(settings)) },
                valueRange = -180f..180f
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("التكبير", color = TextMuted, fontSize = 13.sp)
                Text("${String.format(Locale.US, "%.2f", settings.zoom)}×", color = Cyan, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = settings.zoom,
                onValueChange = { onSettingsChange(settings.copy(zoom = it).validatedOrCurrent(settings)) },
                valueRange = 1f..2f
            )

            Text("إعدادات ألوان جاهزة", fontWeight = FontWeight.SemiBold)
            Text("هذه الوصفات تُطبّق فعليًا أثناء تصدير الفيديو.", color = TextMuted, fontSize = 10.sp)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ColorLookPreset.entries.forEach { look ->
                    FilterChip(
                        selected = settings.lookPresetId == look.id,
                        onClick = { onSettingsChange(settings.copy(lookPresetId = look.id)) },
                        label = { Text(look.title) }
                    )
                }
            }
            Text("توازن قنوات RGB", fontWeight = FontWeight.SemiBold)
            RgbSlider("الأحمر", settings.redScale) { onSettingsChange(settings.copy(redScale = it).validatedOrCurrent(settings)) }
            RgbSlider("الأخضر", settings.greenScale) { onSettingsChange(settings.copy(greenScale = it).validatedOrCurrent(settings)) }
            RgbSlider("الأزرق", settings.blueScale) { onSettingsChange(settings.copy(blueScale = it).validatedOrCurrent(settings)) }
            SignedSlider("السطوع", settings.brightness, -1f..1f) { onSettingsChange(settings.copy(brightness = it)) }
            SignedSlider("التباين", settings.contrast, -1f..1f) { onSettingsChange(settings.copy(contrast = it)) }
            SignedSlider("Hue / درجة اللون", settings.hueDegrees, -180f..180f) { onSettingsChange(settings.copy(hueDegrees = it)) }
            SignedSlider("التشبع", settings.saturation, -100f..100f) { onSettingsChange(settings.copy(saturation = it)) }
            SignedSlider("الإضاءة", settings.lightness, -100f..100f) { onSettingsChange(settings.copy(lightness = it)) }

            Text("قص الإطار من الحواف", fontWeight = FontWeight.SemiBold)
            Text("القيم نسبة من كل حافة؛ اجعلها صفرًا للاحتفاظ بالإطار كاملًا.", color = TextMuted, fontSize = 10.sp)
            CropSlider("اليسار", settings.cropLeft) { onSettingsChange(settings.copy(cropLeft = it.coerceAtMost((0.899f - settings.cropRight).coerceAtLeast(0f)))) }
            CropSlider("الأعلى", settings.cropTop) { onSettingsChange(settings.copy(cropTop = it.coerceAtMost((0.899f - settings.cropBottom).coerceAtLeast(0f)))) }
            CropSlider("اليمين", settings.cropRight) { onSettingsChange(settings.copy(cropRight = it.coerceAtMost((0.899f - settings.cropLeft).coerceAtLeast(0f)))) }
            CropSlider("الأسفل", settings.cropBottom) { onSettingsChange(settings.copy(cropBottom = it.coerceAtMost((0.899f - settings.cropTop).coerceAtLeast(0f)))) }
            OutlinedButton(
                onClick = { onSettingsChange(settings.copy(cropLeft = 0f, cropTop = 0f, cropRight = 0f, cropBottom = 0f)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("إعادة ضبط القص") }

            Text("أبعاد الإخراج", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutputCanvas.entries.forEach { canvas ->
                    FilterChip(
                        selected = settings.outputCanvasId == canvas.id,
                        onClick = { onSettingsChange(settings.copy(outputCanvasId = canvas.id)) },
                        label = { Text(canvas.title) }
                    )
                }
            }

            Text("نص ثابت مدمج داخل الفيديو", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = settings.overlayText,
                onValueChange = { onSettingsChange(settings.copy(overlayText = it.take(EditorSettings.MAX_OVERLAY_TEXT_LENGTH))) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("اكتب النص الذي سيظهر في الفيديو") },
                placeholder = { Text("مثال: عنوان الفيديو") },
                singleLine = false,
                maxLines = 3
            )
            Text("ملصقات سريعة (تُدمج مع النص في الفيديو)", fontSize = 11.sp, color = TextMuted)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("⭐", "❤️", "🔥", "😂", "✨", "🎬", "🎵", "⚡", "🌈", "💯").forEach { emoji ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            val spacer = if (settings.overlayText.isBlank()) "" else " "
                            onSettingsChange(settings.copy(overlayText = (settings.overlayText + spacer + emoji).take(EditorSettings.MAX_OVERLAY_TEXT_LENGTH)))
                        },
                        label = { Text(emoji, fontSize = 17.sp) }
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OverlayAnchor.entries.forEach { anchor ->
                    FilterChip(
                        selected = settings.overlayAnchor == anchor,
                        onClick = { onSettingsChange(settings.copy(overlayAnchor = anchor)) },
                        label = { Text(anchor.title) }
                    )
                }
            }
            Text("لون النص", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OverlayColor.entries.forEach { overlayColor ->
                    FilterChip(
                        selected = settings.overlayColorId == overlayColor.id,
                        onClick = { onSettingsChange(settings.copy(overlayColorId = overlayColor.id)) },
                        label = { Text(overlayColor.title, color = Color(overlayColor.argb)) }
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("شفافية النص", color = TextMuted, fontSize = 13.sp)
                Text("${(settings.overlayOpacity * 100).toInt()}%", color = Cyan)
            }
            Slider(
                value = settings.overlayOpacity,
                onValueChange = { onSettingsChange(settings.copy(overlayOpacity = it)) },
                valueRange = 0f..1f
            )
            SignedSlider("دوران النص", settings.overlayRotationDegrees, -180f..180f) { onSettingsChange(settings.copy(overlayRotationDegrees = it)) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("حجم النص", color = TextMuted, fontSize = 13.sp)
                Text("${String.format(Locale.US, "%.1f", settings.overlayScale)}×", color = Cyan)
            }
            Slider(
                value = settings.overlayScale,
                onValueChange = { onSettingsChange(settings.copy(overlayScale = it)) },
                valueRange = 0.4f..2f
            )

            Divider(color = Color(0xFF30354D))
            Text("الصوت الأصلي", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("كتم صوت الفيديو", color = TextMuted, fontSize = 13.sp)
                    Text("عند التفعيل يُحذف مسار الصوت الأصلي من التصدير", color = TextMuted, fontSize = 10.sp)
                }
                Switch(
                    checked = settings.muteOriginalAudio,
                    onCheckedChange = { onSettingsChange(settings.copy(muteOriginalAudio = it)) }
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("مستوى الصوت", color = TextMuted, fontSize = 13.sp)
                Text("${(settings.sourceVolume * 100).toInt()}%", color = Cyan)
            }
            Slider(
                value = settings.sourceVolume,
                onValueChange = { onSettingsChange(settings.copy(sourceVolume = it)) },
                valueRange = 0f..1f,
                enabled = !settings.muteOriginalAudio
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("تلاشي دخول الصوت", color = TextMuted, fontSize = 13.sp)
                Text("${settings.audioFadeInMs} ms", color = Cyan)
            }
            Slider(
                value = settings.audioFadeInMs.toFloat(),
                onValueChange = { onSettingsChange(settings.copy(audioFadeInMs = it.toLong())) },
                valueRange = 0f..5_000f,
                enabled = !settings.muteOriginalAudio
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("تلاشي خروج الصوت", color = TextMuted, fontSize = 13.sp)
                Text("${settings.audioFadeOutMs} ms", color = Cyan)
            }
            Slider(
                value = settings.audioFadeOutMs.toFloat(),
                onValueChange = { onSettingsChange(settings.copy(audioFadeOutMs = it.toLong())) },
                valueRange = 0f..5_000f,
                enabled = !settings.muteOriginalAudio
            )

            Text("معدل الإطارات عند التصدير", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(24, 25, 30, 50, 60).forEach { fps ->
                    FilterChip(
                        selected = settings.outputFrameRate == fps,
                        onClick = { onSettingsChange(settings.copy(outputFrameRate = fps)) },
                        label = { Text("$fps FPS") }
                    )
                }
            }
            Text("ترميز الفيديو", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutputCodec.entries.forEach { codec ->
                    FilterChip(
                        selected = settings.outputCodecId == codec.id,
                        onClick = { onSettingsChange(settings.copy(outputCodecId = codec.id)) },
                        label = { Text(codec.title) }
                    )
                }
            }
            Text("اختيار H.265 يعتمد على دعم جهازك؛ قد يستخدم محرك التصدير ترميزًا متوافقًا عند عدم توافره.", color = TextMuted, fontSize = 10.sp)
            Text("معدل بيانات الفيديو", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(2_000_000, 4_000_000, 8_000_000, 12_000_000, 20_000_000).forEach { bitrate ->
                    FilterChip(
                        selected = settings.outputVideoBitrateBitsPerSecond == bitrate,
                        onClick = { onSettingsChange(settings.copy(outputVideoBitrateBitsPerSecond = bitrate)) },
                        label = { Text("${bitrate / 1_000_000} Mbps") }
                    )
                }
            }
            Text("معدل بيانات الصوت AAC", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(96_000, 128_000, 192_000, 256_000).forEach { bitrate ->
                    FilterChip(
                        selected = settings.outputAudioBitrateBitsPerSecond == bitrate,
                        onClick = { onSettingsChange(settings.copy(outputAudioBitrateBitsPerSecond = bitrate)) },
                        label = { Text("${bitrate / 1000} kbps") }
                    )
                }
            }
            OutlinedButton(
                onClick = { onSettingsChange(EditorSettings()) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("إعادة ضبط إعدادات التحرير") }
        }
    }
}

@Composable
private fun SignedSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(92.dp))
        Slider(value = value, onValueChange = onValueChange, valueRange = range, modifier = Modifier.weight(1f))
        Text(String.format(Locale.US, "%.0f", value), color = Cyan, fontSize = 11.sp, modifier = Modifier.width(34.dp))
    }
}

@Composable
private fun CropSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextMuted, fontSize = 12.sp, modifier = Modifier.width(52.dp))
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..0.45f, modifier = Modifier.weight(1f))
        Text("${(value * 100).toInt()}%", color = Cyan, fontSize = 11.sp, modifier = Modifier.width(38.dp))
    }
}

@Composable
private fun RgbSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextMuted, fontSize = 12.sp, modifier = Modifier.width(52.dp))
        Slider(value = value, onValueChange = onValueChange, valueRange = 0.5f..1.5f, modifier = Modifier.weight(1f))
        Text(String.format(Locale.US, "%.2f", value), color = Cyan, fontSize = 11.sp, modifier = Modifier.width(42.dp))
    }
}

private fun EditorSettings.validatedOrCurrent(previous: EditorSettings): EditorSettings =
    runCatching { validate() }.getOrDefault(previous)

private fun formatTime(ms: Long): String {
    val safe = ms.coerceAtLeast(0L)
    val totalSeconds = safe / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    val tenths = (safe % 1000L) / 100L
    return String.format(Locale.US, "%02d:%02d.%d", minutes, seconds, tenths)
}
