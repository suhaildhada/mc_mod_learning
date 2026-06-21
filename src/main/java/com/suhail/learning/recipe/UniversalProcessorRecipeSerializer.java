package com.suhail.learning.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * A single serializer class that is instantiated per processor.
 * Each instance knows its processor name and bakes it into every recipe it deserializes.
 */
public class UniversalProcessorRecipeSerializer implements RecipeSerializer<UniversalProcessorRecipe> {

    private final String processorName;
    private final MapCodec<UniversalProcessorRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, UniversalProcessorRecipe> streamCodec;

    public UniversalProcessorRecipeSerializer(String processorName) {
        this.processorName = processorName;

        this.codec = RecordCodecBuilder.mapCodec(inst -> inst.group(
                SizedIngredient.FLAT_CODEC.listOf()
                        .optionalFieldOf("item_inputs", List.of())
                        .forGetter(UniversalProcessorRecipe::itemInputs),
                SizedFluidIngredient.FLAT_CODEC.listOf()
                        .optionalFieldOf("fluid_inputs", List.of())
                        .forGetter(UniversalProcessorRecipe::fluidInputs),
                ItemStack.CODEC.listOf()
                        .optionalFieldOf("item_outputs", List.of())
                        .forGetter(UniversalProcessorRecipe::itemOutputs),
                FluidStack.CODEC.listOf()
                        .optionalFieldOf("fluid_outputs", List.of())
                        .forGetter(UniversalProcessorRecipe::fluidOutputs),
                Codec.INT
                        .fieldOf("process_time")
                        .forGetter(UniversalProcessorRecipe::processTime),
                Codec.INT
                        .fieldOf("energy_per_tick")
                        .forGetter(UniversalProcessorRecipe::energyPerTick)
        ).apply(inst, (itemIn, fluidIn, itemOut, fluidOut, time, energy) ->
                new UniversalProcessorRecipe(processorName, itemIn, fluidIn, itemOut, fluidOut, time, energy)
        ));

        this.streamCodec = new StreamCodec<>() {
            private final StreamCodec<RegistryFriendlyByteBuf, List<SizedIngredient>> ITEM_INPUT_LIST =
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list());
            private final StreamCodec<RegistryFriendlyByteBuf, List<SizedFluidIngredient>> FLUID_INPUT_LIST =
                    SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list());
            private final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> ITEM_OUTPUT_LIST =
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list());
            private final StreamCodec<RegistryFriendlyByteBuf, List<FluidStack>> FLUID_OUTPUT_LIST =
                    FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list());

            @Override
            public @NonNull UniversalProcessorRecipe decode(@NonNull RegistryFriendlyByteBuf buf) {
                List<SizedIngredient> itemInputs = ITEM_INPUT_LIST.decode(buf);
                List<SizedFluidIngredient> fluidInputs = FLUID_INPUT_LIST.decode(buf);
                List<ItemStack> itemOutputs = ITEM_OUTPUT_LIST.decode(buf);
                List<FluidStack> fluidOutputs = FLUID_OUTPUT_LIST.decode(buf);
                int processTime = buf.readVarInt();
                int energyPerTick = buf.readVarInt();
                return new UniversalProcessorRecipe(
                        processorName, itemInputs, fluidInputs, itemOutputs, fluidOutputs, processTime, energyPerTick
                );
            }

            @Override
            public void encode(@NonNull RegistryFriendlyByteBuf buf, @NonNull UniversalProcessorRecipe recipe) {
                ITEM_INPUT_LIST.encode(buf, recipe.itemInputs());
                FLUID_INPUT_LIST.encode(buf, recipe.fluidInputs());
                ITEM_OUTPUT_LIST.encode(buf, recipe.itemOutputs());
                FLUID_OUTPUT_LIST.encode(buf, recipe.fluidOutputs());
                buf.writeVarInt(recipe.processTime());
                buf.writeVarInt(recipe.energyPerTick());
            }
        };
    }

    @Override
    public @NonNull MapCodec<UniversalProcessorRecipe> codec() {
        return codec;
    }

    @Override
    public @NonNull StreamCodec<RegistryFriendlyByteBuf, UniversalProcessorRecipe> streamCodec() {
        return streamCodec;
    }
}
