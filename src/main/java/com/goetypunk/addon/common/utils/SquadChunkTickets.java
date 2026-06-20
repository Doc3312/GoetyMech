package com.goetypunk.addon.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;

public class SquadChunkTickets {
    public static final TicketType<BlockPos> TICKET_TYPE =
            TicketType.create("goetypunk:squad_barracks", BlockPos::compareTo);
}
