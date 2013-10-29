package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class IDEFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	static private final String defTitle = "SOABrain IDE for BrainFuck";
	
	protected BFInterpritator brainfuck;
	
	protected JScrollPane codeEditor;
	protected JTextPane codeText;
	protected JPanel dataPanel;
	protected JPanel outPanel;
	protected JTextArea output;
	
	private final StyledDocument doc;
	/* Define keyword attribute */
	/* Bracket '[' && ']' attributes */
	final SimpleAttributeSet keyBracket = new SimpleAttributeSet();
	/* Plus '+' attributes */
	final SimpleAttributeSet keyPlus = new SimpleAttributeSet();
	/* Minus '-' attributes */
	final SimpleAttributeSet keyMinus = new SimpleAttributeSet();
	/* Minus '>' attributes */
	final SimpleAttributeSet keyBigger = new SimpleAttributeSet();
	/* Minus '<' attributes */
	final SimpleAttributeSet keyLesser = new SimpleAttributeSet();
	/* Minus '.' attributes */
	final SimpleAttributeSet keyPoint = new SimpleAttributeSet();
	/* Minus '>' attributes */
	final SimpleAttributeSet keyComma = new SimpleAttributeSet();
	/* Default attributes */
	final SimpleAttributeSet keyDefault = new SimpleAttributeSet();
	
	JMenuItem saveFile;
	JMenuItem runInto;
	JMenuItem runOut;
	private File file = null;	
	
	public IDEFrame() {
		setTitle(defTitle);
		setLayout(new BorderLayout());
		setSize(800, 600);
		
		
		setJMenuBar(createMenuBar());
		
		codeText = new JTextPane();
		codeText.setFont(new Font("Arial", Font.BOLD, 16));
		doc = codeText.getStyledDocument();
		StyleConstants.setForeground(keyBracket, Color.RED);
		StyleConstants.setForeground(keyPlus, Color.BLUE);
		StyleConstants.setForeground(keyMinus, Color.CYAN);
		StyleConstants.setForeground(keyBigger, Color.GREEN);
		StyleConstants.setForeground(keyLesser, Color.ORANGE);
		StyleConstants.setForeground(keyPoint, Color.MAGENTA);
		StyleConstants.setForeground(keyComma, Color.PINK);
		StyleConstants.setForeground(keyDefault, Color.BLACK);
		
		codeText.addKeyListener(new
				KeyListener(){
					public void keyPressed(KeyEvent key) { }
					public void keyReleased(KeyEvent key) {
						//if (brainfuck != null && brainfuck.debug) return;
						codeText.setEditable(true);
						processingChar(key.getKeyChar());
						switch (key.getKeyChar()) {
						case '[':
							try {
								doc.insertString(codeText.getCaret().getDot(), "]", keyBracket);
							} catch (BadLocationException e) {
								output.setText(e.toString());
							}
							codeText.setCaretPosition(codeText.getCaret().getDot()-1);
							break;
						}
					}
					public void keyTyped(KeyEvent key) {
						codeText.setEditable(false);
					}
		});
		codeEditor = new JScrollPane(codeText);
		add(codeEditor, BorderLayout.CENTER);
		
		dataPanel = new JPanel();
		
		output = new JTextArea(3, 55);
		output.setEditable(false);
		output.setPreferredSize(getPreferredSize());

		outPanel = new JPanel();
		outPanel.add(new Label("Result : "));
		outPanel.add(new JScrollPane(output));		
		
		dataPanel.setLayout(new BorderLayout());
		dataPanel.add(outPanel, BorderLayout.SOUTH);
		
		add(dataPanel, BorderLayout.SOUTH);
	}
	
	
	/* Create a MenuBar */
	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		
		JMenuItem newFile = new JMenuItem("New File... ");
		newFile.addActionListener(new
			      ActionListener()
			      {
			         public void actionPerformed(ActionEvent event) { newFile(); }
			      });
		
		JMenuItem openFile = new JMenuItem("Open File... ");
		openFile.addActionListener(new
			      ActionListener()
			      {
			         public void actionPerformed(ActionEvent event) { openFile(); }
			      });
		saveFile = new JMenuItem("Save File");
		saveFile.setEnabled(false);
		saveFile.addActionListener(new
			      ActionListener()
			      {
			         public void actionPerformed(ActionEvent event) { saveFile(); }
			      });
		JMenuItem saveFileAs = new JMenuItem("Save File As... ");
		saveFileAs.addActionListener(new
			      ActionListener()
			      {
			         public void actionPerformed(ActionEvent event) { saveFileAs(); }
			      });
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new
			      ActionListener()
			      {
			         public void actionPerformed(ActionEvent event) { System.exit(0); }
			      });
		
		fileMenu.add(newFile);
		fileMenu.add(openFile);
		fileMenu.add(saveFile);
		fileMenu.add(saveFileAs);
		fileMenu.add(exit);
		
		
		JMenu runMenu = new JMenu("Run");
		JMenuItem run = new JMenuItem("Run");
		run.addActionListener(new
				ActionListener()
				{
					public void actionPerformed(ActionEvent event) { compileAndRun(false); }
				});
		JMenuItem debug = new JMenuItem("Debug");
		debug.addActionListener(new
				ActionListener()
				{
					public void actionPerformed(ActionEvent event) { compileAndRun(true); }
				});
		runInto = new JMenuItem("Run into...");
		runInto.addActionListener(new
				ActionListener()
				{
					public void actionPerformed(ActionEvent event) { 
						brainfuck.debugPos++; 
						brainfuck.breakCycle = false;
					}
				});
		runInto.setEnabled(false);
		runOut = new JMenuItem("Run out...");
		runOut.addActionListener(new
				ActionListener()
				{
					public void actionPerformed(ActionEvent event) {
						String content = codeText.getText();
						int pos = codeText.getCaretPosition();
						int count = 1;
						while (pos < content.length() && count > 0) {
							if (content.charAt(pos) == '[') count++;
							if (content.charAt(pos) == ']') count--;
							pos++;
						}
						brainfuck.debugPos = pos;
						brainfuck.breakCycle = true;
					}
				});
		runOut.setEnabled(false);
		
		runMenu.add(run);
		runMenu.add(debug);
		runMenu.add(runInto);
		runMenu.add(runOut);
		
		JMenu javaMenu = new JMenu("Java");
		JMenuItem decompileIntoJava = new JMenuItem("Export into *.jar file");
		final BFDecompilator bfDecompile =  new BFDecompilator(this);
		decompileIntoJava.addActionListener(new
				ActionListener()
				{
					public void actionPerformed(ActionEvent event) { 
						bfDecompile.decompile();
					}
				});
		
		
		javaMenu.add(decompileIntoJava);
		
		JMenu aboutMenu = new JMenu("About");
		JMenuItem about = new JMenuItem("About");
		aboutMenu.add(about);
		
		/* HotKeys */
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveFileAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_MASK));
		debug.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.CTRL_MASK));
		runInto.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, InputEvent.CTRL_MASK));
		runOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.CTRL_MASK));
		decompileIntoJava.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, InputEvent.ALT_MASK));
		
		menuBar.add(fileMenu);
		menuBar.add(runMenu);
		menuBar.add(javaMenu);
		menuBar.add(aboutMenu);
		
		return menuBar;
	}
	
	/* New File*/
	public void newFile() {
		if (file != null) saveFile();
		else if (!codeText.getText().equals("")) saveFileAs();
		file = null;
		codeText.setText("");
		saveFile.setEnabled(false);
	}
	
	/* Open File */
	public void openFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));

		chooser.setFileFilter(new
				javax.swing.filechooser.FileFilter() {  
					public boolean accept(File f) {  
						return f.isDirectory() || f.getName().toLowerCase().endsWith(".bf");
					}
					public String getDescription() { return "BrainFuck files"; }
		      	});
		
		
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
		file = chooser.getSelectedFile();
		saveFile.setEnabled(true);
		this.setTitle(defTitle + " : " + file.getName());
		
		try {
			BufferedReader reader = new BufferedReader( new FileReader(file));
			
			codeText.setText("");
			
			String line;
			while ((line = reader.readLine()) != null) {
				char[] chars = line.toCharArray();
				for (char ch : chars) {
					processingChar(ch);
				}
			}
			codeText.setEditable(true);
			codeText.setFocusable(true);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/* Save File */
	public void saveFile() {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(file));
			
			writer.println(codeText.getText());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Save File As */
	public void saveFileAs() {
		JFileChooser fileChooser = new JFileChooser();
		
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			
			this.setTitle(defTitle + " : " + file.getName());
			saveFile();
		}
	}

	/* Compile and Ran */
	public void compileAndRun(boolean debugMode) {
		//if (file != null) saveFile();
		
		output.setText("");
		
		if (brainfuck == null) brainfuck = new BFInterpritator(30000, this, debugMode);
		else brainfuck.initate(30000, debugMode);
		
		try {
			brainfuck.interpret();
		} catch (Exception e) {
			//output.setText(e.toString());
		}
		
	}
	

	/* Processing char */
	public void processingChar(char key) {
		try {
			switch (key) {
			case '[':
				doc.insertString(codeText.getCaret().getDot(), "[", keyBracket);
				break;
			case '+':
				doc.insertString(codeText.getCaret().getDot(), "+", keyPlus);
				break;
			case '-':
				doc.insertString(codeText.getCaret().getDot(), "-", keyMinus);
				break;
			case '>':
				doc.insertString(codeText.getCaret().getDot(), ">", keyBigger);
				break;
			case '<':
				doc.insertString(codeText.getCaret().getDot(), "<", keyLesser);
				break;
			case ',': 
				doc.insertString(codeText.getCaret().getDot(), ",", keyComma);
				break;
			case '.':
				doc.insertString(codeText.getCaret().getDot(), ".", keyPoint);
				break;
			case ']':
				doc.insertString(codeText.getCaret().getDot(), "]", keyBracket);
				break;
			default:
				if (!(key > 'a' && key < 'z') &&
					!(key > 'A' && key < 'Z')	&&
					!(key > '0' && key < '9'))
					break;
				String s = "x";
				s = s.replace('x', key);
				doc.insertString(codeText.getCaret().getDot(), s, keyDefault);
				break;	
			}
		} catch (BadLocationException e) {
			//output.setText(e.toString());
		}
	}
}