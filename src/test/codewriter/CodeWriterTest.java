package codewriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CodeWriterTest {

    @Test
    void pushConstantGeraAssemblyEsperado(@TempDir Path dir) throws Exception {
        Path asm = dir.resolve("Prog.asm");
        CodeWriter cw = new CodeWriter(asm.toString());
        cw.writePush("constant", 42);
        cw.close();

        String out = Files.readString(asm);
        assertTrue(out.contains("@42"));
        assertTrue(out.contains("D=A"));
        assertTrue(out.contains("@SP"));
        assertTrue(out.contains("M=M+1"));
    }

    @Test
    void staticUsaPrefixoDoArquivo(@TempDir Path dir) throws Exception {
        Path asm = dir.resolve("BasicTest.asm");
        CodeWriter cw = new CodeWriter(asm.toString());
        cw.writePush("static", 0);
        cw.close();

        String out = Files.readString(asm);
        assertTrue(out.contains("@BasicTest.0"));
    }

    @Test
    void addGeraOperacaoNaPilha(@TempDir Path dir) throws Exception {
        Path asm = dir.resolve("Prog.asm");
        CodeWriter cw = new CodeWriter(asm.toString());
        cw.writeArithmetic("add");
        cw.close();

        String out = Files.readString(asm);
        assertTrue(out.contains("M=M+D"));
    }

    @Test
    void bootstrapChamaSysInit(@TempDir Path dir) throws Exception {
        Path asm = dir.resolve("Prog.asm");
        CodeWriter cw = new CodeWriter(asm.toString());
        cw.writeBootstrap();
        cw.close();

        String out = Files.readString(asm);
        assertTrue(out.contains("@256"));
        assertTrue(out.contains("@SP"));
        assertTrue(out.contains("M=D"));
        assertTrue(out.contains("@Sys.init"));
        assertTrue(out.contains("0;JMP"));
    }

    @Test
    void labelUsaFuncaoAtual(@TempDir Path dir) throws Exception {
        Path asm = dir.resolve("Prog.asm");
        CodeWriter cw = new CodeWriter(asm.toString());
        cw.writeFunction("Sys.main", 0);
        cw.writeLabel("LOOP");
        cw.close();

        String out = Files.readString(asm);
        assertTrue(out.contains("(Sys.main$LOOP)"));
    }
}
