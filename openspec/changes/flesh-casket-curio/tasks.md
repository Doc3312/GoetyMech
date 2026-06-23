## 1. Setup & Registration

- [x] 1.1 Register Curios BACK slot in CuriosIntegration.java
- [x] 1.2 Add ModConfig entries: FLESH_CASKET_THRESHOLD, cooldowns, etc.
- [x] 1.3 Register three FleshCasket variants in ModItems.java (flesh_casket_a/b/c)
- [x] 1.4 Register AutoCastDummyWand hidden item in ModItems.java
- [x] 1.5 Add creative tab entries and item model JSONs
- [x] 1.6 Add en_us.json localization entries

## 2. Core Item Implementation

- [x] 2.1 Create TargetingType enum
- [x] 2.2 Create TriggerType enum (A/B/C)
- [x] 2.3 Create FleshCasketItem class (extends Item implements ICurioItem)
- [x] 2.4 Implement internal container capability (slot count based on tier NBT)
- [x] 2.5 Implement tier NBT getter/setter
- [x] 2.6 Implement canEquipFromUse, curioTick, validForSlot checks

## 3. AutoCastDummyWand

- [x] 3.1 Create AutoCastDummyWand class implementing IWand with all SpellTypes

## 4. Targeting Registry

- [x] 4.1 Create TargetingRegistry with Map<Item, TargetingType> initialization
- [x] 4.2 Populate with Goety's known foci (SELF, RETALIATORY, SUMMON, UTILITY)
- [x] 4.3 Add fallback logic for unregistered foci

## 5. Independent Cooldown System

- [x] 5.1 Implement NBT cooldown read/write in FleshCasketItem
- [x] 5.2 Implement PlayerTickEvent handler for cooldown tick-down
- [x] 5.3 Implement cooldown check method (isOnCooldown, getRemainingCooldown)

## 6. Auto-Cast Event System

- [x] 6.1 Create FleshCasketEvents (event handler class)
- [x] 6.2 Implement LivingHurtEvent handler for Type A (retaliatory)
- [x] 6.3 Implement health threshold check for Type B (50%) and Type C (configurable)
- [x] 6.4 Implement minimum cast interval (5 tick guard)
- [x] 6.5 Implement lich check for tier 2/3
- [x] 6.6 Implement auto-cast execution: iterate slots → find available → target → cast
- [x] 6.7 Implement halved soul cost logic
- [x] 6.8 Pass EMPTY staff for tier 1, dummy wand for tier 2/3
- [x] 6.9 Skip spells with castDuration > 0 (instant conversion)

## 7. Cursed Infuser Conversion

- [x] 7.1 Add Cursed Infuser recipes for A→B→C cyclic conversion
- [ ] 7.2 ⚠️ NBT preservation during conversion — standard cursed infuser does not auto-copy NBT from input to output. Need additional handling (custom recipe serializer or event listener) to preserve tier + foci. Workaround: recipes exist, but tier resets to 1 and foci are lost on conversion.

## 8. Item Assets

- [x] 8.1 Create item model JSONs for flesh_casket_a/b/c
- [ ] 8.2 ⚠️ Create item textures (PNG) — model JSONs reference textures that don't exist yet. Create PNG files at `assets/goetypunk/textures/item/flesh_casket_a.png`, `flesh_casket_b.png`, `flesh_casket_c.png`.
