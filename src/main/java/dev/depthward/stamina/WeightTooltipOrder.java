package dev.depthward.stamina;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/** Reorder the original components, preserving formatting and equipment-slot boundaries. */
public final class WeightTooltipOrder {
    private static final String WEIGHT_KEY = "attribute.depthward_stamina.weight";

    public static void sort(List<Component> lines) {
        int start = 0;
        while (start < lines.size()) {
            if (!isModifier(lines.get(start))) { start++; continue; }
            int end = start + 1;
            while (end < lines.size() && isModifier(lines.get(end))) end++;
            var others = new ArrayList<Component>();
            var weights = new ArrayList<Component>();
            for (int i = start; i < end; i++) {
                var line = lines.get(i);
                (hasKey(line, WEIGHT_KEY::equals) ? weights : others).add(line);
            }
            if (!weights.isEmpty() && !others.isEmpty()) {
                int i = start;
                for (var line : others) lines.set(i++, line);
                for (var line : weights) lines.set(i++, line);
            }
            start = end;
        }
    }

    private static boolean isModifier(Component line) {
        return hasKey(line, key -> key.startsWith("attribute.modifier.")
            || key.startsWith("attributeslib.modifier."));
    }

    private static boolean hasKey(Component line, Predicate<String> predicate) {
        if (line.getContents() instanceof TranslatableContents text) {
            if (predicate.test(text.getKey())) return true;
            for (var arg : text.getArgs()) {
                if (arg instanceof Component component && hasKey(component, predicate)) return true;
            }
        }
        for (var sibling : line.getSiblings()) if (hasKey(sibling, predicate)) return true;
        return false;
    }
    private WeightTooltipOrder() {}
}
