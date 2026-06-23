## ADDED Requirements

### Requirement: FleshCasketItem 物品类

SHALL extend `Item` and implement `ICurioItem`.
SHALL store internal item handler using capability pattern (like `FocusBagItemCapability`).
SHALL support tier NBT field: 1 (basic, 1 slot), 2 (advanced, 3 slots), 3 (epic, 5 slots).
SHALL support trigger type NBT field: A, B, or C.
SHALL register in common/init/ModItems.java with three variant IDs: `flesh_casket_a`, `flesh_casket_b`, `flesh_casket_c`.
SHALL occupy Curios BACK slot.
SHALL implement `canEquipFromUse(SlotContext, ItemStack) → true`.

#### Scenario: Basic tier has 1 slot
- **WHEN** player obtains basic FleshCasket (tier=1)
- **THEN** internal inventory has exactly 1 slot for foci storage

#### Scenario: Advanced tier has 3 slots
- **WHEN** player obtains advanced FleshCasket (tier=2)
- **THEN** internal inventory has exactly 3 slots for foci storage

#### Scenario: Epic tier has 5 slots
- **WHEN** player obtains epic FleshCasket (tier=3)
- **THEN** internal inventory has exactly 5 slots for foci storage

#### Scenario: Only accepts IFocus items
- **WHEN** player attempts to insert a non-IFocus item into FleshCasket
- **THEN** the insert SHALL be rejected

#### Scenario: Can equip from use
- **WHEN** player right-clicks with FleshCasket in hand
- **THEN** it SHALL equip to BACK slot

### Requirement: 三个变体注册

Three variants SHALL be registered in ModItems:
- `flesh_casket_a`: TriggerType.A (反击型)
- `flesh_casket_b`: TriggerType.B (半血型)
- `flesh_casket_c`: TriggerType.C (阈值型, default 20%)

Each SHALL share the same `FleshCasketItem` class with different constructor parameter.

#### Scenario: Three registry entries exist
- **WHEN** checking ModItems registration
- **THEN** `FLESH_CASKET_A`, `FLESH_CASKET_B`, `FLESH_CASKET_C` SHALL exist with correct variant assigned

### Requirement: Tier upgrade preserves stored foci

When upgrading tier (via recipe), all stored foci SHALL be preserved.

#### Scenario: Upgrade keeps items
- **WHEN** basic FleshCasket with 1 focus is upgraded to advanced
- **THEN** the focus SHALL remain in slot 0, and slots 1-2 SHALL be empty
