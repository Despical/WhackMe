package dev.despical.whackme.menu;

import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.pane.PaginatedPane;

/**
 * @author Despical
 * <p>
 * Created at 2.06.2026
 */
public interface Page {

    void beforeOpening(Gui gui);

    void injectItems(PaginatedPane paginatedPane);
}
