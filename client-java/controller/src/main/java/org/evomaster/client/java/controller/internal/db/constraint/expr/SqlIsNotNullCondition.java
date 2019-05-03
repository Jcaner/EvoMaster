package org.evomaster.client.java.controller.internal.db.constraint.expr;

import java.util.Objects;

public class SqlIsNotNullCondition extends SqlCondition {
    private final /*non-null*/ SqlColumn sqlColumn;

    public SqlIsNotNullCondition(SqlColumn sqlColumn) {
        if (sqlColumn == null) {
            throw new IllegalArgumentException("Column name cannot be null");
        }
        this.sqlColumn = sqlColumn;
    }


    @Override
    public String toSql() {
        return sqlColumn.toSql() + " IS NOT NULL";
    }

    @Override
    public <K, V> K accept(SqlConditionVisitor<K, V> visitor, V argument) {
        return visitor.visit(this, argument);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlIsNotNullCondition that = (SqlIsNotNullCondition) o;
        return sqlColumn.equals(that.sqlColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlColumn);
    }
}
