## ADDED Requirements

### Requirement: Document Squad Barracks subsystem in architecture skill
The `goetypunk-architecture` skill SHALL include the point-and-click barracks (点将台) subsystem in its documented package structure, including:
- Three tiered block types (`Tier1SquadBarracksBlock`, `Tier2SquadBarracksBlock`, `Tier3SquadBarracksBlock`)
- `SquadBarracksBlockEntity` and `SquadChunkTickets`
- `SquadBarracksMenu` and `ModMenuTypes`
- `SquadBarracksScreen`
- `SquadFocus` and `SquadSpell`
- `SquadFocusEvents`
- `SquadConfigC2SPacket`

#### Scenario: Skill documents common/menu/ package
- **WHEN** reading the skill's package structure
- **THEN** `common/menu/` is listed with `ModMenuTypes.java` and `SquadBarracksMenu.java`

#### Scenario: Skill documents common/net/ package
- **WHEN** reading the skill's package structure
- **THEN** `common/net/` is listed with `SquadConfigC2SPacket.java`

#### Scenario: Skill documents client/gui/ package
- **WHEN** reading the skill's package structure
- **THEN** `client/gui/` is listed with `SquadBarracksScreen.java`

### Requirement: Document realistic client render package
The skill SHALL reflect the actual renderer location: `client/render/SculkBidingRenderer.java` and `client/render/SculkBidingModel.java`, not the stale `client/entity/SculkSpikeRenderer.java`.

#### Scenario: Skill shows correct renderer paths
- **WHEN** reading the skill's client section
- **THEN** it lists `client/render/SculkBidingRenderer.java` and `client/render/SculkBidingModel.java`
