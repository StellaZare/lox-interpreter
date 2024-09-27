package craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * Data structure to store variable bindings - associates variable names with their values
 * This is used to store the variables in the current scope
 * 
 */
public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    /**
     * Look up variable by name
     * @throws RuntimeError if the variable is not found
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    /**
     * Binds the name of the variable to a value
     */
    void define(String name, Object value) {
        values.put(name, value);
    }


}
