/*
 * Copyright 2023 Ilya Shishkov (https://github.com/shishkovilja)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ignitop.ignite.util;

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
