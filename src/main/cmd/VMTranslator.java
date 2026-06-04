package cmd;

import codewriter.CodeWriter;
import parser.CommandType;
import parser.Parser;

/**
 * Ponto de entrada: traduz um arquivo .vm para .asm (Parte 1).
 *
 * Uso: java cmd.VMTranslator caminho/Programa.vm
 */
public class VMTranslator {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java cmd.VMTranslator <arquivo.vm>");
            System.exit(1);
        }

        String input = args[0];
        if (!input.endsWith(".vm")) {
            System.err.println("Entrada deve ser um arquivo .vm");
            System.exit(1);
        }

        String output = input.substring(0, input.length() - 3) + ".asm";

        try {
            translate(input, output);
            System.out.println("Traduzido: " + output);
        } catch (Exception e) {
            System.err.println("Erro ao traduzir: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void translate(String input, String output) throws Exception {
        Parser parser = new Parser(input);
        CodeWriter writer = new CodeWriter(output);

        while (parser.hasMoreCommands()) {
            parser.advance();
            switch (parser.commandType()) {
                case C_ARITHMETIC -> writer.writeArithmetic(parser.arg1());
                case C_PUSH -> writer.writePush(parser.arg1(), parser.arg2());
                case C_POP -> writer.writePop(parser.arg1(), parser.arg2());
            }
        }

        writer.close();
    }
}
