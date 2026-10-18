package com.vizoedit.studio.settings

/** Output codecs supported by Media3 Transformer. Hardware support varies by device. */
enum class OutputCodec(val id: String, val title: String, val mimeType: String) {
    H264("h264", "H.264", "video/avc"),
    H265("h265", "H.265 / HEVC", "video/hevc");

    companion object {
        fun fromId(id: String): OutputCodec = entries.firstOrNull { it.id == id } ?: H264
    }
}
