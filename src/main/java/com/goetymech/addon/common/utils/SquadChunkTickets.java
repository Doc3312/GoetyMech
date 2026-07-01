package com.goetymech.addon.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;

public class SquadChunkTickets {
    public static final TicketType<BlockPos> TICKET_TYPE =
            TicketType.create("goetymech:squad_barracks", BlockPos::compareTo);
}
