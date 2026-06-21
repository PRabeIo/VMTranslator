package parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê um arquivo .vm, remove comentários e expõe comandos tokenizados.
 */
public class Parser {

    private final List<String[]> commands;
    private int index = -1;
    private String[] current;

    public Parser(String filename) throws IOException {
        commands = new ArrayList<>();
        for (String line : Files.readAllLines(Path.of(filename))) {
            String trimmed = stripComment(line).trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            commands.add(trimmed.split("\\s+"));
        }
    }

    private static String stripComment(String line) {
        int slash = line.indexOf("//");
        return slash >= 0 ? line.substring(0, slash) : line;
    }

    public boolean hasMoreCommands() {
        return index + 1 < commands.size();
    }

    public void advance() {
        if (!hasMoreCommands()) {
            throw new IllegalStateException("Não há mais comandos VM.");
        }
        index++;
        current = commands.get(index);
    }

    public CommandType commandType() {
        requireCurrent();
        return switch (current[0]) {
            case "push" -> CommandType.C_PUSH;
            case "pop" -> CommandType.C_POP;
            case "label" -> CommandType.C_LABEL;
            case "goto" -> CommandType.C_GOTO;
            case "if-goto" -> CommandType.C_IF;
            case "function" -> CommandType.C_FUNCTION;
            case "call" -> CommandType.C_CALL;
            case "return" -> CommandType.C_RETURN;
            default -> CommandType.C_ARITHMETIC;
        };
    }

    public String arg1() {
        requireCurrent();
        CommandType type = commandType();
        if (type == CommandType.C_ARITHMETIC || type == CommandType.C_RETURN) {
            return current[0];
        }
        return current[1];
    }

    public int arg2() {
        requireCurrent();
        return switch (commandType()) {
            case C_PUSH, C_POP, C_FUNCTION, C_CALL -> Integer.parseInt(current[2]);
            default -> throw new IllegalStateException("arg2() não se aplica a este comando.");
        };
    }

    private void requireCurrent() {
        if (current == null) {
            throw new IllegalStateException("Chame advance() antes de ler o comando atual.");
        }
    }
}
