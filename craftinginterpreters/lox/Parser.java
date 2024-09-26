package craftinginterpreters.lox;

import static craftinginterpreters.lox.TokenType.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Parses the tokens into an expression
 *      1. Given a valid sequence of tokens, produce a corresponding syntax tree
 *      2. Given an invalid sequence of tokens, produce a good error message
 */
public class Parser {
    private static class ParseError extends RuntimeException {}

    // list of tokens to be parsed
    private final List<Token> tokens;
    // next token to be parsed
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses a single expression and returns it
     * Syntax error handling is the parser's responsibility, so we catch any exceptions
     * When an error occurs, we return null and call the error() in Lox.java
     */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }
        return statements;
    }

    /**
     * expression -> equality
     */
    private Expr expression() {
        return equality();
    }

    /**
     * Parses a statement
     * @return
     */
    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    /**
     * equality -> comparison ( ( "!=" | "==" ) comparison )*
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )*
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * term -> factor ( ( "-" | "+" ) factor )*
     */
    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * factor -> unary ( ( "/" | "*" ) unary )*
     * @return  Expr    the parsed expression
     */
    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * unary -> ( "!" | "-" ) unary | primary
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    /**
     * primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")"
     */
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    /**
     * Checks if the current token is of the given type
     * If it is, it consumes the token and returns true
     * @param types
     * @return boolean
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the current token is of the given type
     * if it is, it consumes the token\
     * if some other token is found, it throws an error
     * @param type
     * @return Token   the consumed token
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    /**
     * Returns true if the current token is of the given type
     * Never consumes the token
     * @param type
     * @return Token  the current token
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    /**
     * Consumes the current token and returns it
     * @return Token the consumed token
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * Returns true when the parser has consumed all the tokens
     * @return boolean
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * Retuen the current token to be consumed
     * @return Token the current token to be consumed 
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Returns the last token consumed
     * @return Token the last token consumed 
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * Report an error at a given token
     * Return a ParseError so the caller can throw it
     * @param token
     * @param message
     * @return
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * Synchronize the parser after an error
     * Consume tokens until we find a statement boundary
     * Assuming that the next statememt starts with one of the specified tokens
     */
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}

