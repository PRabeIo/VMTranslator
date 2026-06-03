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
            default -> CommandType.C_ARITHMETIC;
        };
    }

    public String arg1() {
        requireCurrent();
        if (commandType() == CommandType.C_ARITHMETIC) {
            return current[0];
        }
        return current[1];
    }

    public int arg2() {
        requireCurrent();
        if (commandType() != CommandType.C_PUSH && commandType() != CommandType.C_POP) {
            throw new IllegalStateException("arg2() só se aplica a push/pop.");
        }
        return Integer.parseInt(current[2]);
    }

    private void requireCurrent() {
        if (current == null) {
            throw new IllegalStateException("Chame advance() antes de ler o comando atual.");
        }
    }
}
