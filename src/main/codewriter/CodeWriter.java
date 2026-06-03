package codewriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Traduz comandos VM (Parte 1) para Assembly Hack.
 */
public class CodeWriter {

    private static final String[] SEGMENT_POINTERS = {"THIS", "THAT"};

    private final BufferedWriter writer;
    private final String staticPrefix;
    private int labelCounter = 0;

    public CodeWriter(String outputFilename) throws IOException {
        writer = Files.newBufferedWriter(Path.of(outputFilename));
        staticPrefix = extractBaseName(outputFilename);
    }

    private static String extractBaseName(String path) {
        String name = Path.of(path).getFileName().toString();
        if (name.endsWith(".asm")) {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    public void writeArithmetic(String command) throws IOException {
        switch (command) {
            case "add" -> writeBinary("M=M+D");
            case "sub" -> writeBinary("M=M-D");
            case "neg" -> {
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=-M");
            }
            case "eq", "gt", "lt" -> writeComparison(command);
            case "and" -> writeBinary("M=M&D");
            case "or" -> writeBinary("M=M|D");
            case "not" -> {
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=!M");
            }
            default -> throw new IllegalArgumentException("Comando aritmético/lógico desconhecido: " + command);
        }
    }

    private void writeBinary(String compute) throws IOException {
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("A=A-1");
        writeLine(compute);
    }

    private void writeComparison(String jumpCommand) throws IOException {
        String trueLabel = "TRUE" + labelCounter;
        String endLabel = "END" + labelCounter;
        labelCounter++;

        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("A=A-1");
        writeLine("D=M-D");
        writeLine("@" + trueLabel);
        writeLine("D;J" + jumpCommand.toUpperCase());
        writeLine("@SP");
        writeLine("A=M-1");
        writeLine("M=0");
        writeLine("@" + endLabel);
        writeLine("0;JMP");
        writeLine("(" + trueLabel + ")");
        writeLine("@SP");
        writeLine("A=M-1");
        writeLine("M=-1");
        writeLine("(" + endLabel + ")");
    }

    public void writePush(String segment, int index) throws IOException {
        switch (segment) {
            case "constant" -> {
                writeLine("@" + index);
                writeLine("D=A");
            }
            case "local", "argument", "this", "that" -> {
                writeLine("@" + index);
                writeLine("D=A");
                writeLine("@" + segmentPointer(segment));
                writeLine("A=M+D");
                writeLine("D=M");
            }
            case "temp" -> {
                writeLine("@" + (5 + index));
                writeLine("D=M");
            }
            case "pointer" -> {
                writeLine("@" + SEGMENT_POINTERS[index]);
                writeLine("D=M");
            }
            case "static" -> {
                writeLine("@" + staticPrefix + "." + index);
                writeLine("D=M");
            }
            default -> throw new IllegalArgumentException("Segmento push desconhecido: " + segment);
        }
        pushD();
    }

    public void writePop(String segment, int index) throws IOException {
        if ("constant".equals(segment)) {
            throw new IllegalArgumentException("pop constant não é permitido.");
        }

        if ("static".equals(segment)) {
            writePopToAddress(staticPrefix + "." + index);
            return;
        }

        if ("pointer".equals(segment)) {
            writePopToAddress(SEGMENT_POINTERS[index]);
            return;
        }

        writeLine("@" + index);
        writeLine("D=A");
        if ("temp".equals(segment)) {
            writeLine("@5");
            writeLine("D=D+A");
        } else {
            writeLine("@" + segmentPointer(segment));
            writeLine("D=M+D");
        }
        writeLine("@R13");
        writeLine("M=D");
        writePopFromStackToR13();
    }

    private void writePopToAddress(String symbol) throws IOException {
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@" + symbol);
        writeLine("M=D");
    }

    private void writePopFromStackToR13() throws IOException {
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@R13");
        writeLine("A=M");
        writeLine("M=D");
    }

    private void pushD() throws IOException {
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("M=M+1");
    }

    private static String segmentPointer(String segment) {
        return switch (segment) {
            case "local" -> "LCL";
            case "argument" -> "ARG";
            case "this" -> "THIS";
            case "that" -> "THAT";
            default -> throw new IllegalArgumentException("Segmento pop desconhecido: " + segment);
        };
    }

    private void writeLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    public void close() throws IOException {
        writer.close();
    }
}
