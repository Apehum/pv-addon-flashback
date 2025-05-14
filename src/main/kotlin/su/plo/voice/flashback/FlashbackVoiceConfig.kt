package su.plo.voice.flashback

import su.plo.config.Config
import su.plo.config.ConfigField
import su.plo.config.entry.BooleanConfigEntry
import su.plo.config.entry.ConfigEntry

@Config
data class FlashbackVoiceConfig(
    @ConfigField
    val exportVoiceChat: BooleanConfigEntry = BooleanConfigEntry(true),
)

operator fun <T> ConfigEntry<T>.invoke(): T = value()
