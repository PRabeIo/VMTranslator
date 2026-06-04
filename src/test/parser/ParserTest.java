package parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void ignoraComentariosEVazios(@TempDir Path dir) throws Exception {
        Path vm = dir.resolve("Test.vm");
        Files.writeString(vm, """
                // comentário
                push constant 5   // inline
                pop local 0

                add
                """);

        Parser parser = new Parser(vm.toString());
        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(CommandType.C_PUSH, parser.commandType());
        assertEquals("constant", parser.arg1());
        assertEquals(5, parser.arg2());

        parser.advance();
        assertEquals(CommandType.C_POP, parser.commandType());
        assertEquals("local", parser.arg1());
        assertEquals(0, parser.arg2());

        parser.advance();
        assertEquals(CommandType.C_ARITHMETIC, parser.commandType());
        assertEquals("add", parser.arg1());
        assertFalse(parser.hasMoreCommands());
    }
}
