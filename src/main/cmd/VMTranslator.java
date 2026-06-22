package cmd;

import codewriter.CodeWriter;
import parser.CommandType;
import parser.Parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Ponto de entrada: traduz arquivo .vm ou diretório com vários .vm para .asm.
 *
 * Uso:
 *   java cmd.VMTranslator caminho/Programa.vm
 *   java cmd.VMTranslator caminho/Diretorio/
 */
public class VMTranslator {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java cmd.VMTranslator <arquivo.vm | diretório>");
            System.exit(1);
        }

        String input = args[0];
        Path inputPath = Path.of(input);

        if (!Files.exists(inputPath)) {
            System.err.println("Entrada não encontrada: " + input);
            System.exit(1);
        }

        try {
            String output = translate(inputPath);
            System.out.println("Traduzido: " + output);
        } catch (Exception e) {
            System.err.println("Erro ao traduzir: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    static String translate(Path inputPath) throws Exception {
        if (Files.isDirectory(inputPath)) {
            return translateDirectory(inputPath);
        }
        if (!inputPath.toString().endsWith(".vm")) {
            throw new IllegalArgumentException("Entrada deve ser um arquivo .vm ou um diretório.");
        }
        return translateSingleFile(inputPath);
    }

    private static String translateDirectory(Path directory) throws Exception {
        String dirName = directory.getFileName().toString();
        Path outputPath = directory.resolve(dirName + ".asm");

        try (Stream<Path> stream = Files.list(directory)) {
            Path[] vmFiles = stream
                    .filter(path -> path.toString().endsWith(".vm"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toArray(Path[]::new);

            if (vmFiles.length == 0) {
                throw new IllegalArgumentException("Nenhum arquivo .vm encontrado em: " + directory);
            }

            CodeWriter writer = new CodeWriter(outputPath.toString());
            writer.writeBootstrap();

            for (Path vmFile : vmFiles) {
                translateFile(vmFile, writer);
            }

            writer.close();
            return outputPath.toString();
        }
    }

    private static String translateSingleFile(Path vmFile) throws Exception {
        String output = vmFile.toString().substring(0, vmFile.toString().length() - 3) + ".asm";
        CodeWriter writer = new CodeWriter(output);
        translateFile(vmFile, writer);
        writer.close();
        return output;
    }

    private static void translateFile(Path vmFile, CodeWriter writer) throws Exception {
        String baseName = vmFile.getFileName().toString();
        baseName = baseName.substring(0, baseName.length() - 3);
        writer.setFileName(baseName);

        Parser parser = new Parser(vmFile.toString());
        while (parser.hasMoreCommands()) {
            parser.advance();
            dispatch(parser, writer);
        }
    }

    private static void dispatch(Parser parser, CodeWriter writer) throws Exception {
        switch (parser.commandType()) {
            case C_ARITHMETIC -> writer.writeArithmetic(parser.arg1());
            case C_PUSH -> writer.writePush(parser.arg1(), parser.arg2());
            case C_POP -> writer.writePop(parser.arg1(), parser.arg2());
            case C_LABEL -> writer.writeLabel(parser.arg1());
            case C_GOTO -> writer.writeGoto(parser.arg1());
            case C_IF -> writer.writeIf(parser.arg1());
            case C_FUNCTION -> writer.writeFunction(parser.arg1(), parser.arg2());
            case C_CALL -> writer.writeCall(parser.arg1(), parser.arg2());
            case C_RETURN -> writer.writeReturn();
        }
    }
}
