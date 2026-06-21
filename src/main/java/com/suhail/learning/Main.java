package com.suhail.learning;

import com.mojang.logging.LogUtils;
import com.suhail.learning.block_entity.GlobalBlockEntity;
import com.suhail.learning.compat.cc.CCCompatHandler;
import com.suhail.learning.config.*;
import com.suhail.learning.handler.event.ServerEvents;
import com.suhail.learning.multiblock.MultiblockEntry;
import com.suhail.learning.multiblock.MultiblockRegistry;
import com.suhail.learning.network.PacketAE2PatternTransfer;
import com.suhail.learning.network.PacketMultiblockBroken;
import com.suhail.learning.network.PacketMultiblockFormed;
import com.suhail.learning.network.PacketSideConfigToggle;
import com.suhail.learning.registration.ModEntry;
import com.suhail.learning.setup.ModEntries;
import com.suhail.learning.setup.Registers;
import com.suhail.learning.util.MultiblocksProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;

import static com.suhail.learning.setup.ModEntries.EXAMPLE_ITEM;
import static com.suhail.learning.setup.Registers.CREATIVE_MODE_TABS;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Main.MODID)
public class Main {
    public static final String MODID = "learning";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static int TICK_COUNTER = 0;

    // Creates a creative tab with the id "learning:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.%s".formatted(MODID))) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.item().get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.item().get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
//                output.accept(EXAMPLE_MACHINE_BLOCK_ITEM.get());
            }).build());

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        Registers.init(modEventBus);
        ModEntries.init();
        NeoForge.EVENT_BUS.register(new ServerEvents());
        WorldGen.init(
                ModEntries.ENTRIES.values().stream()
                        .map(ModEntry::materialEntry)
                        .filter(mat -> mat != null && mat.hasWorldgenConfig())
                        .toList()
        );
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListener);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::registerClientReloadListeners);
        }
        if (ModList.get().isLoaded("computercraft")) {
            CCCompatHandler.register(modEventBus);
        }
        modContainer.registerConfig(ModConfig.Type.COMMON, Common.SPEC, MODID + "/common.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, WorldGen.SPEC, MODID + "/worldgen.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Materials.SPEC, MODID + "/materials.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Processors.SPEC, MODID + "/processors.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Multiblocks.SPEC, MODID + "/multiblocks.toml");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("1");
        registrar.playToServer(
                PacketSideConfigToggle.TYPE,
                PacketSideConfigToggle.STREAM_CODEC,
                PacketSideConfigToggle::handle
        );
        registrar.playToServer(
                PacketAE2PatternTransfer.TYPE,
                PacketAE2PatternTransfer.STREAM_CODEC,
                PacketAE2PatternTransfer::handle
        );
        registrar.playToClient(
                PacketMultiblockFormed.TYPE,
                PacketMultiblockFormed.STREAM_CODEC,
                PacketMultiblockFormed::handle
        );
        registrar.playToClient(
                PacketMultiblockBroken.TYPE,
                PacketMultiblockBroken.STREAM_CODEC,
                PacketMultiblockBroken::handle
        );
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {

        for (ModEntry entry : ModEntries.ENTRIES.values()) {

            boolean hasBlockEntity = entry.hasBlockEntity();
            if (!hasBlockEntity) {
                continue;
            }

            handleCapabilities(event, entry);
        }

        handleMultiBlockCapabilities(event);
    }

    private static void handleMultiBlockCapabilities(RegisterCapabilitiesEvent event) {
        // Multiblock ports proxy capabilities from their controller. The port's own ModEntry
        // has no cap definitions, so register caps here unconditionally for every port BE type.
        for (MultiblockEntry mb : MultiblockRegistry.ENTRIES.values()) {
            for (ModEntry port : mb.portEntries()) {
                if (!port.hasBlockEntity()) {
                    continue;
                }
                handleItemCap(event, port);
                handleFluidCap(event, port);
                handleEnergyCap(event, port);
            }
        }
    }

    private static void handleCapabilities(RegisterCapabilitiesEvent event, ModEntry entry) {
        if (entry.itemCap() != null) {
            handleItemCap(event, entry);
        }
        if (entry.fluidCap() != null) {
            handleFluidCap(event, entry);
        }
        if (entry.energyCap() != null) {
            handleEnergyCap(event, entry);
        }
    }

    private static void handleEnergyCap(RegisterCapabilitiesEvent event, ModEntry entry) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                entry.blockEntity().get(),
                (be, side) -> {
                    if (be instanceof GlobalBlockEntity gbe) {
                        return gbe.getEnergyHandler(side);
                    }
                    return null;
                }
        );
    }

    private static void handleFluidCap(RegisterCapabilitiesEvent event, ModEntry entry) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                entry.blockEntity().get(),
                (be, side) -> {
                    if (be instanceof GlobalBlockEntity gbe) {
                        return gbe.getFluidHandler(side);
                    }
                    return null;
                }
        );
    }

    private static void handleItemCap(RegisterCapabilitiesEvent event, ModEntry entry) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                entry.blockEntity().get(),
                (be, side) -> {
                    if (be instanceof GlobalBlockEntity gbe) {
                        return gbe.getItemHandler(side);
                    }
                    return null;
                }
        );
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(MultiblocksProvider.getInstance());
    }

    private void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(MultiblocksProvider.getInstance());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() != null) {
                addMaterialEntries(event, entry);
                continue;
            }
            if (entry.hasBlockEntity() && Processors.isEnabled(entry.name())) {
                addFunctionalBlocks(event, entry);
                continue;
            }
            if (entry.hasBlock()) {
                addBuildingBlocks(event, entry);
                continue;
            }
            if (entry.hasToolSet()) {
                addToolSets(event, entry);
                continue;
            }
            if (entry.hasArmorSet()) {
                addArmorSets(event, entry);
                continue;
            }
            if (entry.hasItem() && event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                event.accept(entry.item());
            }
        }
    }

    private static void addArmorSets(BuildCreativeModeTabContentsEvent event, ModEntry entry) {
        var armor = entry.armorSetEntry();
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(armor.helmet());
            event.accept(armor.chestplate());
            event.accept(armor.leggings());
            event.accept(armor.boots());
        }
    }

    private static void addToolSets(BuildCreativeModeTabContentsEvent event, ModEntry entry) {
        var tools = entry.toolSetEntry();
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(tools.sword());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(tools.pickaxe());
            event.accept(tools.axe());
            event.accept(tools.shovel());
            event.accept(tools.hoe());
        }
    }

    private static void addBuildingBlocks(BuildCreativeModeTabContentsEvent event, ModEntry entry) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(entry.item());
        }
    }

    private static void addFunctionalBlocks(BuildCreativeModeTabContentsEvent event, ModEntry entry) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(entry.item());
        }
    }

    // Helper: run the action only if enabled
    private static void acceptIf(BuildCreativeModeTabContentsEvent event, boolean enabled, Runnable action) {
        if (enabled) action.run();
    }

    // Helper: convenience to check Materials.isTypeEnabled with matName
    private static boolean enabled(String matName, String type) {
        return Materials.isTypeEnabled(matName, type);
    }

    private static void addMaterialEntries(BuildCreativeModeTabContentsEvent event, ModEntry entry) {
        var mat = entry.materialEntry();
        String matName = mat.name;
        var tab = event.getTabKey();

        if (tab == CreativeModeTabs.NATURAL_BLOCKS) {
            acceptIf(event, mat.hasOre() && enabled(matName, "ore"), () -> event.accept(mat.oreItem()));
            acceptIf(event, mat.hasRawOre() && enabled(matName, "raw_ore"), () -> event.accept(mat.rawOre()));
            return;
        }

        if (tab == CreativeModeTabs.BUILDING_BLOCKS) {
            acceptIf(event, mat.hasBlock() && enabled(matName, "block"), () -> event.accept(mat.storageItem()));
            return;
        }

        if (tab == CreativeModeTabs.INGREDIENTS) {
            acceptIf(event, mat.hasIngot() && enabled(matName, "ingot"), () -> event.accept(mat.ingot()));
            acceptIf(event, mat.hasGem() && enabled(matName, "gem"), () -> event.accept(mat.gem()));
            acceptIf(event, mat.hasDust() && enabled(matName, "dust"), () -> event.accept(mat.dust()));
            acceptIf(event, mat.hasPlate() && enabled(matName, "plate"), () -> event.accept(mat.plate()));
            acceptIf(event, mat.hasNugget() && enabled(matName, "nugget"), () -> event.accept(mat.nugget()));
            return;
        }

        if (tab == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            acceptIf(event, mat.hasFluid() && enabled(matName, "fluid"), () -> event.accept(mat.bucket()));
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation rlFromString(String name) {
        return ResourceLocation.tryParse(name);
    }

    public static int getTickCounter() {
        return TICK_COUNTER;
    }

    public static void updateTickCounter() {
        if (TICK_COUNTER + 1 == Integer.MAX_VALUE) {
            TICK_COUNTER = 0;
        } else {
            TICK_COUNTER++;
        }

    }
}
