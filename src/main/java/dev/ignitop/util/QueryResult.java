package dev.ignitop.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.ignite.cache.query.FieldsQueryCursor;

/**
 *
 */
public class QueryResult {
    /** Columns of query result set. */
    private final List<String> columns;

    /** Rows of query result set. */
    private final List<List<?>> rows;

    /**
     * @param qryCursor Query cursor.
     */
    public QueryResult(FieldsQueryCursor<List<?>> qryCursor) {
        rows = qryCursor.getAll();

        int colCnt = qryCursor.getColumnsCount();

        columns = new ArrayList<>(colCnt);

        for (int i = 0; i < colCnt; i++)
            columns.add(qryCursor.getFieldName(i));
    }

    /**
     * @return Columns of query result set.
     */
    public List<String> columns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * @return Rows of query result set.
     */
    public List<List<?>> rows() {
        return Collections.unmodifiableList(rows);
    }

    /**
     *
     */
    public boolean isEmpty() {
        return rows().isEmpty();
    }
}
