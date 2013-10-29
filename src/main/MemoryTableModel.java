package main;

import java.util.Formatter;

import javax.swing.table.AbstractTableModel;

public class MemoryTableModel extends AbstractTableModel{

	private static final long serialVersionUID = 1L;
	protected BFInterpritator brainfuck;
	Object[][] memoryCells;
	String[] columnNames = {"Offset", "0", "1", "2", "3", "4", "5", "6", "7", "8",
			"9", "A", "B", "C", "D", "E", "F"};
	
	public MemoryTableModel(BFInterpritator brainfuck, int cells) {
		super();
		init(brainfuck, cells);
	}
	
	public void init(BFInterpritator brainfuck, int cells) {
		this.brainfuck = brainfuck;
		memoryCells = new Object[cells/16][17];
		for (int i=0; i< cells/16; i++) {
			Formatter f = new Formatter();
			f.format("%03X0", i);
			memoryCells[i][0] = f;
			for (int j=0; j<16; j++) {
				setValueAt(brainfuck.data[i*16+j], i, j+1);
				fireTableCellUpdated(i, j+1);
			}
		}
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return memoryCells.length;
	}
	
	public String getColumnName(int col){
        return columnNames[col];
    }

	@Override
	public Object getValueAt(int line, int column) {
		return memoryCells[line][column];
	}
	@Override
	public void setValueAt(Object cell, int row, int column) {
		memoryCells[row][column] = (byte) Integer.parseInt(cell.toString());
		brainfuck.data[row*16+column-1] = (byte) memoryCells[row][column];
		fireTableCellUpdated(row, column);
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
	    if (columnIndex == 0)
	    	return false;
		return true ; 
	}
}
