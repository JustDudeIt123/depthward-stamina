package dev.depthward.stamina;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/** Ordered, data-only rules. Slash characters in Tetra NBT keys are literal, dots separate compounds. */
public record ShieldWeaponRule(boolean tag, String selector, List<String> nbtPath,
                               boolean contains, String expected, double multiplier) {
    public static ShieldWeaponRule parse(String text) {
        var fields = new HashMap<String, String>();
        var allowed = Set.of("item", "tag", "nbt", "contains", "equals", "multiplier");
        for (var part : text.split(";", -1)) {
            int separator = part.indexOf('=');
            if (separator < 1) throw new IllegalArgumentException("Expected key=value: " + part);
            String key = part.substring(0, separator).trim();
            String value = part.substring(separator + 1).trim();
            if (!allowed.contains(key) || fields.putIfAbsent(key, value) != null)
                throw new IllegalArgumentException("Unknown or duplicate rule field: " + key);
        }
        if (fields.containsKey("item") == fields.containsKey("tag"))
            throw new IllegalArgumentException("Use exactly one item or tag selector");
        boolean tag = fields.containsKey("tag");
        String selector = fields.get(tag ? "tag" : "item");
        if (!(selector.matches("[a-z0-9_.-]+:[a-z0-9/._-]+") || !tag && selector.equals("*")))
            throw new IllegalArgumentException("Expected namespace:id selector");
        boolean hasNbt = fields.containsKey("nbt");
        if (hasNbt ? fields.containsKey("contains") == fields.containsKey("equals")
                   : fields.containsKey("contains") || fields.containsKey("equals"))
            throw new IllegalArgumentException("NBT requires exactly one contains or equals comparison");
        var path = hasNbt ? List.of(fields.get("nbt").split("\\.", -1)) : List.<String>of();
        if (path.stream().anyMatch(String::isBlank)) throw new IllegalArgumentException("Empty NBT path segment");
        String amount = fields.get("multiplier");
        if (amount == null) throw new IllegalArgumentException("Missing multiplier");
        double multiplier = Double.parseDouble(amount);
        if (!Double.isFinite(multiplier) || multiplier < 0 || multiplier > 1000)
            throw new IllegalArgumentException("Multiplier must be finite and between 0 and 1000");
        return new ShieldWeaponRule(tag, selector, path, fields.containsKey("contains"),
            fields.getOrDefault("contains", fields.get("equals")), multiplier);
    }
    public static boolean valid(Object value) {
        if (!(value instanceof String text)) return false;
        try { parse(text); return true; } catch (IllegalArgumentException ex) { return false; }
    }
    public boolean matches(String itemId, Predicate<String> hasTag, Function<List<String>, String> readNbt) {
        if (tag ? !hasTag.test(selector) : !selector.equals("*") && !selector.equals(itemId)) return false;
        if (nbtPath.isEmpty()) return true;
        String actual = readNbt.apply(nbtPath);
        return actual != null && (contains ? actual.contains(expected) : actual.equals(expected));
    }
    public static double firstMatch(List<ShieldWeaponRule> rules, String itemId, Predicate<String> hasTag,
                                    Function<List<String>, String> readNbt, double fallback) {
        for (var rule : rules) if (rule.matches(itemId, hasTag, readNbt)) return rule.multiplier;
        return fallback;
    }
    public static List<String> defaults() {
        var rules = new ArrayList<String>();
        nbt(rules, "modular_sword", "sword/blade", "short_blade", 0.6);
        nbt(rules, "modular_sword", "sword/blade", "rapier_blade", 0.6);
        nbt(rules, "modular_sword", "sword/blade", "katana_blade", 1);
        nbt(rules, "modular_sword", "sword/blade", "basic_blade", 1);
        nbt(rules, "modular_sword", "sword/blade", "heavy_blade", 1.6);
        // Preserve the original left-head priority; fall back to the right head when no left rule matches.
        for (var side : List.of("left", "right")) {
            String key = "double/head_" + side;
            nbt(rules, "modular_double", key, "basic_axe_" + side, 1.5);
            nbt(rules, "modular_double", key, "basic_hammer_" + side, 1.8);
            nbt(rules, "modular_double", key, "basic_pickaxe_" + side, 1.4);
            nbt(rules, "modular_double", key, "hoe_" + side, 0.7);
            nbt(rules, "modular_double", key, "reaping_scythe_" + side, 0.7);
        }
        nbt(rules, "modular_single", "single/head", "mace_head", 1.7);
        nbt(rules, "modular_single", "single/head", "spearhead", 0.6);
        nbt(rules, "modular_single", "single/head", "basic_shovel", 0.7);
        for (var entry : List.of("axes;1.5", "pickaxes;1.5", "shovels;0.7", "hoes;0.7", "swords;1.0")) {
            var parts = entry.split(";");
            rules.add("tag=minecraft:" + parts[0] + ";multiplier=" + parts[1]);
        }
        return List.copyOf(rules);
    }
    private static void nbt(List<String> rules, String item, String key, String part, double multiplier) {
        rules.add("item=tetra:" + item + ";nbt=" + key + ";contains=" + part + ";multiplier=" + multiplier);
    }
}
