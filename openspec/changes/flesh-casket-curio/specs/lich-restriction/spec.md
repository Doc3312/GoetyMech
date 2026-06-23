## ADDED Requirements

### Requirement: Lich-only for tier 2 and 3

Advanced (tier 2) and Epic (tier 3) FleshCasket SHALL check `LichdomHelper.isLich(player)` before auto-cast.
If player is NOT a lich, SHALL NOT auto-cast and SHALL display a warning message.

#### Scenario: Non-lich cannot use advanced
- **WHEN** non-lich player equips advanced FleshCasket and takes damage
- **THEN** SHALL NOT auto-cast
- **THEN** SHALL display "Only liches can harness this power" message

#### Scenario: Lich can use advanced
- **WHEN** lich player equips advanced FleshCasket
- **THEN** SHALL auto-cast normally

### Requirement: Basic usable by all

Basic (tier 1) FleshCasket SHALL work for all players regardless of lich status.
No lich check required.

#### Scenario: Non-lich basic works
- **WHEN** non-lich player equips basic FleshCasket
- **THEN** SHALL auto-cast normally
