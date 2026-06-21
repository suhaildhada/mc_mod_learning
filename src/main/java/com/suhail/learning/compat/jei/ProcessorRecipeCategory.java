package com.suhail.learning.compat.jei;

import com.suhail.learning.Main;
import com.suhail.learning.recipe.UniversalProcessorRecipe;
import com.suhail.learning.registration.ModEntry;
import com.suhail.learning.util.caps.FluidCapDefinition;
import com.suhail.learning.util.caps.ItemCapDefinition;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static com.suhail.learning.util.TextUtils.getMutableComponent;

public class ProcessorRecipeCategory implements IRecipeCategory<UniversalProcessorRecipe> {

    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 20;
    private static final int START_X = 1;
    private static final int START_Y = 1;
    private static final int OUTPUT_GAP = 30;

    public final RecipeType<UniversalProcessorRecipe> recipeType;
    private final ModEntry modEntry;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    private final int itemInputCount;
    private final int fluidInputCount;
    private final int itemOutputCount;
    private final int fluidOutputCount;

    public ProcessorRecipeCategory(IGuiHelper guiHelper, ModEntry entry, RecipeType<UniversalProcessorRecipe> recipeType) {
        this.recipeType = recipeType;
        this.modEntry = entry;
        this.title = getMutableComponent("block." + Main.MODID + "." + entry.name());

        ItemCapDefinition itemCap = entry.itemCap();
        FluidCapDefinition fluidCap = entry.fluidCap();

        this.itemInputCount = itemCap != null ? itemCap.inputSlots : 0;
        this.fluidInputCount = fluidCap != null ? fluidCap.inputTanks.size() : 0;
        this.itemOutputCount = itemCap != null ? itemCap.outputSlots : 0;
        this.fluidOutputCount = fluidCap != null ? fluidCap.outputTanks.size() : 0;

        int totalInputs = itemInputCount + fluidInputCount;
        int totalOutputs = itemOutputCount + fluidOutputCount;
        int totalSlots = totalInputs + totalOutputs;

        int width = START_X + totalSlots * SLOT_SPACING + (totalOutputs > 0 ? OUTPUT_GAP : 0) + 4;
        int height = START_Y + SLOT_SPACING + 4;

        this.background = guiHelper.createBlankDrawable(Math.max(width, 40), Math.max(height, 24));

        if (entry.hasItem()) {
            this.icon = guiHelper.createDrawableItemStack(new ItemStack(entry.item().get()));
        } else {
            this.icon = null;
        }
    }

    @Override
    public RecipeType<UniversalProcessorRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, UniversalProcessorRecipe recipe, IFocusGroup focuses) {
        int x = START_X;
        int y = START_Y;

        // Item input slots
        List<SizedIngredient> itemInputs = recipe.itemInputs();
        for (int i = 0; i < itemInputCount; i++) {
            if (i < itemInputs.size()) {
                SizedIngredient si = itemInputs.get(i);
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .addItemStacks(Arrays.stream(si.ingredient().getItems())
                                .map(stack -> {
                                    ItemStack copy = stack.copy();
                                    copy.setCount(si.count());
                                    return copy;
                                })
                                .toList());
            } else {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y);
            }
            x += SLOT_SPACING;
        }

        // Fluid input slots
        List<SizedFluidIngredient> fluidInputs = recipe.fluidInputs();
        for (int i = 0; i < fluidInputCount; i++) {
            if (i < fluidInputs.size()) {
                SizedFluidIngredient sfi = fluidInputs.get(i);
                var slot = builder.addSlot(RecipeIngredientRole.INPUT, x, y);
                for (FluidStack fs : sfi.getFluids()) {
                    FluidStack copy = fs.copy();
                    copy.setAmount(sfi.amount());
                    slot.addFluidStack(copy.getFluid(), copy.getAmount());
                }
                slot.setFluidRenderer(sfi.amount(), false, SLOT_SIZE - 2, SLOT_SIZE - 2);
            } else {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y);
            }
            x += SLOT_SPACING;
        }

        // Gap before outputs
        x += OUTPUT_GAP;

        // Item output slots
        List<ItemStack> itemOutputs = recipe.itemOutputs();
        for (int i = 0; i < itemOutputCount; i++) {
            if (i < itemOutputs.size()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .addItemStack(itemOutputs.get(i));
            } else {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y);
            }
            x += SLOT_SPACING;
        }

        // Fluid output slots
        List<FluidStack> fluidOutputs = recipe.fluidOutputs();
        for (int i = 0; i < fluidOutputCount; i++) {
            if (i < fluidOutputs.size()) {
                FluidStack fs = fluidOutputs.get(i);
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .addFluidStack(fs.getFluid(), fs.getAmount())
                        .setFluidRenderer(fs.getAmount(), false, SLOT_SIZE - 2, SLOT_SIZE - 2);
            } else {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y);
            }
            x += SLOT_SPACING;
        }
    }
}
