package com.suhail.learning.block_entity;

import com.suhail.learning.container.UniversalProcessorContainer;
import com.suhail.learning.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class UniversalProcessorBE extends GlobalBlockEntity implements MenuProvider {

    public UniversalProcessorBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("block.modtemplate." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NonNull Inventory playerInventory, @NonNull Player player) {
        return new UniversalProcessorContainer(containerId, playerInventory, this, containerData);
    }
}
