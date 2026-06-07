package com.marvelspectrum.features.local.domain.recommendation

import com.marvelspectrum.shared.data.model.Song

object LanguagePreferenceModel {
    private val hindiTransliterationTokens = setOf(
        "dil", "pyar", "pyaar", "ishq", "mohabbat", "yaar", "sanam", "sajan", "sajna",
        "tere", "teri", "tera", "mere", "meri", "mera", "tum", "hum", "main", "mein", "jiya",
        "zindagi", "saath", "raat", "subah", "jaan", "jane", "khwab", "sapna", "desi",
        "bollywood", "hindustani", "aankh", "aankhon", "dhadkan", "mehfil", "nasha"
    )

    fun predict(song: Song): LanguagePrediction = predictText(
        listOf(song.title, song.artist, song.album, song.albumArtist.orEmpty(), song.genre.orEmpty())
            .joinToString(" ")
    )

    fun predictText(text: String): LanguagePrediction {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return LanguagePrediction(TrackLanguage.UNKNOWN, 0.1f, LanguageSource.UNKNOWN)
        val hasDevanagari = trimmed.any { it in '\u0900'..'\u097F' }
        val hasLatin = trimmed.any { it in 'A'..'Z' || it in 'a'..'z' }
        if (hasDevanagari && hasLatin) return LanguagePrediction(TrackLanguage.MIXED, 0.9f, LanguageSource.SCRIPT)
        if (hasDevanagari) return LanguagePrediction(TrackLanguage.HINDI, 0.95f, LanguageSource.SCRIPT)

        val tokens = TrackFeatureFactory.tokenize(trimmed)
        if (tokens.contains("instrumental") || tokens.contains("karaoke")) {
            return LanguagePrediction(TrackLanguage.INSTRUMENTAL, 0.8f, LanguageSource.KEYWORD_HEURISTIC)
        }
        val hindiHits = tokens.count { it in hindiTransliterationTokens }
        if (hindiHits >= 2 || (hindiHits == 1 && tokens.any { it == "bollywood" || it == "hindi" })) {
            return LanguagePrediction(TrackLanguage.HINDI_TRANSLITERATED, 0.68f, LanguageSource.KEYWORD_HEURISTIC)
        }
        if (hasLatin) return LanguagePrediction(TrackLanguage.ENGLISH, 0.55f, LanguageSource.SCRIPT)
        return LanguagePrediction(TrackLanguage.UNKNOWN, 0.25f, LanguageSource.UNKNOWN)
    }

    fun areCompatible(
        previous: TrackLanguage?,
        candidate: TrackLanguage,
        settings: SmartRecommendationSettings
    ): Boolean {
        if (previous == null) return true
        if (previous.isHindiFamily() && candidate == TrackLanguage.ENGLISH && !settings.allowEnglishBetweenHindi) return false
        if (previous == TrackLanguage.ENGLISH && candidate.isHindiFamily() && !settings.allowHindiBetweenEnglish) return false
        return when (settings.languageMixMode) {
            LanguageMixMode.SAME_LANGUAGE -> previous.normalizedFamily() == candidate.normalizedFamily() || candidate == TrackLanguage.UNKNOWN
            else -> true
        }
    }
}

fun TrackLanguage.isHindiFamily(): Boolean = this == TrackLanguage.HINDI || this == TrackLanguage.HINDI_TRANSLITERATED
fun TrackLanguage.normalizedFamily(): TrackLanguage = when (this) {
    TrackLanguage.HINDI_TRANSLITERATED -> TrackLanguage.HINDI
    else -> this
}
