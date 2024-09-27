package craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main class for the Lox interpreter
 * Opens a prompt for user input or accepts a file as an argument
 * and runs the interpreter on the input
 */

public class Lox {
    private static final Interpreter interpreter = new Interpreter();

    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    /**
     * Main method for the Lox interpreter
     * Determines the "mode" of the interpreter
     * Accepts a file as an argument or opens a prompt for user input
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Reads the file if the user provides one as an argument
     * @throws IOException
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    /**
     * Reads the user prompt then runs the interpreter on the input
     * @throws IOException
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    /**
     * Runs the scanner on the provided source code to generate tokens
     * Runs the parser on the tokens to generate an expression
     * Runs the interpreter on the expression
     */
    private static void run(String src) {
        Scanner scanner = new Scanner(src);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();


        if (hadError) return;

        interpreter.interpret(statements);
    }

    /**
     * Dispatch a syntaxt error from the scanner at a given line
     */
    static void error(Token token, String msg) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", msg);
        } else {
            report(token.line, " at '" + token.lexeme + "'", msg);
        }
    }
    static void error(int line, String msg) {
        report(line, "", msg);
    }

    private static void report(int line, String loc, String msg) {
        System.err.printf("[line %d] Error %s : %s", line, loc, msg);
        hadError = true;
    }

    /**
     * Distpatch and report a runtime error from the interpreter at a given line
     */
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadError = true;
    }

}

