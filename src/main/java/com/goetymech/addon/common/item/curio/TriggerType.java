package com.goetymech.addon.common.item.curio;

public enum TriggerType {
    A, B, C;

    public TriggerType next() {
        return switch (this) {
            case A -> B;
            case B -> C;
            case C -> A;
        };
    }

    public static TriggerType fromName(String name) {
        for (TriggerType t : values()) {
            if (t.name().equalsIgnoreCase(name)) return t;
        }
        return A;
    }
}
