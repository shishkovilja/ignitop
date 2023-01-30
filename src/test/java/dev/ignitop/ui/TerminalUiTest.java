package dev.ignitop.ui;

import java.util.List;
import dev.ignitop.ui.component.impl.Label;
import dev.ignitop.ui.component.impl.Table;
import dev.ignitop.ui.updater.ScreenUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
class TerminalUiTest {
    /** Wide table width. */
    public static final int WIDE_TABLE_WIDTH = 40;

    /** Narrow table width. */
    public static final int NARROW_TABLE_WIDTH = 20;

    /** Label width. */
    public static final int LABEL_WIDTH = 10;

    /** Mock terminal. */
    private Terminal mockTerminal;

    /** Mock sreen updater. */
    private ScreenUpdater mockSreenUpdater;

    /**
     *
     */
    @BeforeEach
    public void setUp() {
        mockTerminal = mock(Terminal.class);

        mockSreenUpdater = mock(ScreenUpdater.class);
    }

    /**
     *
     */
    @Test
    public void refresh_withExpanding_toWidestTableWidth() {
        checkWithTerminalWidth(WIDE_TABLE_WIDTH + 20, WIDE_TABLE_WIDTH);
    }

    /**
     *
     */
    @Test
    public void refresh_withShrinking_ofWidestTable() {
        checkWithTerminalWidth(WIDE_TABLE_WIDTH - 2, WIDE_TABLE_WIDTH - 2);
    }

    /**
     *
     */
    @Test
    public void refresh_withShrinking_ofAllComponents() {
        checkWithTerminalWidth(LABEL_WIDTH - 2, LABEL_WIDTH - 2);
    }

    /**
     * @param terminalWidth Terminal width.
     * @param expectedWidth Expected width user to render components.
     */
    private void checkWithTerminalWidth(int terminalWidth, int expectedWidth) {
        Table wideTable = mock(Table.class);
        when(wideTable.contentWidth()).thenReturn(WIDE_TABLE_WIDTH);

        Table narrowTable = mock(Table.class);
        when(narrowTable.contentWidth()).thenReturn(NARROW_TABLE_WIDTH);

        Label lbl = mock(Label.class);
        when(lbl.contentWidth()).thenReturn(LABEL_WIDTH);

        TerminalUi ui = new TerminalUi(mockTerminal);
        ui.updater(mockSreenUpdater);
        when(mockSreenUpdater.components()).thenReturn(List.of(wideTable, narrowTable, lbl));

        when(mockTerminal.width()).thenReturn(terminalWidth);

        ui.refresh();

        InOrder inOrder = inOrder(wideTable, narrowTable, lbl);

        inOrder.verify(wideTable).render(eq(expectedWidth), any());
        inOrder.verify(narrowTable).render(eq(expectedWidth), any());
        inOrder.verify(lbl).render(eq(expectedWidth), any());
        inOrder.verifyNoMoreInteractions();
    }
}
