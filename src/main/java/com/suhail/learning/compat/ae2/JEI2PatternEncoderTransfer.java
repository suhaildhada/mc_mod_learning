package com.suhail.learning.compat.ae2;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.suhail.learning.network.PacketAE2PatternTransfer;
import com.suhail.learning.recipe.UniversalProcessorRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JEI2PatternEncoderTransfer implements IRecipeTransferHandler<PatternEncodingTermMenu, UniversalProcessorRecipe> {

    private final RecipeType<UniversalProcessorRecipe> recipeType;

    public JEI2PatternEncoderTransfer(RecipeType<UniversalProcessorRecipe> recipeType) {
        this.recipeType = recipeType;
    }

    @Override
    public @NonNull Class<PatternEncodingTermMenu> getContainerClass() {
        return PatternEncodingTermMenu.class;
    }

    @Override
    public @NonNull Optional<MenuType<PatternEncodingTermMenu>> getMenuType() {
        return Optional.of(PatternEncodingTermMenu.TYPE);
    }

    @Override
    public @NonNull RecipeType<UniversalProcessorRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NonNull PatternEncodingTermMenu container, @NonNull UniversalProcessorRecipe recipe, @NonNull IRecipeSlotsView recipeSlots, @NonNull Player player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }

        List<ItemStack> inputItems = new ArrayList<>();
        List<FluidStack> inputFluids = new ArrayList<>();
        List<ItemStack> outputItems = new ArrayList<>();
        List<FluidStack> outputFluids = new ArrayList<>();

        for (SizedIngredient si : recipe.itemInputs()) {
            if (!si.ingredient().isEmpty()) {
                ItemStack stack = si.ingredient().getItems()[0].copy();
                stack.setCount(si.count());
                inputItems.add(stack);
            }
        }

        for (SizedFluidIngredient sfi : recipe.fluidInputs()) {
            if (!sfi.ingredient().isEmpty()) {
                FluidStack fluid = sfi.getFluids()[0].copy();
                fluid.setAmount(sfi.amount());
                inputFluids.add(fluid);
            }
        }

        outputItems.addAll(recipe.itemOutputs());

        outputFluids.addAll(recipe.fluidOutputs());

        PacketDistributor.sendToServer(new PacketAE2PatternTransfer(inputItems, inputFluids, outputItems, outputFluids));
        return null;
    }
}
