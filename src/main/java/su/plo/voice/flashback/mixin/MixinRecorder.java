package su.plo.voice.flashback.mixin;

import com.moulberry.flashback.record.Recorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.flashback.event.FlashbackEvents;

@Mixin(value = Recorder.class, remap = false)
public class MixinRecorder {
    @Inject(
            method = "endTick",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lcom/moulberry/flashback/record/Recorder;writeSnapshot(Z)V",
                    ordinal = 0
            )
    )
    public void endTick(boolean close, CallbackInfo ci) {
        FlashbackEvents.WRITE_INITIAL_SNAPSHOT.invoker().onWrite(
                (Recorder) ((Object) this)
        );
    }
}
