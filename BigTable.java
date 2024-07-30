package practice;

import java.util.TreeMap;
import java.util.HashMap;
import java.util.Map;

class Cell {
    private long timestamp;
    private String value;

    public Cell(long timestamp, String value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getValue() {
        return value;
    }
}

class Column {
    private TreeMap<Long, Cell> versions;

    public Column() {
        versions = new TreeMap<>();
    }

    public void addCell(Cell cell) {
        versions.put(cell.getTimestamp(), cell);
    }

    public Cell getCell(long timestamp) {
        return versions.floorEntry(timestamp).getValue();
    }
}

class Row {
    private Map<String, Column> columns;

    public Row() {
        columns = new HashMap<>();
    }

    public void addCell(String columnName, Cell cell) {
        columns.computeIfAbsent(columnName, k -> new Column()).addCell(cell);
    }

    public Cell getCell(String columnName, long timestamp) {
        Column column = columns.get(columnName);
        if (column == null) {
            return null;
        }
        return column.getCell(timestamp);
    }
}

public class BigTable {
    private Map<String, Row> rows;

    public BigTable() {
        rows = new HashMap<>();
    }

    public void put(String rowKey, String columnName, long timestamp, String value) {
        rows.computeIfAbsent(rowKey, k -> new Row()).addCell(columnName, new Cell(timestamp, value));
    }

    public Cell get(String rowKey, String columnName, long timestamp) {
        Row row = rows.get(rowKey);
        if (row == null) {
            return null;
        }
        return row.getCell(columnName, timestamp);
    }

    public static void main(String[] args) {
        BigTable bigTable = new BigTable();
        bigTable.put("row1", "col1", 1620000000000L, "value1");
        bigTable.put("row1", "col1", 1620000001000L, "value2");
        bigTable.put("row1", "col2", 1620000002000L, "value3");

        Cell cell0 = bigTable.get("row1", "col1", 1620000000050L);
        System.out.println("Retrieved cell: " + (cell0 != null ? cell0.getValue() : "null"));
        
        Cell cell1 = bigTable.get("row1", "col1", 1620000001050L);
        System.out.println("Retrieved cell: " + (cell1 != null ? cell1.getValue() : "null"));

        Cell cell2 = bigTable.get("row1", "col2", 1620000002050L);
        System.out.println("Retrieved cell: " + (cell2 != null ? cell2.getValue() : "null"));
    }

}
