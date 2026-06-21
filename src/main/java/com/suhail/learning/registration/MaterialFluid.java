package com.suhail.learning.registration;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Holds the registered fluid objects for a material fluid (source, flowing, block, bucket, fluid type).
 */
public record MaterialFluid(DeferredHolder<FluidType, FluidType> fluidType, DeferredHolder<Fluid, FlowingFluid> source,
                            DeferredHolder<Fluid, FlowingFluid> flowing, DeferredBlock<LiquidBlock> fluidBlock,
                            DeferredItem<Item> bucket) {
}
