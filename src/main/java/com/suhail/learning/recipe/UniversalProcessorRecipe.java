package com.suhail.learning.recipe;

import com.suhail.learning.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Universal recipe class that works for any processor.
 * Each processor registers its own RecipeType and RecipeSerializer,
 * but they all share this single Recipe implementation.
 */
public record UniversalProcessorRecipe(String processorName, List<SizedIngredient> itemInputs,
                                       List<SizedFluidIngredient> fluidInputs, List<ItemStack> itemOutputs,
                                       List<FluidStack> fluidOutputs, int processTime,
                                       int energyPerTick) implements Recipe<ProcessorRecipeInput> {

    @Override
    public boolean matches(@NonNull ProcessorRecipeInput input, Level level) {
        if (level.isClientSide()) return false;

        // Check all item inputs match
        for (int i = 0; i < itemInputs.size(); i++) {
            if (i >= input.size()) return false;
            if (!itemInputs.get(i).test(input.getItem(i))) return false;
        }

        // Check all fluid inputs match
        for (int i = 0; i < fluidInputs.size(); i++) {
            if (i >= input.fluidSize()) return false;
            if (!fluidInputs.get(i).test(input.getFluid(i))) return false;
        }

        return true;
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull ProcessorRecipeInput input, HolderLookup.@NonNull Provider registries) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.getFirst().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NonNull ItemStack getResultItem(HolderLookup.@NonNull Provider registries) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.getFirst().copy();
    }

    @Override
    public @NonNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (SizedIngredient si : itemInputs) {
            list.add(si.ingredient());
        }
        return list;
    }

    @Override
    public @NonNull RecipeSerializer<?> getSerializer() {
        return ModEntries.get(processorName).recipeSerializer().get();
    }

    @Override
    public @NonNull RecipeType<?> getType() {
        return ModEntries.get(processorName).recipeType().get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    // --- Getters ---

    public boolean isComplete() {
        for (SizedIngredient si : itemInputs) {
            if (si.ingredient().isEmpty()) return false;
        }
        for (SizedFluidIngredient sfi : fluidInputs) {
            if (sfi.ingredient().isEmpty()) return false;
        }
        for (ItemStack stack : itemOutputs) {
            if (stack.isEmpty()) return false;
        }
        for (FluidStack stack : fluidOutputs) {
            if (stack.isEmpty()) return false;
        }
        return true;
    }
}
