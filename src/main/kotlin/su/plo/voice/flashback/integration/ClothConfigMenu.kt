package su.plo.voice.flashback.integration

import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import su.plo.config.entry.BooleanConfigEntry
import su.plo.voice.flashback.FlashbackVoiceAddon

private fun ConfigCategory.booleanEntry(
    entryBuilder: ConfigEntryBuilder,
    configEntry: BooleanConfigEntry,
    text: Component,
    onSave: (Boolean) -> Unit = {},
    builder: BooleanToggleBuilder.() -> Unit = {},
) {
    addEntry(
        entryBuilder
            .startBooleanToggle(text, configEntry.value())
            .setDefaultValue(configEntry.default)
            .setSaveConsumer {
                configEntry.set(it)
                onSave(it)
            }.apply(builder)
            .build(),
    )
}

private fun configBuilder(builder: ConfigBuilder.() -> Unit) =
    ConfigBuilder
        .create()
        .apply(builder)

private fun ConfigBuilder.category(
    text: Component,
    builder: ConfigCategory.() -> Unit,
) = getOrCreateCategory(text)
    .apply(builder)

fun createClothConfigMenu(parent: Screen): Screen =
    configBuilder {
        val config = FlashbackVoiceAddon.config

        parentScreen = parent
        title = Component.literal("pv-addon-flashback")
        savingRunnable = Runnable { FlashbackVoiceAddon.instance.saveConfigAsync() }

        category(Component.translatable("clothconfig.pvaddonflashback.general")) {
            booleanEntry(
                entryBuilder(),
                config.exportVoiceChat,
                Component.translatable("clothconfig.pvaddonflashback.export"),
            ) {
                setTooltip(Component.translatable("clothconfig.pvaddonflashback.export_tooltip"))
            }
        }
    }.build()
