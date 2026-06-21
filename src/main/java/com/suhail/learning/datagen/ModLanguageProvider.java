package com.suhail.learning.datagen;

import com.suhail.learning.registration.ArmorSetEntry;
import com.suhail.learning.registration.MaterialEntry;
import com.suhail.learning.registration.ToolSetEntry;
import com.suhail.learning.setup.ModEntries;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static com.suhail.learning.Main.MODID;
import static com.suhail.learning.util.TextUtils.convertToName;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), MODID, locale);
    }

    @Override
    protected void addTranslations() {
        labels();
        for (String name : ModEntries.ENTRIES.keySet()) {
            if (ModEntries.get(name).hasBlock()) {
                add(ModEntries.get(name).block().get(), convertToName(name));
                if (ModEntries.get(name).hasRecipes()) {
                    add("emi.category.%s.%s".formatted(MODID, name), convertToName(name));

                }
                continue;
            }
            if (ModEntries.get(name).hasItem()) {
                add(ModEntries.get(name).item().get(), convertToName(name));
                continue;
            }
            if (ModEntries.get(name).toolSetEntry() instanceof ToolSetEntry toolSet) {
                add(toolSet.sword().get(), convertToName(toolSet.name + "_sword"));
                add(toolSet.pickaxe().get(), convertToName(toolSet.name + "_pickaxe"));
                add(toolSet.axe().get(), convertToName(toolSet.name + "_axe"));
                add(toolSet.shovel().get(), convertToName(toolSet.name + "_shovel"));
                add(toolSet.hoe().get(), convertToName(toolSet.name + "_hoe"));
                continue;
            }
            if (ModEntries.get(name).armorSetEntry() instanceof ArmorSetEntry armorSet) {
                add(armorSet.helmet().get(), convertToName(armorSet.name + "_helmet"));
                add(armorSet.chestplate().get(), convertToName(armorSet.name + "_chestplate"));
                add(armorSet.leggings().get(), convertToName(armorSet.name + "_leggings"));
                add(armorSet.boots().get(), convertToName(armorSet.name + "_boots"));
                continue;
            }
            if (ModEntries.get(name).materialEntry() instanceof MaterialEntry materialEntry) {
                if (materialEntry.hasOre()) {
                    add(materialEntry.oreBlock().get(), convertToName(materialEntry.name + "_ore"));
                }
                if (materialEntry.hasBlock()) {
                    add(materialEntry.storageBlock().get(), convertToName(materialEntry.name + "_block"));
                }
                if (materialEntry.hasIngot()) {
                    add(materialEntry.ingot().get(), convertToName(materialEntry.name + "_ingot"));
                }
                if (materialEntry.hasGem()) {
                    add(materialEntry.gem().get(), convertToName(materialEntry.name + "_gem"));
                }
                if (materialEntry.hasRawOre()) {
                    add(materialEntry.rawOre().get(), convertToName("raw_" + materialEntry.name));
                }
                if (materialEntry.hasDust()) {
                    add(materialEntry.dust().get(), convertToName(materialEntry.name + "_dust"));
                }
                if (materialEntry.hasPlate()) {
                    add(materialEntry.plate().get(), convertToName(materialEntry.name + "_plate"));
                }
                if (materialEntry.hasNugget()) {
                    add(materialEntry.nugget().get(), convertToName(materialEntry.name + "_nugget"));
                }
                if (materialEntry.hasFluid()) {
                    var fluid = materialEntry.materialFluid();
                    String fluidName = materialEntry.fluidDefinition.isMolten
                            ? "molten_" + materialEntry.name
                            : materialEntry.name + "_fluid";
                    add(fluid.bucket().get(), convertToName(fluidName + "_bucket"));
                    add("fluid_type.%s.%s".formatted(MODID, fluidName), convertToName(fluidName));
                }
            }
        }
    }

    private void labels() {
        add("screen.%s.side_config".formatted(MODID), "Side Configuration");
        add("screen.%s.slot_selection".formatted(MODID), "Select Slot");
        add("screen.%s.multiblock.assembled".formatted(MODID), "Assembled");
        add("screen.%s.multiblock.not_assembled".formatted(MODID), "Not Assembled");
        add("tooltip.fluid.empty", "Empty");
    }
}
