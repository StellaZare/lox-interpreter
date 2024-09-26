package craftinginterpreters.lox;

/**
 * Represents a token in the source code.
 * Token type defined in TokenType.java
 */
public class Token {
    
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token (TokenType type, String lex, Object literal, int line) {
        this.type = type;
        this.lexeme = lex;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
