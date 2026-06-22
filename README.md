# VMTranslator — Partes 1 e 2 (Projects 07 e 08)

Tradutor **VM → Assembly Hack** para o curso nand2tetris.

| Parte | Conteúdo |
|-------|----------|
| **Parte 1** | `push`/`pop`, operações aritméticas/lógicas |
| **Parte 2** | Bootstrap, controle de fluxo (`label`, `goto`, `if-goto`), sub-rotinas (`function`, `call`, `return`), múltiplos `.vm` por diretório |

*Curso:* Engenharia da Computação — CCET — UFMA  
*Professor:* Sergio Souza Costa

## Equipe

| Integrante | Matrícula |
|------------|-----------|
| **Paulo Eduardo Lima Rabelo** | 20260001203 |
| **Italo Francisco Almeida de Oliveira** | 20260001230 |

## Linguagem e ferramentas

| Item | Versão / detalhe |
|------|------------------|
| Linguagem | **Java** 17+ (Java puro, sem frameworks) |
| Build | **Maven** 3.x (opcional) ou `javac` |
| IDE | IntelliJ IDEA (opcional) |
| Testes unitários | **JUnit 5** |
| Validação funcional | **CPUEmulator** (nand2tetris) |

---

## Estrutura do projeto

```bash
VMTranslator/
├── pom.xml
├── out/                              # bytecode compilado
├── src/main/
│   ├── cmd/VMTranslator.java         # ponto de entrada
│   ├── parser/Parser.java            # leitura dos .vm
│   └── codewriter/CodeWriter.java      # geração do .asm
├── src/test/                         # testes JUnit
├── projects/
│   ├── 07/                           # testes Parte 1 (Project 07)
│   └── 08/                           # testes Parte 2 (Project 08)
│       ├── ProgramFlow/
│       └── FunctionCalls/
└── README.md
```

---

## Pré-requisitos

1. **JDK 17+** instalado — verifique no terminal:

```powershell
java -version
javac -version
```

2. **CPUEmulator** do pacote nand2tetris (para validar os testes `.tst`).

3. Clone ou baixe este repositório e abra um terminal na pasta raiz `VMTranslator/`.

---

## Passo a passo — compilar, traduzir e testar

### 1. Compilar o tradutor

Abra o PowerShell na pasta do projeto:

```powershell
cd C:\Users\Paulo\Desktop\UFMA\VMTranslator

New-Item -ItemType Directory -Force -Path out
javac -encoding UTF-8 -d out src\main\parser\*.java src\main\codewriter\*.java src\main\cmd\*.java
```

**Resultado esperado:** nenhuma mensagem de erro; arquivos `.class` em `out\`.

**Alternativa — IntelliJ:** **File → Open** na pasta do projeto → **Build → Build Project**.

**Alternativa — Maven** (se `mvn` estiver no PATH):

```powershell
mvn compile
```

---

### 2. Gerar o arquivo `.asm`

O CPUEmulator executa **`.asm`**, não `.vm`. O tradutor aceita **um arquivo `.vm`** ou **um diretório** com vários `.vm`.

#### Modo arquivo único (Parte 1 e ProgramFlow)

Use quando o teste tem **apenas um** `.vm` e **não** precisa de bootstrap.

```powershell
java -cp out cmd.VMTranslator caminho\para\Programa.vm
```

**Saída:** `Programa.asm` na **mesma pasta** do `.vm` (sem bootstrap).

**Exemplo — BasicLoop (Parte 2):**

```powershell
java -cp out cmd.VMTranslator projects\08\ProgramFlow\BasicLoop\BasicLoop.vm
```

**Saída esperada no terminal:**

```
Traduzido: projects\08\ProgramFlow\BasicLoop\BasicLoop.asm
```

#### Modo diretório (Parte 2 — FunctionCalls com Sys.init)

Use quando a pasta contém **vários** `.vm` ou quando o teste exige **bootstrap**.

```powershell
java -cp out cmd.VMTranslator caminho\para\Diretorio
```

**Saída:** `NomeDaPasta.asm` **dentro** do diretório, contendo:

1. Bootstrap (`SP = 256` + `call Sys.init 0`)
2. Todos os `.vm` do diretório concatenados

**Exemplo — NestedCall:**

```powershell
java -cp out cmd.VMTranslator projects\08\FunctionCalls\NestedCall
```

**Saída esperada:**

```
Traduzido: projects\08\FunctionCalls\NestedCall\NestedCall.asm
```

> **Regra prática:** `ProgramFlow` e `SimpleFunction` → traduza o **arquivo `.vm`**. Testes com `Sys.init` (NestedCall, FibonacciElement, StaticsTest) → traduza o **diretório**.

#### Tabela rápida — o que traduzir

| Teste | Comando |
|-------|---------|
| `projects/07/.../SimpleAdd.vm` | `java -cp out cmd.VMTranslator projects\07\StackArithmetic\SimpleAdd\SimpleAdd.vm` |
| `projects/08/.../BasicLoop.vm` | `java -cp out cmd.VMTranslator projects\08\ProgramFlow\BasicLoop\BasicLoop.vm` |
| `projects/08/.../SimpleFunction.vm` | `java -cp out cmd.VMTranslator projects\08\FunctionCalls\SimpleFunction\SimpleFunction.vm` |
| `projects/08/.../NestedCall/` | `java -cp out cmd.VMTranslator projects\08\FunctionCalls\NestedCall` |
| `projects/08/.../FibonacciElement/` | `java -cp out cmd.VMTranslator projects\08\FunctionCalls\FibonacciElement` |

---

### 3. Testar no CPUEmulator

O `.tst` é um **script de teste**: carrega o `.asm`, simula a CPU, grava `.out` e compara com `.cmp`.

#### Opção A — Interface gráfica

1. Abra a pasta `nand2tetris\tools` e execute `CPUEmulator.bat`.
2. **File → Load Script…** *(não use Load Program — ele não aceita `.tst`)*
3. Navegue até a pasta do teste e selecione o `.tst` (ex.: `BasicLoop.tst`).
4. Aguarde o script terminar.
5. Role até o **rodapé** da janela e confirme:

```
Comparison ended successfully
```

> Na GUI, a linha `output;` em amarelo indica o último passo do script — **não** é a confirmação final. A mensagem de sucesso fica na parte inferior da janela.

#### Opção B — Terminal (recomendado)

```powershell
cd C:\Users\Paulo\Desktop\UFMA\nand2tetris\nand2tetris\tools
.\CPUEmulator.bat C:\Users\Paulo\Desktop\UFMA\VMTranslator\projects\08\ProgramFlow\BasicLoop\BasicLoop.tst
```

**Resultado esperado:**

```
End of script - Comparison ended successfully
```

| Mensagem | Significado |
|----------|-------------|
| `Comparison ended successfully` | Teste **passou** — `.out` igual ao `.cmp` |
| `Comparison failure at line X` | Teste **falhou** — revise o tradutor e gere o `.asm` de novo |

---

### 4. Exemplos completos por teste (Parte 2)

Substitua o caminho do nand2tetris se o seu for diferente.

#### BasicLoop (controle de fluxo)

```powershell
cd C:\Users\Paulo\Desktop\UFMA\VMTranslator
java -cp out cmd.VMTranslator projects\08\ProgramFlow\BasicLoop\BasicLoop.vm

cd C:\Users\Paulo\Desktop\UFMA\nand2tetris\nand2tetris\tools
.\CPUEmulator.bat C:\Users\Paulo\Desktop\UFMA\VMTranslator\projects\08\ProgramFlow\BasicLoop\BasicLoop.tst
```

**Valores esperados** (`BasicLoop.cmp`): `RAM[0]=257`, `RAM[256]=6` (soma 1+2+3 com n=3).

#### SimpleFunction (function / return)

```powershell
cd C:\Users\Paulo\Desktop\UFMA\VMTranslator
java -cp out cmd.VMTranslator projects\08\FunctionCalls\SimpleFunction\SimpleFunction.vm

cd C:\Users\Paulo\Desktop\UFMA\nand2tetris\nand2tetris\tools
.\CPUEmulator.bat C:\Users\Paulo\Desktop\UFMA\VMTranslator\projects\08\FunctionCalls\SimpleFunction\SimpleFunction.tst
```

#### NestedCall (bootstrap + call aninhado)

```powershell
cd C:\Users\Paulo\Desktop\UFMA\VMTranslator
java -cp out cmd.VMTranslator projects\08\FunctionCalls\NestedCall

cd C:\Users\Paulo\Desktop\UFMA\nand2tetris\nand2tetris\tools
.\CPUEmulator.bat C:\Users\Paulo\Desktop\UFMA\VMTranslator\projects\08\FunctionCalls\NestedCall\NestedCall.tst
```

**Valores principais esperados:** temp 0 = **135** (123+12), temp 1 = **246** (soma dos locals).

---

### 5. Executar pelo IntelliJ (opcional)

1. **File → Open** → pasta `VMTranslator` → **Load Maven Project**.
2. Abra `src/main/cmd/VMTranslator.java`.
3. Clique no ▶ verde → **Modify Run Configuration…**
4. Configure:
   - **Main class:** `cmd.VMTranslator`
   - **Working directory:** `$ProjectFileDir$`
   - **Program arguments:** caminho do `.vm` ou diretório (ex.: `projects\08\FunctionCalls\NestedCall`)
5. **Run** — o console deve exibir `Traduzido: ...`.

A validação no CPUEmulator continua sendo feita **fora** do IntelliJ (passos da seção 3).

---

## Status dos testes

### Project 07 (Parte 1)

| Programa | Caminho | Resultado |
|----------|---------|-----------|
| **SimpleAdd** | `projects/07/StackArithmetic/SimpleAdd/` | Passou |
| **BasicTest** | `projects/07/MemoryAccess/BasicTest/` | Passou |

### Project 08 (Parte 2)

| Programa | Como traduzir | Resultado |
|----------|---------------|-----------|
| **BasicLoop** | arquivo `.vm` | Passou |
| **FibonacciSeries** | arquivo `.vm` | Passou |
| **SimpleFunction** | arquivo `.vm` | Passou |
| **NestedCall** | **diretório** | Passou |
| **FibonacciElement** | diretório | Passou |
| **StaticsTest** | diretório | Passou |

---

## Testes unitários (JUnit)

Requer Maven com dependências baixadas:

```powershell
mvn test
```

Cobertura: parser (comentários, tipos Parte 1 e 2) e CodeWriter (push, static, bootstrap, labels).

---

## Componentes

| Classe | Responsabilidade |
|--------|------------------|
| `Parser` | Remove comentários, tokeniza linhas, expõe `commandType`, `arg1`, `arg2` |
| `CodeWriter` | Emite assembly Hack: push/pop, aritmética, fluxo, call/return, bootstrap |
| `VMTranslator` | Orquestra parser + writer; entrada = arquivo `.vm` ou diretório |

### Parte 2 — detalhes de implementação

- **Rótulos:** `funcao$label` dentro de funções; nome simples fora de função.
- **Retorno de `call`:** rótulos únicos `funcao$ret.N`.
- **`static`:** prefixo = nome do arquivo `.vm` (via `setFileName`).
- **Bootstrap:** `SP=256` + `call Sys.init 0` (somente em modo diretório).

Segmentos suportados: `constant`, `local`, `argument`, `this`, `that`, `temp`, `pointer`, `static`.

---

## Problemas comuns

| Problema | Causa provável | Solução |
|----------|----------------|---------|
| `java` não reconhecido | JDK fora do PATH | Instale JDK 17+ e reinicie o terminal |
| `Entrada não encontrada` | Caminho errado | Confira `cd` e caminho completo |
| `Comparison failure` no NestedCall | Traduziu `Sys.vm` em vez do diretório | Use `VMTranslator projects\08\FunctionCalls\NestedCall` |
| Bootstrap ausente no `.asm` | Modo arquivo em teste de diretório | Traduza a **pasta** inteira |
| CPUEmulator rejeita `.tst` | Usou Load Program | Use **File → Load Script…** |
| Teste passa sem rodar tradutor | `.asm` antigo na pasta | Apague o `.asm`, traduza de novo e reteste |

---

## Fluxo resumido

| Etapa | Ferramenta | Ação |
|-------|------------|------|
| 1 | Terminal / IntelliJ | Compilar (`javac` ou Build Project) |
| 2 | Terminal / IntelliJ | `java -cp out cmd.VMTranslator ...` → gera `.asm` |
| 3 | CPUEmulator | Load Script → `.tst` → `Comparison ended successfully` |

---
