package dev.depthward.stamina;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShieldWeaponRuleTest {
    private static final List<ShieldWeaponRule> DEFAULTS = ShieldWeaponRule.defaults().stream().map(ShieldWeaponRule::parse).toList();
    private static double tetra(String item, String key, String value) {
        return ShieldWeaponRule.firstMatch(DEFAULTS, "tetra:" + item, tag -> tag.equals("minecraft:swords"),
            path -> path.equals(List.of(key)) ? value : null, 1);
    }
    @Test void tetraBladeRulesTakePriorityOverSwordTags() {
        assertEquals(1.6, tetra("modular_sword", "sword/blade", "sword/heavy_blade"));
        assertEquals(0.6, tetra("modular_sword", "sword/blade", "sword/rapier_blade"));
        assertEquals(0.6, tetra("modular_sword", "sword/blade", "sword/short_blade"));
        assertEquals(1, tetra("modular_sword", "sword/blade", "sword/katana_blade"));
        assertEquals(1, tetra("modular_sword", "sword/blade", "sword/basic_blade"));
    }
    @Test void tetraToolPartsRetainMultipliers() {
        for (String side : List.of("left", "right")) {
            for (var pair : Map.of("basic_axe_", 1.5, "basic_hammer_", 1.8, "basic_pickaxe_", 1.4,
                "hoe_", 0.7, "reaping_scythe_", 0.7).entrySet())
                assertEquals(pair.getValue(), tetra("modular_double", "double/head_" + side, "double/" + pair.getKey() + side));
        }
        assertEquals(1.7, tetra("modular_single", "single/head", "single/mace_head"));
        assertEquals(0.6, tetra("modular_single", "single/head", "single/spearhead"));
        assertEquals(0.7, tetra("modular_single", "single/head", "single/basic_shovel"));
    }
    @Test void vanillaTagsAndUnknownWeaponsUseTheirConfiguredFallback() {
        assertEquals(1.5, ShieldWeaponRule.firstMatch(DEFAULTS, "minecraft:diamond_axe", "minecraft:axes"::equals, path -> null, 1));
        assertEquals(1.5, ShieldWeaponRule.firstMatch(DEFAULTS, "minecraft:iron_pickaxe", "minecraft:pickaxes"::equals, path -> null, 1));
        assertEquals(0.7, ShieldWeaponRule.firstMatch(DEFAULTS, "minecraft:iron_shovel", "minecraft:shovels"::equals, path -> null, 1));
        assertEquals(1, ShieldWeaponRule.firstMatch(DEFAULTS, "custom:unknown", tag -> false, path -> null, 1));
        assertEquals(0.25, ShieldWeaponRule.firstMatch(List.of(), "custom:unknown", tag -> false, path -> null, 0.25));
    }
    @Test void customExactNbtRulesCanBeAddedRemovedAndReordered() {
        var exact = ShieldWeaponRule.parse("item=custom:maul;nbt=weapon.type;equals=hammer;multiplier=2.5");
        var general = ShieldWeaponRule.parse("item=custom:maul;multiplier=0.5");
        assertTrue(exact.matches("custom:maul", tag -> false, path -> path.equals(List.of("weapon", "type")) ? "hammer" : null));
        assertFalse(exact.matches("custom:maul", tag -> false, path -> "warhammer"));
        assertEquals(2.5, ShieldWeaponRule.firstMatch(List.of(exact, general), "custom:maul", tag -> false, path -> "hammer", 1));
        assertEquals(0.5, ShieldWeaponRule.firstMatch(List.of(general, exact), "custom:maul", tag -> false, path -> "hammer", 1));
        assertEquals(0.5, ShieldWeaponRule.firstMatch(List.of(general), "custom:maul", tag -> false, path -> "hammer", 1));
    }
    @Test void missingNbtNeverMatchesAndZeroMultiplierIsAllowed() {
        assertFalse(ShieldWeaponRule.parse("item=*;nbt=sword/blade;contains=heavy;multiplier=0").matches("tetra:modular_sword", tag -> true, path -> null));
        assertEquals(0, ShieldWeaponRule.firstMatch(List.of(ShieldWeaponRule.parse("item=*;multiplier=0")), "any:item", tag -> false, path -> null, 1));
    }
    @Test void malformedAndUnsafeRulesAreRejected() {
        for (String text : List.of("", "tag=bad tag;multiplier=1", "item=x:y;tag=x:y;multiplier=1",
            "item=x:y", "item=x:y;multiplier=NaN", "item=x:y;multiplier=Infinity", "item=x:y;multiplier=-1",
            "item=x:y;multiplier=1001", "item=x:y;nbt=foo..bar;equals=x;multiplier=1",
            "item=x:y;nbt=foo;multiplier=1", "item=x:y;contains=x;multiplier=1",
            "item=x:y;nbt=foo;equals=x;contains=y;multiplier=1", "item=x:y;typo=1;multiplier=1",
            "item=x:y;multiplier=1;multiplier=2"))
            assertFalse(ShieldWeaponRule.valid(text), text);
        assertTrue(ShieldWeaponRule.defaults().stream().allMatch(ShieldWeaponRule::valid));
    }
}
