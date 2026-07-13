package dev.despical.whackme.menu;

import dev.despical.inventoryframework.Gui;

/**
 * @author Despical
 * <p>
 * Created at 2.06.2026
 */
public interface Menu {

    Gui getGui();

    void open();

    default void close() {
        getGui().close();
    }
}
