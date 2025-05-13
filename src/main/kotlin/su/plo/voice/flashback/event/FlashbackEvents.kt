package su.plo.voice.flashback.event

import com.moulberry.flashback.record.Recorder
import net.fabricmc.fabric.api.event.EventFactory

data object FlashbackEvents {
    @JvmField
    val WRITE_INITIAL_SNAPSHOT =
        EventFactory.createArrayBacked(
            WriteInitialSnapshot::class.java,
        ) { callbacks ->
            WriteInitialSnapshot { recorder ->
                callbacks.forEach { callback -> callback.onWrite(recorder) }
            }
        }

    @JvmField
    val EXPORT_START =
        EventFactory.createArrayBacked(
            ExportStart::class.java,
        ) { callbacks ->
            ExportStart {
                callbacks.forEach { callback -> callback.onStart() }
            }
        }

    @JvmField
    val EXPORT_END =
        EventFactory.createArrayBacked(
            ExportEnd::class.java,
        ) { callbacks ->
            ExportEnd {
                callbacks.forEach { callback -> callback.onEnd() }
            }
        }

    fun interface WriteInitialSnapshot {
        fun onWrite(recorder: Recorder)
    }

    fun interface ExportStart {
        fun onStart()
    }

    fun interface ExportEnd {
        fun onEnd()
    }
}
