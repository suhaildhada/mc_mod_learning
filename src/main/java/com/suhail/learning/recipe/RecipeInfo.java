package com.suhail.learning.recipe;

import com.suhail.learning.block_entity.GlobalBlockEntity;
import com.suhail.learning.registration.ModEntry;
import com.suhail.learning.setup.ModEntries;
import com.suhail.learning.util.ClientUtil;
import com.suhail.learning.util.caps.FluidCapDefinition;
import com.suhail.learning.util.caps.ItemCapDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.suhail.learning.Main.rlFromString;

public class RecipeInfo {

    public boolean stuck = false;
    public boolean changed = false;
    public int ticks = 0;
    public int ticksNeeded = 0;
    public int energyPerTick = 0;
    public final GlobalBlockEntity be;
    private String recipeId;
    public int multiplier = 1;
    public Recipe<?> recipe;
    public Recipe<?> lastRecipe;
    private final HashMap<String, Recipe<?>> allRecipes = new HashMap<>();

    public RecipeInfo(GlobalBlockEntity be) {
        this.be = be;
    }

    public boolean isDone() {
        return ticks >= ticksNeeded;
    }

    public void tick() {
        changed = false;
        if (!be.supportRecipes()) {
            return;
        }

        if (stuck) {
            if (!produceOutputs()) {
                return;
            }
            changed = true;
            stuck = false;
        }

        if (!hasRecipe()) {
            findRecipe();
        }

        if (!hasRecipe()) {
            return;
        }

        if (be.energyStorage == null) {
            return; // no energy storage, cannot process
        }
        int required = energyPerTick * multiplier;
        if (required > 0) {
            int extracted = be.energyStorage.getEnergyStored() >= required ? required : 0;
            if (extracted < required) {
                return; // not enough energy, stall
            }
            be.energyStorage.drainEnergy(required);
        } else {
            be.energyStorage.setEnergyStored(be.energyStorage.getEnergyStored() - required);
        }

        ticks += multiplier;
        be.progress = getProgress();
        changed = true;

        if (isDone()) {
            lastRecipe = recipe;
            recipe = null;

            if (!produceOutputs()) {
                stuck = true;
            }
        }
    }

    public boolean produceOutputs() {
        if (lastRecipe == null) return true;
        if (!(lastRecipe instanceof UniversalProcessorRecipe upr)) return true;

        ModEntry entry = ModEntries.get(be.name);
        if (entry == null) return true;

        ItemCapDefinition itemCap = entry.itemCap();
        FluidCapDefinition fluidCap = entry.fluidCap();

        List<ItemStack> itemOutputs = upr.itemOutputs();
        List<FluidStack> fluidOutputs = upr.fluidOutputs();

        // Determine output slot range: output slots start right after input slots
        int outputSlotStart = (itemCap != null) ? itemCap.inputSlots : 0;
        int outputSlotCount = (itemCap != null) ? itemCap.outputSlots : 0;

        // Determine output tank range: output tanks start right after input tanks
        int outputTankStart = (fluidCap != null) ? fluidCap.inputTanks.size() : 0;
        int outputTankCount = (fluidCap != null) ? fluidCap.outputTanks.size() : 0;

        // Check if all item outputs can fit
        if (be.contentHandler.hasItemCapability()) {
            var itemHandler = be.contentHandler.getItemHandler();
            for (int i = 0; i < itemOutputs.size(); i++) {
                if (i >= outputSlotCount) return false;
                ItemStack output = itemOutputs.get(i);
                int slot = outputSlotStart + i;
                ItemStack existing = itemHandler.getStackInSlot(slot);
                if (!existing.isEmpty()) {
                    if (!ItemStack.isSameItemSameComponents(existing, output)) return false;
                    if (existing.getCount() + output.getCount() > existing.getMaxStackSize()) return false;
                }
            }
        } else if (!itemOutputs.isEmpty()) {
            return false;
        }

        // Check if all fluid outputs can fit
        if (be.contentHandler.hasFluidCapability()) {
            var fluidHandler = be.contentHandler.getFluidHandler();
            for (int i = 0; i < fluidOutputs.size(); i++) {
                if (i >= outputTankCount) return false;
                int tankIdx = outputTankStart + i;
                FluidStack output = fluidOutputs.get(i);
                int filled = fluidHandler.fillTank(tankIdx, output, IFluidHandler.FluidAction.SIMULATE);
                if (filled < output.getAmount()) return false;
            }
        } else if (!fluidOutputs.isEmpty()) {
            return false;
        }

        // All checks passed - place outputs
        if (be.contentHandler.hasItemCapability()) {
            var itemHandler = be.contentHandler.getItemHandler();
            for (int i = 0; i < itemOutputs.size(); i++) {
                int slot = outputSlotStart + i;
                ItemStack existing = itemHandler.getStackInSlot(slot);
                if (existing.isEmpty()) {
                    itemHandler.setStackInSlot(slot, itemOutputs.get(i).copy());
                } else {
                    existing.grow(itemOutputs.get(i).getCount());
                }
            }
        }

        // Place fluid outputs
        if (be.contentHandler.hasFluidCapability()) {
            var fluidHandler = be.contentHandler.getFluidHandler();
            for (int i = 0; i < fluidOutputs.size(); i++) {
                int tankIdx = outputTankStart + i;
                fluidHandler.fillTank(tankIdx, fluidOutputs.get(i).copy(), IFluidHandler.FluidAction.EXECUTE);
            }
        }

        return true;
    }

    public void consumeInputs() {
        if (recipe == null) return;
        if (!(recipe instanceof UniversalProcessorRecipe upr)) return;

        // Consume item inputs
        List<SizedIngredient> itemInputs = upr.itemInputs();
        if (be.contentHandler.hasItemCapability()) {
            var itemHandler = be.contentHandler.getItemHandler();
            for (int i = 0; i < itemInputs.size(); i++) {
                int count = itemInputs.get(i).count();
                itemHandler.extractItem(i, count, false);
            }
        }

        // Consume fluid inputs
        List<SizedFluidIngredient> fluidInputs = upr.fluidInputs();
        if (be.contentHandler.hasFluidCapability()) {
            var fluidHandler = be.contentHandler.getFluidHandler();
            for (int i = 0; i < fluidInputs.size(); i++) {
                int amount = fluidInputs.get(i).amount();
                fluidHandler.drainTank(i, amount, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        changed = true;
    }

    private void findRecipe() {
        if (lastRecipe != null) {
            if (isValidRecipe(lastRecipe)) {
                setRecipe(lastRecipe);
                return;
            }
        }
        for (Recipe<?> r : getRecipes().values()) {
            if (isValidRecipe(r)) {
                setRecipe(r);
                return;
            }
        }
    }

    public void setRecipe(Recipe<?> recipe) {
        this.recipe = recipe;
        this.lastRecipe = null;
        // Resolve recipeId from the recipes map
        this.recipeId = null;
        for (var entry : getRecipes().entrySet()) {
            if (entry.getValue() == recipe) {
                this.recipeId = entry.getKey();
                break;
            }
        }
        clear();
        if (recipe instanceof UniversalProcessorRecipe upr) {
            this.ticksNeeded = upr.processTime();
            this.energyPerTick = upr.energyPerTick();
        }
        consumeInputs();
    }

    @SuppressWarnings("unchecked")
    public boolean isValidRecipe(Recipe<?> recipe) {
        return recipe instanceof UniversalProcessorRecipe &&
                ((Recipe<ProcessorRecipeInput>) recipe).matches(be.inputs(), getLevel());
    }

    private boolean hasRecipe() {
        return recipe != null;
    }

    public int getProgress() {
        return (int) ((double) ticks / ticksNeeded * 100);
    }

    public void clear() {
        ticks = 0;
        ticksNeeded = 0;
        energyPerTick = 0;
        be.progress = 0;
    }

    public Recipe<?> recipe() {
        if (recipe == null && recipeId != null && !recipeId.isEmpty()) {
            recipe = getRecipeFromTag(recipeId);
        }
        return recipe;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Recipe<?>> getRecipes() {
        Level level = getLevel();
        ModEntry entry = ModEntries.get(be.name);

        if (!allRecipes.isEmpty() || level == null || entry == null) {
            return allRecipes;
        }

        var recipeType = (RecipeType<UniversalProcessorRecipe>) entry.recipeType().get();
        level.getRecipeManager().getAllRecipesFor(recipeType)
                .forEach(r -> {
            if (r.value().isComplete()) {
                allRecipes.put(r.id().toString(), r.value());
            }
        });
        return allRecipes;
    }

    private Recipe<?> getRecipeFromTag(String recipe) {
        Recipe<?> cachedRecipe = getRecipes().getOrDefault(recipe, null);
        if (cachedRecipe != null) {
            return cachedRecipe;
        }
        ResourceLocation id = rlFromString(recipe);
        if (getLevel() == null) return null;
        return getLevel()
                .getRecipeManager()
                .byKey(id)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    private Level getLevel() {
        if (be != null) return be.getLevel();
        return switch (FMLEnvironment.dist) {
            case CLIENT -> ClientUtil.tryGetClientWorld();
            case DEDICATED_SERVER -> ServerLifecycleHooks.getCurrentServer().overworld();
        };
    }

    public Tag save() {
        CompoundTag data = new CompoundTag();
        data.putInt("ticks", ticks);
        data.putInt("ticksNeeded", ticksNeeded);
        data.putInt("energyPerTick", energyPerTick);
        if (recipe != null && recipeId != null) {
            data.putString("recipe", recipeId);
        }
        return data;
    }

    public void load(Tag nbt) {
        if (nbt instanceof CompoundTag compoundTag) {
            ticks = compoundTag.getInt("ticks");
            ticksNeeded = compoundTag.getInt("ticksNeeded");
            energyPerTick = compoundTag.getInt("energyPerTick");
            recipeId = compoundTag.getString("recipe");
            recipe = null;
            if (!recipeId.isEmpty()) {
                recipe = getRecipeFromTag(recipeId);
            }
        }
    }
}
