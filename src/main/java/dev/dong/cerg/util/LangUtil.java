package dev.dong.cerg.util;

import com.simibubi.create.foundation.utility.CreateLang;
import dev.dong.cerg.CErg;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.ChatFormatting.*;

public class LangUtil extends Lang {
    @Contract(value = " -> new", pure = true)
    public static @NotNull LangBuilder builder() {
        return new LangBuilder(CErg.ID);
    }

    public static @NotNull LangBuilder translate(String langKey, Object... args) {
        return builder().translate(langKey, args);
    }

    public static @NotNull LangBuilder translate(ChatFormatting format, String langKey, Object... args) {
        return translate(langKey, args).style(format);
    }

    public static @NotNull LangBuilder text(String text) {
        return builder().text(text);
    }

    public static @NotNull LangBuilder text(ChatFormatting format, String literalText) {
        return builder().text(format, literalText);
    }

    public static @NotNull LangBuilder number(double number) {
        return text(LangNumberFormat.format(number));
    }

    public static @NotNull LangBuilder number(int number) {
        return text(String.valueOf(number));
    }

    public static @NotNull LangBuilder number(ChatFormatting format, int number) {
        return text(format, String.valueOf(number));
    }

    public static @NotNull LangBuilder progress(float progress, int totalBars) {
        var filledBars = (int) (progress * totalBars);
        return text(GREEN, "|".repeat(filledBars)).text(GRAY, "|".repeat(totalBars - filledBars));
    }

    public static @NotNull LangBuilder enabled(boolean enabled) {
        return enabled ? translate(GREEN, "message.enabled") : translate(RED, "message.disabled");
    }

    public static @NotNull LangBuilder seconds() {
        return CreateLang.translate("generic.unit.seconds");
    }

    public static @NotNull LangBuilder item(@NotNull ItemStack stack) {
        return builder().add(stack.getHoverName().copy().setStyle(stack.getDisplayName().getStyle()))
                .text(GREEN, " x%d".formatted(stack.getCount()));
    }
}
