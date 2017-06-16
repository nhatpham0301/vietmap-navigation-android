package com.mapbox.services.android.navigation.v5.milestone;

import java.util.Map;

public class Trigger {

  /**
   * Base Trigger statement. Subclassed to provide concrete statements.
   *
   * @since 0.4.0
   */
  @SuppressWarnings("WeakerAccess") // Public exposed for creation of compound statements outside SDK
  public abstract static class Statement {

    public Statement() {
    }

    /**
     * Validates whether the statement meets the specified trigger criteria.
     *
     * @param statementObjects a {@link Map} that contains all the trigger statements to validate
     * @return true if the statement is valid, otherwise false
     * @since 0.4.0
     */
    public abstract boolean validate(Map<Integer, Number[]> statementObjects);
  }

  /*
   * Compound statements
   */

  /**
   * All class used to validate that all of the statements are valid.
   *
   * @since 0.4.0
   */
  private static class AllStatement extends Statement {
    private final Statement[] statements;

    AllStatement(Statement... statements) {
      this.statements = statements;
    }

    @Override
    public boolean validate(Map<Integer, Number[]> statementObjects) {
      boolean all = true;
      for (Statement statement : statements) {
        if (!statement.validate(statementObjects)) {
          all = false;
        }
      }
      return all;
    }
  }

  /**
   * None class used to validate that none of the statements are valid.
   *
   * @since 0.4.0
   */
  private static class NoneStatement extends Statement {
    private final Statement[] statements;

    NoneStatement(Statement... statements) {
      this.statements = statements;
    }

    @Override
    public boolean validate(Map<Integer, Number[]> statementObjects) {
      for (Statement statement : statements) {
        if (statement.validate(statementObjects)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Any class used to validate that any of the statements are valid.
   *
   * @since 0.4.0
   */
  private static class AnyStatement extends Statement {
    private final Statement[] statements;

    AnyStatement(Statement... statements) {
      this.statements = statements;
    }

    @Override
    public boolean validate(Map<Integer, Number[]> statementObjects) {
      for (Statement statement : statements) {
        if (statement.validate(statementObjects)) {
          return true;
        }
      }
      return false;
    }
  }

  /*
   * Simple statement
   */

  /**
   * Greater than class used to validate that the {@code RouteProgress} key property is greater than the specified
   * value.
   *
   * @since 0.4.0
   */
  private static class GreaterThanStatement extends Statement {
    private final int key;
    private final Object value;

    GreaterThanStatement(int key, Object value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public boolean validate(Map<Integer, Number[]> statementObjects) {
      return Operation.greaterThan(statementObjects.get(key), (Number) value);
    }
  }

  /**
   * Greater than equal class used to validate that the {@code RouteProgress} key property is greater than or equal to
   * the specified value.
   *
   * @since 0.4.0
   */
  private static class GreaterThanEqualStatement extends Statement {
    private final int key;
    private final Object value;

    GreaterThanEqualStatement(int key, Object value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public boolean validate(Map<Integer, Number[]> statementObjects) {
      return Operation.greaterThanEqual(statementObjects.get(key), (Number) value);
    }
  }

  /**
   * Less than class used to validate that the {@code RouteProgress} key property is less than the specified value.
   *
   * @since 0.4.0
   */
  private static class LessThanStatement extends Statement {
    private final int key;
    private final Object value;

    LessThanStatement(int key, Object value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public boolean validate(Map<Integer, Number[]> statementObjects) {
      return Operation.lessThan(statementObjects.get(key), (Number) value);
    }
  }

  /**
   * Less than equal class used to validate that the {@code RouteProgress} key property is less than or equal to the
   * specified value.
   *
   * @since 0.4.0
   */
  private static class LessThanEqualStatement extends Statement {
    private final int key;
    private final Object value;

    LessThanEqualStatement(int key, Object value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public boolean validate(Map<Integer, Number[]> statementObjects) {
      return Operation.lessThanEqual(statementObjects.get(key), (Number) value);
    }
  }

  /**
   * Not equals class used to validate that the {@code RouteProgress} key property does not equal the specified value.
   *
   * @since 0.4.0
   */
  private static class NotEqualStatement extends Statement {
    private final int key;
    private final Object[] values;

    NotEqualStatement(int key, Object... values) {
      this.key = key;
      this.values = values;
    }

    @Override
    public boolean validate(Map<Integer, Number[]> statementObjects) {
      return Operation.notEqual(statementObjects.get(key), (Number) values[0]);
    }
  }

  /**
   * Equals class used to validate that the {@code RouteProgress} key property equals the specified value.
   *
   * @since 0.4.0
   */
  private static class EqualStatement extends Statement {
    private final int key;
    private final Object value;

    EqualStatement(int key, Object value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public boolean validate(Map<Integer, Number[]> statementObjects) {
      return Operation.equal(statementObjects.get(key), (Number) value);
    }
  }

  /**
   * Groups a collection of statements in an {@code all} relationship.
   *
   * @param statements the collection of statements
   * @return the statements compounded
   * @since 0.4.0
   */
  public static Statement all(Statement... statements) {
    return new AllStatement(statements);
  }

  /**
   * Groups a collection of statements in an {@code any} relationship.
   *
   * @param statements the collection of statements
   * @return the statements compounded
   * @since 0.4.0
   */
  public static Statement any(Statement... statements) {
    return new AnyStatement(statements);
  }

  /**
   * Groups a collection of statements in an {@code none} relationship.
   *
   * @param statements the collection of statements
   * @return the statements compounded
   * @since 0.4.0
   */
  public static Statement none(Statement... statements) {
    return new NoneStatement(statements);
  }

  /**
   * Check the property equals the given value.
   *
   * @param key   the property key which must be one of the constants found in {@link TriggerProperty}
   * @param value the value to check against
   * @return the statement
   * @since 0.4.0
   */
  public static Statement eq(int key, Object value) {
    return new EqualStatement(key, value);
  }

  /**
   * Check the property does not equals the given value.
   *
   * @param key   the property key which must be one of the constants found in {@link TriggerProperty}
   * @param value the value to check against
   * @return the statement
   * @since 0.4.0
   */
  public static Statement neq(int key, Object value) {
    return new NotEqualStatement(key, value);
  }

  /**
   * Check the property exceeds the given value.
   *
   * @param key   the property key which must be one of the constants found in {@link TriggerProperty}
   * @param value the value to check against
   * @return the statement
   * @since 0.4.0
   */
  public static Statement gt(int key, Object value) {
    return new GreaterThanStatement(key, value);
  }

  /**
   * Check the property does not exceeds the given value.
   *
   * @param key   the property key which must be one of the constants found in {@link TriggerProperty}
   * @param value the value to check against
   * @return the statement
   * @since 0.4.0
   */
  public static Statement lt(int key, Object value) {
    return new LessThanStatement(key, value);
  }

  /**
   * Check the property equals or does not exceeds the given value.
   *
   * @param key   the property key which must be one of the constants found in {@link TriggerProperty}
   * @param value the value to check against
   * @return the statement
   * @since 0.4.0
   */
  public static Statement lte(int key, Object value) {
    return new LessThanEqualStatement(key, value);
  }

  /**
   * Check the property exceeds or equals the given value.
   *
   * @param key   the property key which must be one of the constants found in {@link TriggerProperty}
   * @param value the value to check against
   * @return the statement
   * @since 0.4.0
   */
  public static Statement gte(int key, Object value) {
    return new GreaterThanEqualStatement(key, value);
  }


}
