# Changelog

## v1.0.1 - NeoForge 1.21.1

### Multiblock API
- `MultiblockEntryBuilder` - fluent registration for controller + ports, casing/interior block predicates, or NBT structure templates
- `MultiblockRegistry` holds all multiblock entries; capabilities auto-wired in `Main.registerCapabilities`
- `IMultiblockValidator` / `IMultiblockLogic` / `IMultiblockCache` interfaces with default impls (`CubicMultiblockValidator`, `DeterminedMultiblockValidator`, `MultiblockLogicImpl`, `MultiblockCacheImpl`)
- `MultiblockPattern` + `BlockPredicate` for declarative shape definitions
- `MultiblockStructure` + `.nbt` template loading for arbitrary structures
- `MultiblockControllerBlock` / `MultiblockPartBlock` with form/break lifecycle and port linking
- `MultiblockControllerBE` / `MultiblockPartBE` - controller aggregates port I/O; ports forward capabilities to controller
- `MultiblockControllerContainer` / `MultiblockPortContainer` + screens with structure preview
- `StructurePreviewRenderer` - in-world ghost rendering of unformed structure
- `PacketMultiblockFormed` / `PacketMultiblockBroken` network sync
- `multiblocks` TOML config gates entries

### Recipe Integration
- JEI: `MultiblockExampleCategory` + `MultiblockStructureRecipe` show multiblock assembly
- EMI: `MultiblockExampleEmiRecipe` mirrors JEI integration
- KubeJS: multiblock recipe schema (`foo_controller/dirt_silver_to_water` example)
- `FooMultiblockRecipes` datagen example

### ComputerCraft
- `ControllerPerihperal` - peripheral exposing multiblock controller state, I/O, recipe progress to CC programs

### Example Content
- `foo_controller` + `foo_port` blocks demonstrating full multiblock setup
- `foo_multiblock.nbt` example structure

### Docs
- `docs/multiblocks.md` - full multiblock registration guide

## v1.0.0 - NeoForge 1.21.1

### Core Framework
- Single-point registration via `ModEntryBuilder` - one builder chain registers block, block entity, menu, screen, recipe type, serializer, and creative tab entry
- `ModEntry` record holds all deferred references in one object

### Machine / Processor System
- Universal processor block, block entity, container, screen, and recipe classes - reusable across any machine type
- Side configuration system: per-slot, per-face push/pull/disable for items and fluids; NBT-persistent; auto-transfer via `SidedContentHandler`
- Block updates propagated on content handler changes

### GUI
- Energy bar widget (`EnergyBar`) - renders live FE fill bar with hover tooltip
- Slot layout fixes and GUI rendering improvements
- Side config button and panel
- Fluid rendering in GUI

### Materials
- `addMetalOreMaterial` / `addCrystalOreMaterial` - registers full material set (ore, ingot, dust, plate, nugget, raw ore, storage block, molten fluid, bucket) from one call
- Fluid, item, and block tags auto-registered per material

### World Generation
- Automatic ore worldgen: configured features, placed features, and biome modifiers auto-registered when `worldgenQty > 0`
- Height range and vein counts driven by live TOML config

### Recipe Integration
- JEI: dynamic category, catalyst, and recipe registration for every processor entry
- EMI support - recipe viewer integration + transfer to AE2 pattern encoder
- KubeJS: processor recipe schemas auto-registered for all entries
- Recipe datagen via `UniversalProcessorRecipeBuilder` (fluent API, item/fluid I/O, process time, energy cost)
- Incomplete recipes filtered from displays

### Networking
- Network sync for GUI fluid/item state

### Datagen
- Providers: block states, item models, loot tables, recipes, tags (block/item/fluid), language files
- Dynamic JEI category datagen
