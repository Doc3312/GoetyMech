## ADDED Requirements

### Requirement: A⇔B⇔C cyclic conversion

Cursed Infuser recipe SHALL convert between FleshCasket variants:
- flesh_casket_a → flesh_casket_b
- flesh_casket_b → flesh_casket_c
- flesh_casket_c → flesh_casket_a

Conversion SHALL preserve all NBT data (tier, stored foci, cooldowns).

#### Scenario: Type A to Type B
- **WHEN** player puts basic FleshCasket (Type A, contains 1 focus) into Cursed Infuser
- **THEN** output SHALL be basic FleshCasket (Type B, same focus in slot 0)

### Requirement: Tier preserved during conversion

Conversion SHALL NOT change the tier value.

#### Scenario: Epic stays epic
- **WHEN** player converts epic Type A to Type B
- **THEN** output SHALL be epic Type B with same tier value
