package com.goetymech.addon.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;

/**
 * 兵团常加载工具。支持跨维度区块加载。
 */
public class SquadChunkTickets {
    public static final TicketType<BlockPos> TICKET_TYPE =
            TicketType.create("goetymech:squad_barracks", BlockPos::compareTo);
}
