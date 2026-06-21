package com.suhail.learning.container;

import com.suhail.learning.block_entity.MultiblockPartBE;
import com.suhail.learning.setup.ModEntries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class MultiblockPortContainer extends AbstractContainerMenu {

    private final MultiblockPartBE blockEntity;
    private final ContainerLevelAccess access;

    public MultiblockPortContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (MultiblockPartBE) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                null);
    }

    public MultiblockPortContainer(int containerId, Inventory playerInventory,
                                    MultiblockPartBE blockEntity, ContainerData data) {
        super(ModEntries.get(blockEntity.name).menu().get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos());

        ContainerData boundData = data != null ? data : new SimpleContainerData(Math.max(1, blockEntity.getSyncFieldCount()));
        addDataSlots(boundData);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 10 + col * 18, 96 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 10 + col * 18, 154));
        }
    }

    public MultiblockPartBE getBlockEntity() {
        return blockEntity;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return stillValid(access, player, ModEntries.get(blockEntity.name).block().get());
    }
}
