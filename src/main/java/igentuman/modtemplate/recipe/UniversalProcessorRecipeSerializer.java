package igentuman.modtemplate.recipe;

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
                        .forGetter(UniversalProcessorRecipe::getItemInputs),
                SizedFluidIngredient.FLAT_CODEC.listOf()
                        .optionalFieldOf("fluid_inputs", List.of())
                        .forGetter(UniversalProcessorRecipe::getFluidInputs),
                ItemStack.CODEC.listOf()
                        .optionalFieldOf("item_outputs", List.of())
                        .forGetter(UniversalProcessorRecipe::getItemOutputs),
                FluidStack.CODEC.listOf()
                        .optionalFieldOf("fluid_outputs", List.of())
                        .forGetter(UniversalProcessorRecipe::getFluidOutputs),
                Codec.INT
                        .fieldOf("process_time")
                        .forGetter(UniversalProcessorRecipe::getProcessTime),
                Codec.INT
                        .fieldOf("energy_per_tick")
                        .forGetter(UniversalProcessorRecipe::getEnergyPerTick)
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
            public UniversalProcessorRecipe decode(RegistryFriendlyByteBuf buf) {
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
            public void encode(RegistryFriendlyByteBuf buf, UniversalProcessorRecipe recipe) {
                ITEM_INPUT_LIST.encode(buf, recipe.getItemInputs());
                FLUID_INPUT_LIST.encode(buf, recipe.getFluidInputs());
                ITEM_OUTPUT_LIST.encode(buf, recipe.getItemOutputs());
                FLUID_OUTPUT_LIST.encode(buf, recipe.getFluidOutputs());
                buf.writeVarInt(recipe.getProcessTime());
                buf.writeVarInt(recipe.getEnergyPerTick());
            }
        };
    }

    @Override
    public MapCodec<UniversalProcessorRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, UniversalProcessorRecipe> streamCodec() {
        return streamCodec;
    }
}
