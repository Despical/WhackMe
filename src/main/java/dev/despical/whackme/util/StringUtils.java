package dev.despical.whackme.util;

import dev.despical.commons.miscellaneous.DefaultFontInfo;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

/**
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
@NoArgsConstructor
public final class StringUtils {

    private static final int CENTER_PX = 165;

    public static void sendCenteredMessage(CommandSender sender, Component component) {
        sender.sendMessage(getCenteredComponent(component));
    }

    private static Component getCenteredComponent(Component component) {
        int messagePxSize = measureComponent(component);
        int spaceWidth = DefaultFontInfo.SPACE.getLength() + 1;
        int paddingSpaces = (CENTER_PX - messagePxSize / 2) / spaceWidth;

        String padding = " ".repeat(Math.max(0, paddingSpaces));
        Component paddingComponent = Component.text(padding);

        return paddingComponent.append(component);
    }

    private static int measureComponent(Component component) {
        int width = 0;

        if (component instanceof TextComponent textComponent) {
            boolean bold = component.style().hasDecoration(TextDecoration.BOLD);
            String content = textComponent.content();

            for (int i = 0; i < content.length(); i++) {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(content.charAt(i));
                width += bold ? dFI.getBoldLength() : dFI.getLength();
                width++;
            }
        }

        for (Component child : component.children()) {
            width += measureComponent(child);
        }

        return width;
    }
}
