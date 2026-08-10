package dashboard.database;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Runs once, before the dashboard draws anything.
 *
 * What SQLite genuinely knows, and gives us for free:
 *   - table names, column names, storage type   (PRAGMA table_info)
 *   - real foreign key relationships             (PRAGMA foreign_key_list)
 * These are pulled fresh every run via ApiClient -> /api/schema/introspect.
 * There is nothing to hand-maintain here; if a column is added to the DB,
 * it shows up automatically next launch.
 *
 * What SQLite does NOT know, and never will from the storage type alone:
 *   - whether a TEXT column is a date string or a text label
 *   - whether an INTEGER column is a foreign key / id or a real measure
 * Two disambiguation strategies are used, on purpose kept separate so it's
 * obvious which one is doing the work:
 *   1. TEXT columns: the single sampled value is tested against a hardcoded
 *      canonical date pattern. This is safe to hardcode precisely BECAUSE
 *      the project enforces one date format everywhere (YYYY-MM-DD, or
 *      YYYY-MM-DD HH:MM:SS for timestamps) - a fixed format is a legitimate
 *      thing to hardcode, an unlimited variety of formats would not be.
 *   2. INTEGER/REAL columns: a single sample value CANNOT distinguish an id
 *      from a measure (1002 and 34 are both just numbers). There is no way
 *      around this without inspecting many rows for cardinality, which is
 *      out of scope for a startup-time check. So this still falls back to a
 *      column-name convention (*_id, id -> ID). This is flagged explicitly
 *      rather than silently pretending sampling solved it.
 */
public final class SchemaIntrospector {

    public enum SemanticType { ID, MEASURE, DIMENSION, DATE, UNKNOWN }

    // The one and only date format this project allows. Because it is
    // enforced project-wide, hardcoding its shape is safe and scalable -
    // this is a format check, not a guess about meaning.
    private static final Pattern DATE_ONLY = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern DATE_TIME = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");

    public static final class ColumnMeta {
        public final String name;
        public final String sqlType;      // as SQLite reports it: INTEGER / REAL / TEXT
        public final String sample;       // first observed non-null value, or null
        public final SemanticType semanticType;

        ColumnMeta(String name, String sqlType, String sample, SemanticType semanticType) {
            this.name = name;
            this.sqlType = sqlType;
            this.sample = sample;
            this.semanticType = semanticType;
        }

        @Override
        public String toString() {
            return name + "(" + sqlType + " -> " + semanticType + ", sample=" + sample + ")";
        }
    }

    public static final class ForeignKey {
        public final String column, refTable, refColumn;
        ForeignKey(String column, String refTable, String refColumn) {
            this.column = column;
            this.refTable = refTable;
            this.refColumn = refColumn;
        }
    }

    public static final class TableMeta {
        public final String name;
        public final Map<String, ColumnMeta> columns = new LinkedHashMap<>();
        public final List<ForeignKey> foreignKeys = new ArrayList<>();
        TableMeta(String name) { this.name = name; }
    }

    private SchemaIntrospector() {}

    /**
     * Hits the server, parses the response, classifies every column.
     * Call this once at startup, before any UI is built.
     */
    public static Map<String, TableMeta> introspect() throws Exception {
        String json = ApiClient.getData("api/schema/introspect", null);
        Object parsed = MiniJson.parse(json);
        Map<String, Object> root = asMap(parsed);

        if (!Boolean.TRUE.equals(root.get("success"))) {
            throw new RuntimeException("Schema introspection failed: " + root.get("error"));
        }

        Map<String, Object> rawTables = asMap(root.get("tables"));
        Map<String, TableMeta> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> tableEntry : rawTables.entrySet()) {
            String tableName = tableEntry.getKey();
            Map<String, Object> tableObj = asMap(tableEntry.getValue());
            TableMeta table = new TableMeta(tableName);

            for (Object colObj : asList(tableObj.get("columns"))) {
                Map<String, Object> col = asMap(colObj);
                String colName = String.valueOf(col.get("name"));
                String sqlType = String.valueOf(col.get("sqlType"));
                Object sampleRaw = col.get("sample");
                String sample = sampleRaw == null ? null : String.valueOf(sampleRaw);

                SemanticType semanticType = classify(colName, sqlType, sample);
                table.columns.put(colName, new ColumnMeta(colName, sqlType, sample, semanticType));
            }

            for (Object fkObj : asList(tableObj.get("foreignKeys"))) {
                Map<String, Object> fk = asMap(fkObj);
                table.foreignKeys.add(new ForeignKey(
                        String.valueOf(fk.get("column")),
                        String.valueOf(fk.get("refTable")),
                        String.valueOf(fk.get("refColumn"))
                ));
            }

            result.put(tableName, table);
        }

        return result;
    }

    /**
     * Classification order:
     *   1. sqlType TEXT   -> regex the sample against the canonical date
     *      format. Matches -> DATE. Otherwise -> DIMENSION (a label like
     *      channel, warehouse, category, country, gender, region).
     *   2. sqlType INTEGER/REAL -> name convention only, since the sample
     *      value genuinely cannot tell an id apart from a measure.
     *   3. Anything else (no sample, empty table, unexpected sqlType) ->
     *      UNKNOWN, so it can be surfaced instead of silently mis-filed.
     */
    static SemanticType classify(String columnName, String sqlType, String sample) {
        String type = sqlType == null ? "" : sqlType.toUpperCase(Locale.ROOT);
        String lowerName = columnName.toLowerCase(Locale.ROOT);

        if (type.contains("CHAR") || type.equals("TEXT") || type.equals("")) {
            if (sample != null && (DATE_ONLY.matcher(sample).matches()
                    || DATE_TIME.matcher(sample).matches())) {
                return SemanticType.DATE;
            }
            if (sample == null) {
                return SemanticType.UNKNOWN; // empty column, nothing to test
            }
            return SemanticType.DIMENSION;
        }

        if (type.contains("INT") || type.contains("REAL")
                || type.contains("FLOA") || type.contains("DOUB")) {
            // Sample value can't disambiguate id vs. measure - fall back to
            // the naming convention this schema actually follows.
            if (lowerName.equals("id") || lowerName.endsWith("_id")) {
                return SemanticType.ID;
            }
            return SemanticType.MEASURE;
        }

        return SemanticType.UNKNOWN;
    }

    /**
     * Enforces the "only compare what's actually relatable" rule:
     *   - two MEASURE columns can be compared/plotted directly
     *   - a MEASURE can be compared against another MEASURE once both are
     *     bucketed by a shared DATE column (time series)
     *   - two DIMENSION columns can be cross-tabulated / grouped
     *   - a DIMENSION can bucket a MEASURE (group-by + aggregate)
     *   - two ID columns can be compared only via a real foreign key
     *   - anything else is rejected rather than silently plotted
     */
    public static boolean canCompare(ColumnMeta a, ColumnMeta b) {
        if (a.semanticType == SemanticType.UNKNOWN || b.semanticType == SemanticType.UNKNOWN) {
            return false;
        }
        if (a.semanticType == SemanticType.MEASURE && b.semanticType == SemanticType.MEASURE) {
            return true;
        }
        if (a.semanticType == SemanticType.MEASURE && b.semanticType == SemanticType.DATE) {
            return true;
        }
        if (a.semanticType == SemanticType.DATE && b.semanticType == SemanticType.MEASURE) {
            return true;
        }
        if (a.semanticType == SemanticType.MEASURE && b.semanticType == SemanticType.DIMENSION) {
            return true;
        }
        if (a.semanticType == SemanticType.DIMENSION && b.semanticType == SemanticType.MEASURE) {
            return true;
        }
        if (a.semanticType == SemanticType.DIMENSION && b.semanticType == SemanticType.DIMENSION) {
            return true;
        }
        // ID <-> ID is only meaningful through an actual foreign key, which
        // the caller should check separately via TableMeta.foreignKeys.
        return false;
    }

    // ---- minimal dependency-free JSON reader -------------------------
    // Swap this out for org.json / Jackson if either is already on your
    // classpath; this exists only so the file is self-contained.

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object o) {
        if (o instanceof List) return (List<Object>) o;
        return Collections.emptyList();
    }

    static final class MiniJson {
        private final String s;
        private int i;

        private MiniJson(String s) { this.s = s; }

        static Object parse(String json) {
            MiniJson p = new MiniJson(json);
            p.skipWs();
            Object v = p.readValue();
            return v;
        }

        private Object readValue() {
            skipWs();
            char c = s.charAt(i);
            switch (c) {
                case '{': return readObject();
                case '[': return readArray();
                case '"': return readString();
                case 't': i += 4; return Boolean.TRUE;
                case 'f': i += 5; return Boolean.FALSE;
                case 'n': i += 4; return null;
                default:  return readNumber();
            }
        }

        private Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            i++; // {
            skipWs();
            if (s.charAt(i) == '}') { i++; return map; }
            while (true) {
                skipWs();
                String key = readString();
                skipWs();
                i++; // :
                Object val = readValue();
                map.put(key, val);
                skipWs();
                if (s.charAt(i) == ',') { i++; continue; }
                if (s.charAt(i) == '}') { i++; break; }
            }
            return map;
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            i++; // [
            skipWs();
            if (s.charAt(i) == ']') { i++; return list; }
            while (true) {
                Object val = readValue();
                list.add(val);
                skipWs();
                if (s.charAt(i) == ',') { i++; continue; }
                if (s.charAt(i) == ']') { i++; break; }
            }
            return list;
        }

        private String readString() {
            StringBuilder sb = new StringBuilder();
            i++; // opening "
            while (s.charAt(i) != '"') {
                char c = s.charAt(i);
                if (c == '\\') {
                    i++;
                    char esc = s.charAt(i);
                    switch (esc) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'u':
                            String hex = s.substring(i + 1, i + 5);
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                            break;
                        default: sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
                i++;
            }
            i++; // closing "
            return sb.toString();
        }

        private Object readNumber() {
            int start = i;
            while (i < s.length() && "-+.eE0123456789".indexOf(s.charAt(i)) >= 0) i++;
            String num = s.substring(start, i);
            if (num.contains(".") || num.toLowerCase(Locale.ROOT).contains("e")) {
                return Double.parseDouble(num);
            }
            return Long.parseLong(num);
        }

        private void skipWs() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }
    }
}