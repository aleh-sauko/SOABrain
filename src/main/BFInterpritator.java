package main;

import java.awt.BorderLayout;
import java.io.BufferedReader;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class BFInterpritator {

	protected JTable memoryTable;
	protected JScrollPane memoryScroll;
	protected MemoryTableModel model;
	
	// The memory thats available for this brainfuck program.
	protected byte[] data = null;
	// The data pointer that points to the current index in the memory array.
	protected int dataPointer = 0;
	//The character pointer that points to the current index of the character array of value of its file or string.
	protected int charPointer = 0;
	//The fileReader allows use to read from a file if one is specified.
	protected BufferedReader fileReader;
	// IDEFrame
	protected IDEFrame ide;
	// Input Data
	protected StringBuffer inputData;
	// Input pointer of Data
	protected int posInput;
	// Execution thread
	protected Thread exec;
	
	//Debug
	protected boolean debug;
	//Debug position
	protected int debugPos;
	//Debug break cycle
	protected boolean breakCycle;
	
	boolean close;
	
	
	//The Token class contains tokens in brainfuck.
	protected static class Token {
		
		public final static char NEXT = '>';
		public final static char PREVIOUS = '<';
		public final static char PLUS = '+';
		public final static char MINUS = '-';
		public final static char OUTPUT = '.';
		public final static char INPUT = ',';
		public final static char BRACKET_LEFT = '[';
		public final static char BRACKET_RIGHT = ']';
	}

	/**
	 * Constructs a new BFInterpritator instance.
	 */
	public BFInterpritator(int cells, IDEFrame ide, boolean debug) {
		this.ide = ide;
		this.debug = debug;
		initate(cells, debug); 
	}

	/**
	 * Initiate this instance.
	 */
	protected void initate(int cells, boolean debug) {
		this.debug = debug;
		
		if (data == null) data = new byte[cells];
		else
			for (int i=0; i<data.length; i++)
				data[i] = 0;
		
		if (exec != null) exec.stop();
		
		dataPointer = 0;
		charPointer = 0;
		posInput = 0;
		breakCycle = false;
		close = false;
		ide.codeText.requestFocus();
		ide.codeText.setCaretPosition(0);
		inputData = new StringBuffer();
		
		if (debug) {
			ide.runInto.setEnabled(true);
			ide.runOut.setEnabled(true);
		}
		
		if (model == null) model = new MemoryTableModel(this, cells);
		else model.init(this, cells);
		
		boolean first = false;
		if (memoryTable == null) { 
			memoryTable = new ToolTipTable(model);
			first = true;
		} else memoryTable.setModel(model);
		
		
		if (first) {
			memoryTable.setCellSelectionEnabled(true);
			//memoryTable.setFont(new Font("Consolas", Font.PLAIN, 14));
			memoryScroll = new JScrollPane(memoryTable);
			ide.dataPanel.add(memoryScroll, BorderLayout.CENTER);
			memoryTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			ide.setExtendedState(IDEFrame.NORMAL);
			ide.setExtendedState(IDEFrame.MAXIMIZED_BOTH);
		}	
	}


	/**
	 * Interpret code
	 */
	public void interpret() throws Exception {
		
		if (!debug) debugPos = ide.codeText.getText().length();
		else debugPos = 0;
		
		exec = new Thread(new 
				Runnable() {
					@Override
					public void run() {
						interpret(ide.codeText.getText());
					}
				});
		exec.start();
	}


	/**
	 * Interprets the given string.
	 */
	public void interpret(String str) {
		while (charPointer < str.length()){
			if (charPointer < debugPos) {
				synchronized(new Object()) {
					ide.codeText.requestFocus();
					interpret(str.charAt(charPointer), str.toCharArray());
				}
				if (close) break;
				charPointer++;
				ide.codeText.setCaretPosition(charPointer);
			}
		}
		if (close)  JOptionPane.showMessageDialog(ide, "Execution interrapted!");
		else JOptionPane.showMessageDialog(ide, "Successfull completed!");
		ide.codeText.setEditable(true);
		ide.runOut.setEnabled(false);
		ide.runInto.setEnabled(false);
	}
	
	/**
	 * Interprets the given char
	 * @param c the char to interpret.
	 * @throws Exception
	 */
	public void interpret(char c, char[] chars) {
		
		if (c == ' ' || c == '\n' || c == '\t')
			return;
		
		switch (c) {
		case Token.NEXT:
			// increment the data pointer (to point to the next cell to the right).
			if ((dataPointer + 1) > data.length) {
				dataPointer = 0;
			}
			dataPointer++;
			break;
		case Token.PREVIOUS:
			// decrement the data pointer (to point to the next cell to the left).
			if ((dataPointer - 1) < 0) {
				dataPointer = data.length - 1;
			}
			dataPointer--;
			break;
		case Token.PLUS:

			// increment (increase by one) the byte at the data pointer.
			if ((((int) data[dataPointer]) + 1) > Integer.MAX_VALUE) {
				JOptionPane.showMessageDialog(ide, "Error on line byte value at data pointer ("
						+ dataPointer + ") " + " on postion " + charPointer
						+ " higher than byte max value.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			data[dataPointer]++;
			memoryTable.setValueAt((data[dataPointer]+256)%256, dataPointer/16, dataPointer%16+1);
			break;
		case Token.MINUS:
			data[dataPointer]--;
			memoryTable.setValueAt((data[dataPointer]+256)%256, dataPointer/16, dataPointer%16+1);
			break;
		case Token.OUTPUT:
			// Output the byte at the current index in a character.
			ide.output.setText(ide.output.getText() + (char) data[dataPointer]);
			break;
		case Token.INPUT:
			// accept one byte of input, storing its value in the byte at the data pointer.
			String input = null;
			while (posInput >= inputData.length() && !close) {
				input = JOptionPane.showInputDialog(ide, "Write input data: ", "Input Data", JOptionPane.INFORMATION_MESSAGE);
				if (input == null) {
					close = true;
					break;
				}
				inputData.append(input);
			}
			
			if (posInput >= inputData.length() && input == null) {
				close = true;
				break;
			}
			
			data[dataPointer] = (byte) inputData.charAt(posInput++);
			memoryTable.setValueAt(data[dataPointer], dataPointer/16, dataPointer%16+1);
			ide.codeText.setCaretPosition(ide.codeText.getCaretPosition()+1);
			break;
		case Token.BRACKET_LEFT:
			if (data[dataPointer] == 0) {
				int i = 1;
				while (i > 0) {
					char c2 = chars[++charPointer];
					if (c2 == Token.BRACKET_LEFT)
						i++;
					else if (c2 == Token.BRACKET_RIGHT)
						i--;
				}
			} 
			if (!breakCycle && debug) debugPos = charPointer + 1;
			break;
		case Token.BRACKET_RIGHT:
			int i = 1;
			while (i > 0) {
				char c2 = chars[--charPointer];
				if (c2 == Token.BRACKET_LEFT)
					i--;
				else if (c2 == Token.BRACKET_RIGHT)
					i++;
			}
			charPointer--;
			if (!breakCycle && debug) {
				debugPos = charPointer + 1;
			}
			break;
		}
	}
}