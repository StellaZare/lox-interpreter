package craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * Data structure to store variable bindings - associates variable names with their values
 * This is used to store the variables in the current scope
 * 
 */
public class Environment {
    // Reference to the enclosing scope environment 
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Look up variable by name
     * @throws RuntimeError if the variable is not found
     */
    Object get(Token name) {
        // Look in the current scope
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        // Look in the enclosing scope
        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    /**
     * Binds the name of the variable to a value
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Modifies the value bound to an existing variable
     * Lox does not support implicit variable declaration 
     * 
     * @throws RuntimeError if the variable is not found
     */
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined varibale '" + name.lexeme + "'.");
    }


}
