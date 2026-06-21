package igentuman.modtemplate.config;

import igentuman.modtemplate.registration.ModEntry;
import igentuman.modtemplate.setup.ModEntries;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Processors {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final Map<String, ModConfigSpec.BooleanValue> ENABLED = new LinkedHashMap<>();

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("processors");
        ModEntries.ENTRIES.values().stream()
                .filter(e -> e.materialEntry() == null && e.hasBlockEntity())
                .sorted(Comparator.comparing(ModEntry::name))
                .forEach(entry ->
                        ENABLED.put(entry.name(), BUILDER.define(entry.name(), true))
                );
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static boolean isEnabled(String name) {
        ModConfigSpec.BooleanValue val = ENABLED.get(name);
        return val == null || val.get();
    }
}
