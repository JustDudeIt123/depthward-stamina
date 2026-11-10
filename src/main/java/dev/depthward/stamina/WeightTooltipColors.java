package dev.depthward.stamina;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

/** Weight increases are penalties; decreases are benefits. Numeric values and signs stay unchanged. */
public final class WeightTooltipColors {
    private static final String KEY = "attribute.depthward_stamina.weight";
    public static void apply(List<Component> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (containsWeight(lines.get(i))) lines.set(i, format(lines.get(i)));
        }
    }
    private static Component format(Component original) {
        MutableComponent copy;
        ChatFormatting color = null;
        if (original.getContents() instanceof TranslatableContents text) {
            Object[] args = text.getArgs().clone();
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Component component) args[i] = format(component);
            }
            copy = Component.translatableWithFallback(text.getKey(), text.getFallback(), args);
            if (containsWeight(original)) {
                String key = text.getKey();
                if (key.startsWith("attribute.modifier.plus.") || key.equals("attributeslib.modifier.plus")) color = ChatFormatting.RED;
                if (key.startsWith("attribute.modifier.take.") || key.equals("attributeslib.modifier.take")) color = ChatFormatting.BLUE;
            }
        } else copy = original.plainCopy();
        copy.setStyle(original.getStyle());
        if (color != null) copy.withStyle(color);
        for (Component sibling : original.getSiblings()) copy.append(format(sibling));
        return copy;
    }
    private static boolean containsWeight(Component component) {
        if (component.getContents() instanceof TranslatableContents text) {
            if (KEY.equals(text.getKey())) return true;
            for (Object arg : text.getArgs()) if (arg instanceof Component child && containsWeight(child)) return true;
        }
        for (Component child : component.getSiblings()) if (containsWeight(child)) return true;
        return false;
    }
    private WeightTooltipColors() {}
}
