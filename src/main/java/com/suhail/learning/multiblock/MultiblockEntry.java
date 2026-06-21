package com.suhail.learning.multiblock;

import com.suhail.learning.api.impl.MultiblockCacheImpl;
import com.suhail.learning.api.multiblock.IMultiblockCache;
import com.suhail.learning.api.multiblock.IMultiblockLogic;
import com.suhail.learning.api.multiblock.IMultiblockValidator;
import com.suhail.learning.registration.ModEntry;
import com.suhail.learning.util.MultiblockStructure;
import com.suhail.learning.util.MultiblocksProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public record MultiblockEntry(String name, Supplier<IMultiblockValidator> validatorSupplier,
                              Supplier<IMultiblockLogic> logicSupplier, Supplier<IMultiblockCache> cacheSupplier,
                              List<Supplier<Block>> requiredBlocks, ModEntry controllerEntry,
                              List<ModEntry> portEntries) {

    public MultiblockEntry(String name,
                           Supplier<IMultiblockValidator> validatorSupplier,
                           Supplier<IMultiblockLogic> logicSupplier,
                           Supplier<IMultiblockCache> cacheSupplier,
                           List<Supplier<Block>> requiredBlocks,
                           ModEntry controllerEntry,
                           List<ModEntry> portEntries) {
        this.name = name;
        this.validatorSupplier = validatorSupplier;
        this.logicSupplier = logicSupplier;
        this.cacheSupplier = cacheSupplier;
        this.requiredBlocks = requiredBlocks != null ? List.copyOf(requiredBlocks) : List.of();
        this.controllerEntry = controllerEntry;
        this.portEntries = portEntries != null ? List.copyOf(portEntries) : List.of();
    }

    public MultiblockEntry(String name,
                           Supplier<IMultiblockValidator> validatorSupplier,
                           Supplier<IMultiblockLogic> logicSupplier,
                           Supplier<IMultiblockCache> cacheSupplier,
                           List<Supplier<Block>> requiredBlocks) {
        this(name, validatorSupplier, logicSupplier, cacheSupplier, requiredBlocks, null, Collections.emptyList());
    }

    public MultiblockEntry(String name,
                           Supplier<IMultiblockValidator> validatorSupplier,
                           Supplier<IMultiblockLogic> logicSupplier,
                           Supplier<IMultiblockCache> cacheSupplier) {
        this(name, validatorSupplier, logicSupplier, cacheSupplier, Collections.emptyList(), null, Collections.emptyList());
    }

    public static MultiblockEntry of(String name,
                                     Supplier<IMultiblockValidator> validator,
                                     Supplier<IMultiblockLogic> logic) {
        return new MultiblockEntry(name, validator, logic, MultiblockCacheImpl::new);
    }

    /**
     * Returns true if every block referenced by this multiblock resolves to a registered, non-air block.
     */
    public boolean isBuildable() {
        if (requiredBlocks.isEmpty()) return false;
        for (Supplier<Block> sup : requiredBlocks) {
            Block block;
            try {
                block = sup.get();
            } catch (Exception e) {
                return false;
            }
            if (block == null || block == Blocks.AIR) return false;
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
            if (key == null || !BuiltInRegistries.BLOCK.containsKey(key)) return false;
        }
        return true;
    }

    /**
     * Looks up an example structure for this entry. First checks already-loaded structures in
     * {@link MultiblocksProvider}; falls back to direct classpath load from
     * {@code /data/<namespace>/example_structures/<name>.nbt}. Returns null if no file exists.
     */
    public MultiblockStructure getExampleStructure() {
        for (MultiblockStructure s : MultiblocksProvider.getStructures()) {
            ResourceLocation id = s.getId();
            if (id != null && id.getPath().endsWith("/" + name + ".nbt")) return s;
        }
        return MultiblocksProvider.loadStructureFromClasspath(name);
    }
}
