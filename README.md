# VMTranslator — Parte 1 (Project 07)

Tradutor **VM → Assembly Hack** para o curso nand2tetris. Esta entrega implementa a **Parte 1**: comandos `push`/`pop` (todos os segmentos) e operações aritméticas/lógicas (`add`, `sub`, `neg`, `eq`, `gt`, `lt`, `and`, `or`, `not`).

*Curso:* Engenharia da Computação — CCET — UFMA  
*Professor:* Sergio Souza Costa  
*Atividade:* VMTranslator — Parte 1 (Acesso a Dados, Operações Aritméticas e Lógicas)

## Equipe

| Integrante | Matrícula |
|------------|-----------|
| **Paulo Eduardo Lima Rabelo** | 20260001203 |
| **Italo Francisco Almeida de Oliveira** | 20260001230 |

## Linguagem e ferramentas

| Item | Versão / detalhe |
|------|------------------|
| Linguagem | **Java** 17+ |
| Build | **Maven** 3.x (opcional) ou `javac` |
| Testes unitários | **JUnit 5** |
| Validação funcional | **CPUEmulator** |

**Justificativa da linguagem:** mesma stack do compilador Jack, facilitando reaproveitar conceitos e ferramentas (Maven, IntelliJ, JUnit).

---

## Estrutura do projeto

```bash
VMTranslator/
├── pom.xml
├── out/                           # bytecode compilado (javac / Maven)
├── src/main/
│   ├── cmd/VMTranslator.java      # ponto de entrada
│   ├── parser/Parser.java         # leitura e tokenização de .vm
│   └── codewriter/CodeWriter.java # geração de .asm
├── src/test/                      # testes JUnit
│   ├── parser/ParserTest.java
│   └── codewriter/CodeWriterTest.java
└── projects/07/                   # testes oficiais (Project 07)
    ├── StackArithmetic/SimpleAdd/
    └── MemoryAccess/BasicTest/
```

---

## Passo a passo — compilar, gerar `.asm` e testar

### 1. Compilar o tradutor (terminal)

Abra o PowerShell na pasta do projeto:

```powershell
cd C:\Users\Paulo\Desktop\VMTranslator

$out = "out"
New-Item -ItemType Directory -Force -Path $out
javac -encoding UTF-8 -d $out src\main\parser\*.java src\main\codewriter\*.java src\main\cmd\*.java
```

Se não aparecer erro, os arquivos `.class` foram gerados em `out\`.

> **Maven (opcional):** se tiver `mvn` no PATH, `mvn package` também compila o projeto.

---

### 2. Gerar o arquivo `.asm` (no powershell)

O CPUEmulator executa **`.asm`**, não `.vm`. Antes de testar, traduza cada programa:

**SimpleAdd:**
```powershell
java -cp out cmd.VMTranslator projects\07\StackArithmetic\SimpleAdd\SimpleAdd.vm
```

**BasicTest:**
```powershell
java -cp out cmd.VMTranslator projects\07\MemoryAccess\BasicTest\BasicTest.vm
```

Saída esperada:

```
Traduzido: projects\07\StackArithmetic\SimpleAdd\SimpleAdd.asm
```

Confira se o `.asm` foi criado **na mesma pasta** do `.vm` (ao lado de `SimpleAdd.tst` e `SimpleAdd.cmp`).

---

### 3. Testar no CPUEmulator (interface gráfica)

O pacote nand2tetris deve estar no Desktop. Use **Load Script** — **não** Load Program — para carregar o `.tst`.

#### 3.1 Abrir o emulador

1. Abra a pasta:
   ```
   C:\Users\Paulo\Desktop\nand2tetris\nand2tetris\tools
   ```
2. Dê **duplo clique** em `CPUEmulator.bat`.
3. Aguarde a janela **CPU Emulator** abrir.

#### 3.2 Carregar o script de teste (`.tst`)

1. No menu, clique em **File → Load Script…**  
   *(não use **Load Program** — ele só aceita `.asm` e `.hack` e rejeita `.tst`)*  
2. Navegue até a pasta do teste. Exemplo (SimpleAdd):
   ```
   C:\Users\Paulo\Desktop\VMTranslator\projects\07\StackArithmetic\SimpleAdd
   ```
3. Selecione `SimpleAdd.tst` e abra.  
   Se o arquivo não aparecer, mude o filtro para **All files (*.*)**.
4. O emulador carrega o `SimpleAdd.asm`, roda a simulação e compara com `SimpleAdd.cmp`.

#### 3.3 Verificar o resultado

Na parte **inferior** da janela deve aparecer:

```
Comparison ended successfully
```

Se aparecer diferença entre `.out` e `.cmp`, revise o `CodeWriter` e gere o `.asm` de novo (passo 2).

#### 3.4 Repetir para BasicTest

1. Gere o `BasicTest.asm` no terminal (passo 2).
2. No CPUEmulator: **File → Load Script…**
3. Selecione:
   ```
   C:\Users\Paulo\Desktop\VMTranslator\projects\07\MemoryAccess\BasicTest\BasicTest.tst
   ```
4. Confira novamente **Comparison ended successfully**.

---

### Resumo do fluxo

| Etapa | Onde | O quê |
|-------|------|--------|
| 1 | Terminal | `javac` → compila o tradutor |
| 2 | Terminal | `java -cp out cmd.VMTranslator ...` → gera `.asm` |
| 3 | CPUEmulator (GUI) | **File → Load Script** → `.tst` → compara com `.cmp` |

---

## Status dos testes (Project 07)

| Programa | Caminho | Resultado |
|----------|---------|-----------|
| **SimpleAdd** | `projects/07/StackArithmetic/SimpleAdd/` | Passou no CPUEmulator |
| **BasicTest** | `projects/07/MemoryAccess/BasicTest/` | Passou no CPUEmulator |

Os arquivos `.vm`, `.tst` e `.cmp` foram copiados do pacote oficial em `Desktop\nand2tetris\nand2tetris\projects\7\`.

---

## Testes unitários (JUnit)

```powershell
mvn test
```

Cobertura básica: parser (comentários, tipos de comando) e CodeWriter (`push constant`, `static`, `add`).

---

## Componentes

| Classe | Responsabilidade |
|--------|------------------|
| `Parser` | Remove `//`, tokeniza linhas, expõe `commandType`, `arg1`, `arg2` |
| `CodeWriter` | Emite assembly Hack para push/pop e operações da pilha |
| `VMTranslator` | Orquestra parser + writer; entrada = 1 arquivo `.vm` |

Segmentos suportados: `constant`, `local`, `argument`, `this`, `that`, `temp`, `pointer`, `static` (prefixo = nome do arquivo `.asm` sem extensão).

---

## Desafios da implementação

- **`pop` em segmentos indexados:** calcular endereço base + índice em `R13` antes de desempilhar.
- **`static`:** usar rótulo `NomeArquivo.i` (ex.: `BasicTest.0`), não endereço fixo.
- **Comparações (`eq`, `gt`, `lt`):** rótulos únicos por operação para evitar colisão no `.asm`.
- **Ordem da pilha:** em operações binárias, o segundo operando desempilhado é o correto para `sub` e comparações.

---
