package igentuman.modtemplate.block_entity;

import igentuman.modtemplate.container.UniversalProcessorContainer;
import igentuman.modtemplate.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class UniversalProcessorBE extends GlobalBlockEntity implements MenuProvider {

    public UniversalProcessorBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.modtemplate." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new UniversalProcessorContainer(containerId, playerInventory, this, containerData);
    }
}
