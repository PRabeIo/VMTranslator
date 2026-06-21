package parser;

/**
 * Tipos de comando reconhecidos pelo parser VM (Partes 1 e 2).
 */
public enum CommandType {
    C_ARITHMETIC,
    C_PUSH,
    C_POP,
    C_LABEL,
    C_GOTO,
    C_IF,
    C_FUNCTION,
    C_CALL,
    C_RETURN
}
