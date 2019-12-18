package astro.tool.box.module;

import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class FixedTable implements ChangeListener, PropertyChangeListener {

    private final JTable main;
    private final JTable fixed;
    private final JScrollPane scrollPane;

    public FixedTable(int fixedColumns, JScrollPane scrollPane) {
        this.scrollPane = scrollPane;

        main = ((JTable) scrollPane.getViewport().getView());
        main.setAutoCreateColumnsFromModel(false);
        main.addPropertyChangeListener(this);

        fixed = new JTable();
        fixed.setAutoCreateColumnsFromModel(false);
        fixed.setModel(main.getModel());
        fixed.setSelectionModel(main.getSelectionModel());
        fixed.setFocusable(false);

        for (int i = 0; i < fixedColumns; i++) {
            TableColumnModel columnModel = main.getColumnModel();
            TableColumn column = columnModel.getColumn(0);
            columnModel.removeColumn(column);
            fixed.getColumnModel().addColumn(column);
        }

        fixed.setPreferredScrollableViewportSize(fixed.getPreferredSize());
        scrollPane.setRowHeaderView(fixed);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixed.getTableHeader());
    }

    public JTable getFixedTable() {
        return fixed;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JViewport viewport = (JViewport) e.getSource();
        scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if ("selectionModel".equals(e.getPropertyName())) {
            fixed.setSelectionModel(main.getSelectionModel());
        }
        if ("model".equals(e.getPropertyName())) {
            fixed.setModel(main.getModel());
        }
    }

}
