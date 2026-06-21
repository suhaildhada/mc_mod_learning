package com.suhail.learning.datagen.tag;

import com.suhail.learning.registration.MaterialEntry;
import com.suhail.learning.registration.ModEntry;
import com.suhail.learning.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static com.suhail.learning.Main.MODID;
import static com.suhail.learning.Main.rl;

public class ModFluidTagProvider extends FluidTagsProvider {
    public ModFluidTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() instanceof MaterialEntry mat && mat.hasFluid()) {
                var materialFluid = mat.materialFluid();
                String fluidName = mat.fluidDefinition.isMolten ? "molten_" + mat.name : mat.name;

                // Tag both source and flowing under a mod-namespaced tag
                var fluidTag = net.minecraft.tags.FluidTags.create(
                        rl(fluidName)
                );
                tag(fluidTag)
                        .add(materialFluid.source().getKey())
                        .add(materialFluid.flowing().getKey());

                // Common tags (c: namespace)
                // e.g. c:molten_silver for molten metals, c:silver for non-molten fluids
                var commonFluidTag = net.minecraft.tags.FluidTags.create(
                        ResourceLocation.fromNamespaceAndPath("c", fluidName)
                );
                tag(commonFluidTag)
                        .add(materialFluid.source().getKey())
                        .add(materialFluid.flowing().getKey());

                // Also add a base material tag (c:silver) for molten fluids
                if (mat.fluidDefinition.isMolten) {
                    var commonMaterialTag = net.minecraft.tags.FluidTags.create(
                            ResourceLocation.fromNamespaceAndPath("c", mat.name)
                    );
                    tag(commonMaterialTag)
                            .add(materialFluid.source().getKey())
                            .add(materialFluid.flowing().getKey());
                }
            }
        }
    }
}
