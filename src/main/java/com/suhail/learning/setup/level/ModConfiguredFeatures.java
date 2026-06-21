package com.suhail.learning.setup.level;

import com.suhail.learning.config.WorldGen;
import com.suhail.learning.registration.MaterialEntry;
import com.suhail.learning.registration.ModEntry;
import com.suhail.learning.setup.ModEntries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

import static com.suhail.learning.Main.rl;

public class ModConfiguredFeatures {

    private static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, rl(name));
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() == null || !entry.materialEntry().hasWorldgenConfig()) continue;
            MaterialEntry mat = entry.materialEntry();
            List<OreConfiguration.TargetBlockState> targets = List.of(
                OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), mat.oreBlock().get().defaultBlockState()),
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), mat.oreBlock().get().defaultBlockState())
            );
            WorldGen.OreGenConfig cfg = WorldGen.ORE_CONFIGS.get(mat.name);
            int veinSize = (cfg != null && WorldGen.SPEC != null && WorldGen.SPEC.isLoaded())
                ? cfg.veinSize().get() : mat.worldgenQty;
            register(context, registerKey(mat.name + "_ore"), Feature.ORE, new OreConfiguration(targets, veinSize));
        }
    }

    private ModConfiguredFeatures() {
        /* This utility class should not be instantiated */
    }
}
