package craftinginterpreters.lox;

import static craftinginterpreters.lox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // first char in the lexeme being scanned
    private int start = 0;
    // the current char being considered
    private int current = 0;
    // source line of the current char
    private int line = 1;

    // Defined set of keywords
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    Scanner(String src) {
        this.source = src;
    }

    /**
     * Scans the source code and returns a list of tokens
     * @return List<Token>
     */
    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(':   addToken(LEFT_PAREN);   break;
            case ')':   addToken(RIGHT_PAREN);  break;
            case '{':   addToken(LEFT_BRACE);   break;
            case '}':   addToken(RIGHT_BRACE);  break;
            case ',':   addToken(COMMA);        break;
            case '.':   addToken(DOT);          break;
            case '-':   addToken(MINUS);        break;
            case '+':   addToken(PLUS);         break;
            case ';':   addToken(SEMICOLON);    break;
            case '*':   addToken(STAR);         break;
            /* ------------------------------------ */
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            /* ------------------------------------ */
            case 'o':
                if (match('r')) addToken(OR);
                break;
            /* ------------------------------------ */  
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();

                } else if (match('*')) {
                    // A block comment goes until the end of the block.
                    while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
                        if (peek() == '\n') line++;
                        advance();
                    }
                    // Consume the closing '*/'
                    advance(); advance();

                } else {
                    // Regular slash
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\t':
            case '\r':  break;
            case '\n':  line++;     break;
            /* ------------------------------------ */ 
            case '"':   string();   break;
            default:
                if (isDigit(c)) {
                    // Reached a number
                    number();
                } else if (isAlpha(c)) {
                    // Reached an identifier
                    identifier();
                } else {
                    Lox.error(line, "Error: Unexpected character.");
                }
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||
            c == '_';
      }
    
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add( new Token(type, text, literal, line) );
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;

        if (source.charAt(current) != expected) return false;

        // When the character matches consume it
        current++; 
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';

        return source.charAt(current);
    }

    private char peekNext() {
        if (current+1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()){
            if (peek() == '\n') line++;

            advance();
        }

        // If you reach the end of source before next '"'
        if (isAtEnd()) {
            Lox.error(line, "Error: Unterminated String.");
            return;
        }

        // Advance to index containing the '"' 
        advance();

        // Extract the literal without '"'s
        String literal = source.substring(start+1, current-1);
        addToken(STRING, literal);
    }

    private void number() {
        while (isDigit(peek()) && !isAtEnd()){
            advance();
        }

        // Allow for decimal if followed by more digits
        if (peek() == '.' && isDigit(peekNext())){
            advance();
            // Consume digits after decimal
            while (isDigit(peek()) && !isAtEnd()){
                advance();
            }
        }

        // Extract value
        double number = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, number);
    }

    private void identifier() {
        // Consume the rest of the identifier
        while (isAlphaNumeric( peek() )) advance();
        
        // Extract the identifier
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        // Not a keyword then its user defined
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }
}
