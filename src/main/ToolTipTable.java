package main;

import java.awt.Component;
import java.util.Formatter;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class ToolTipTable extends JTable {
	
	private static final long serialVersionUID = 1L;

	public ToolTipTable(TableModel model) {
		super(model);
	}
	
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (c instanceof JComponent && column != 0) {
            JComponent jc = (JComponent) c;
            Formatter f = new Formatter();
            f.format("dec='%d', hex='%X', '%c'", row*16+column-1, row*16+column-1, ((byte)getValueAt(row, column)+256)%256);
            jc.setToolTipText(f.toString());
            f.close();
        }
        return c;
    }
}
