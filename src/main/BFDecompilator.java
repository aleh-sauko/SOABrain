package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BFDecompilator {
    static final String TERMINAL_SYMBOLS = "<>+-.,[]";
    static final String TAB = "    ";
    
    static int pointer = 0;
    static int offset = 2;
    static int incrementCount = 0;
    static int decrementCount = 0;

    static Set<Integer> variables = new HashSet<Integer>();
    private static StringBuilder decompiledCode = new StringBuilder("\n");
    
    private IDEFrame ide;
    
    public BFDecompilator(IDEFrame ide) {
    	this.ide = ide;
    }
    
    public void init() {
    	pointer = 0;
        offset = 2;
        incrementCount = 0;
        decrementCount = 0;
        
        variables = new HashSet<Integer>();
        decompiledCode = new StringBuilder("\n");
    }

    static void incrementPointer() {
        if (pointer == 255) pointer = 0;
        else pointer++;
    }

    static void decrementPointer() {
        if (pointer == 0) pointer = 255;
        else pointer--;
    }

    static void append(String line) {
        for (int i = 0; i < offset; i++)
            decompiledCode.append(TAB);
        decompiledCode.append(line);
        decompiledCode.append("\n");
    }

    static void insert(String line) {
        decompiledCode.insert(0, "\n");
        decompiledCode.insert(0, line);
        decompiledCode.insert(0, TAB);
        decompiledCode.insert(0, TAB);
    }

    static void createVariableIfNotExists() {
        if (variables.contains(pointer)) return;

        insert("FuckedByte fb" + pointer + " = new FuckedByte(0);");
        variables.add(pointer);
    }

    static void flushVariableIncrementIfRequired() {
        if (incrementCount == 0) return;

        createVariableIfNotExists();

        append("fb" + pointer + ".inc(" + incrementCount + ");");
        incrementCount = 0;
    }

    static void flushVariableDecrementIfRequired() {
        if (decrementCount == 0) return;

        createVariableIfNotExists();

        append("fb" + pointer + ".dec(" + decrementCount + ");");
        decrementCount = 0;
    }

    static void readValue() {
        append("fb" + pointer + " = FuckedByte.read();");
    }

    static void writeValue() {
        append("fb" + pointer + ".write();");
    }

    static void beginWhileLoop() {
        append("while (fb" + pointer + ".get() != 0) {");
        offset++;
    }

    static void endWhileLoop() {
        offset--;
        append("}");
    }

    public void decompile() {
    	
    	String code = ide.codeText.getText();
    	
    	init();
    	
        for (char c : code.toCharArray()) {
            switch (c) {
            case '<':
                flushVariableIncrementIfRequired();
                flushVariableDecrementIfRequired();

                decrementPointer();
                break;
            case '>':
                flushVariableIncrementIfRequired();
                flushVariableDecrementIfRequired();

                incrementPointer();
                break;
            case '+':
                flushVariableDecrementIfRequired();

                incrementCount++;
                break;
            case '-':
                flushVariableIncrementIfRequired();

                decrementCount++;
                break;
            case ',':
                createVariableIfNotExists();

                readValue();
                break;
            case '.':
                flushVariableIncrementIfRequired();
                flushVariableDecrementIfRequired();

                writeValue();
                break;
            case '[':
                flushVariableIncrementIfRequired();
                flushVariableDecrementIfRequired();

                beginWhileLoop();
                break;
            case ']':
                flushVariableIncrementIfRequired();
                flushVariableDecrementIfRequired();

                endWhileLoop();
                break;
            }
        }
        
 
        CompilerAPI compiler = new CompilerAPI(ide);
        compiler.doCompilation(TEMPLATE.replace("{}", BFDecompilator.decompiledCode));

    }

    static class FuckedException extends RuntimeException {
        FuckedException(String message) {
            super(message);
        }
        FuckedException(Throwable cause) {
            super(cause);
        }
    }

    static String readAndValidate(File file) {
        StringBuilder source = new StringBuilder();

        try {
            int lineNumber = 1;
            String line;
            int nestedLoops = 0;

            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                int symbolNumber = 1;

                for (char c : line.toCharArray()) {
                    if (TERMINAL_SYMBOLS.contains(c + "")) {
                        source.append(c);

                        if (c == '[') nestedLoops++;
                        if (c == ']') nestedLoops--;

     /*                   if (nestedLoops < 0)
                            throw new FuckedException("Unexpected ']' at line " + lineNumber + ", symbol " + symbolNumber);*/
                    }

                    symbolNumber++;
                }

                lineNumber++;
            }

            /*if (nestedLoops != 0)
                throw new FuckedException("Unexpected EOF. No matching ']' found.");*/

        } catch (FileNotFoundException e) {
            throw new FuckedException("File " + file.getName() + " not found");
        } catch (IOException e) {
            throw new FuckedException("Error reading file " + file.getName());
        }

        return source.toString();
    }

    static String TEMPLATE = "class GeneratedCode {\n" +
            "    static class FuckedByte {\n" +
            "        char value;\n" +
            "\n" +
            "        FuckedByte(int value) {\n" +
            "            this.value = (char) value;\n" +
            "        }\n" +
            "\n" +
            "        char get() { return value; }\n" +
            "\n" +
            "        void inc(int i) {\n" +
            "            value += i;\n" +
            "            value %= 256;\n" +
            "        }\n" +
            "        void dec(int i) {\n" +
            "            value -= i;\n" +
            "            value %= 256;\n" +
            "            if (value < 0)\n" +
            "                value += 256;\n" +
            "        }\n" +
            "\n" +
            "        static FuckedByte read() {\n" +
            "            try {\n" +
            "                int b;\n" +
            "                while (((b = System.in.read()) == '\\n') || (b == '\\r'));\n" +
            "                return new FuckedByte(b);\n" +
            "            } catch (Exception e) {\n" +
            "                throw new RuntimeException(\"Error reading from System.in\");\n" +
            "            }\n" +
            "        }\n" +
            "        void write() {\n" +
            "            System.out.print(value);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "{}\n" +
            "    }\n" +
            "}";
}
