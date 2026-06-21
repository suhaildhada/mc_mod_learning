package com.suhail.learning.api.impl;

import com.suhail.learning.api.multiblock.IMultiblockCache;
import com.suhail.learning.api.multiblock.IMultiblockLogic;
import com.suhail.learning.block_entity.MultiblockPartBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MultiblockLogicImpl implements IMultiblockLogic {
    @Override
    public void onFormed(Level level, BlockPos controllerPos, IMultiblockCache cache) {
        long controllerKey = controllerPos.asLong();
        for (long key : cache.getStructurePositions()) {
            if (key == controllerKey) continue;
            BlockPos pos = BlockPos.of(key);
            BlockEntity be = cache.getBlockEntity(level, pos);
            if (be instanceof MultiblockPartBE part) {
                part.setControllerPos(controllerPos);
            }
        }
    }

    @Override
    public void onBroken(Level level, BlockPos controllerPos, IMultiblockCache cache) {
        long controllerKey = controllerPos.asLong();
        for (long key : cache.getStructurePositions()) {
            if (key == controllerKey) continue;
            BlockPos pos = BlockPos.of(key);
            BlockEntity be = cache.getBlockEntity(level, pos);
            if (be instanceof MultiblockPartBE part) {
                part.setControllerPos(null);
            }
        }
    }

    @Override
    public void tickServer(Level level, BlockPos controllerPos, IMultiblockCache cache) {

    }

    @Override
    public void tickClient(Level level, BlockPos controllerPos) {

    }
}
