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

    @Test
    void reconheceComandosDaParte2(@TempDir Path dir) throws Exception {
        Path vm = dir.resolve("Flow.vm");
        Files.writeString(vm, """
                label LOOP
                goto LOOP
                if-goto LOOP
                function Sys.main 2
                call Sys.add12 1
                return
                """);

        Parser parser = new Parser(vm.toString());
        String[] expected = {
                "C_LABEL", "C_GOTO", "C_IF", "C_FUNCTION", "C_CALL", "C_RETURN"
        };

        for (String type : expected) {
            assertTrue(parser.hasMoreCommands());
            parser.advance();
            assertEquals(CommandType.valueOf(type), parser.commandType());
        }
        assertFalse(parser.hasMoreCommands());
    }
}
