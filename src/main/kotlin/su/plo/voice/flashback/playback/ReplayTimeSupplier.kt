package su.plo.voice.flashback.playback

import com.moulberry.flashback.Flashback
import su.plo.voice.api.client.time.TimeSupplier

data object ReplayTimeSupplier : TimeSupplier {
    override fun getCurrentTimeMillis(): Long = Flashback.getVisualMillis()
}
