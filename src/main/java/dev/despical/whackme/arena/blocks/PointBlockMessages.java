package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.chat.ChatManager;
import net.kyori.adventure.text.Component;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
record PointBlockMessages(Component punchMe, Component dontPunchMe, Component ouch) {

    static PointBlockMessages from(ChatManager chatManager) {
        return new PointBlockMessages(
            chatManager.getMessageComponent("point-blocks.punch-me"),
            chatManager.getMessageComponent("point-blocks.dont-punch-me"),
            chatManager.getMessageComponent("point-blocks.ouch")
        );
    }
}
