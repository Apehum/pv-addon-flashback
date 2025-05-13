package su.plo.voice.flashback.mixin;

import com.mojang.blaze3d.audio.Library;
import com.moulberry.flashback.Flashback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.flashback.FlashbackVoiceAddon;

@Mixin(Library.class)
public class MixinSoundLibrary {
    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        if (Flashback.isExporting() && Flashback.EXPORT_JOB.getSettings().recordAudio()) {
            FlashbackVoiceAddon.getInstance()
                    .getVoiceClient()
                    .getDeviceManager()
                    .getOutputDevice()
                    .ifPresent(device -> {
                        try {
                            device.reload();
                        } catch (DeviceException e) {
                            FlashbackVoiceAddon.LOGGER.error("Failed to reload output device", e);
                        }
                    });
        }
    }
}
