package com.suhail.learning.block;

import com.mojang.serialization.MapCodec;
import com.suhail.learning.block_entity.MultiblockPartBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class MultiblockPartBlock extends BaseEntityBlock {

    public static MapCodec<MultiblockPartBlock> CODEC;
    private final String name;
    private final Supplier<BlockEntityType<? extends MultiblockPartBE>> beTypeSupplier;

    public MultiblockPartBlock(BlockBehaviour.Properties props, String name,
                                Supplier<BlockEntityType<? extends MultiblockPartBE>> beTypeSupplier) {
        super(props);
        this.name = name;
        this.beTypeSupplier = beTypeSupplier;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        if (CODEC == null) {
            CODEC = simpleCodec(props -> new MultiblockPartBlock(props, name, beTypeSupplier));
        }
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new MultiblockPartBE(beTypeSupplier.get(), pos, state, name);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos,
                                                        @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MultiblockPartBE partBE) {
                serverPlayer.openMenu(partBE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
