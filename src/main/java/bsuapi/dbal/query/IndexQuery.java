package bsuapi.dbal.query;

import bsuapi.dbal.CypherException;
import bsuapi.dbal.Node;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.neo4j.graphdb.Result;

import java.util.Map;
import java.util.regex.Pattern;

public class IndexQuery extends CypherQuery
implements QueryResultCollector
{
    protected String indexName;
    protected long resultCount = 0;

    public IndexQuery(String indexName, String query)
    {
        this.indexName = indexName;
        this.initQuery = query;
    }

    public String getName() { return this.indexName; }

    public String getCommand()
    {
        return this.resultQuery = this.cleanCommand(this.initQuery) + this.getPageLimitCmd();
    }

    public long getResultCount() { return this.resultCount; }

    /**
     * Fulltext Index won't have a stable Java API until neo4j 3.6 at the earliest
     * lucene.apache.org/core/5_5_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package.description
     * neo4j.com/docs/cypher-manual/3.5/schema/index/#schema-index-fulltext-search
     * neo4j.com/developer/kb/fulltext-search-in-neo4j
     *
     * @param query lucene syntax query
     * @return String Cypher command to execute
     */
    protected String cleanCommand(String query)
    {
        // @todo add to RootResource & document search-syntax
        query = IndexQuery.sanitizeQuery(query);

        // DANGER! injection potential
        return
            "CALL db.index.fulltext.queryNodes(\""+ this.indexName+"\", \""+query+"\") "+
            "YIELD node " +
            "WITH count(node) as total " +
            "CALL db.index.fulltext.queryNodes(\""+ this.indexName+"\", \""+query+"\") " +
            "YIELD node, score " +
            "RETURN node, score, total "
            ;
    }

    /**
     * cleanQuery currently allows alphaNumeric, spaces, parens, quotes, +- and *
     * it should safely allow for the majority of lucene syntax queries
     */
    protected static Pattern strip = Pattern.compile("[^a-zA-Z0-9\\s()+\\-*~\"]");
    protected static String sanitizeQuery(String query)
    {
        query = strip.matcher(query).replaceAll("");

        int parenBalance = 0;
        boolean quotesOpen = false;

        for (int i = 0; i < query.length() && parenBalance >= 0; i++) {
            switch (query.charAt(i)) {
                case '(': parenBalance++; break;
                case ')': parenBalance--; break;
                case '"': quotesOpen = !quotesOpen; break;
            }
        }

        if (parenBalance != 0) {
            query = StringUtils.remove(query, '(');
            query = StringUtils.remove(query, ')');
        }

        if (quotesOpen) {
            query = StringUtils.remove(query, '"');
        }

        return query;
    }

    protected void rowHandler(Map<String, Object> row)
    {
        JSONObject node = null;
        double score = 0;
        for ( Map.Entry<String,Object> column : row.entrySet() ) {
            Object value = column.getValue();
            if (column.getKey().equals("node") && value instanceof org.neo4j.graphdb.Node) {
                node = (new Node((org.neo4j.graphdb.Node) value)).toJsonObject();
            } else if (column.getKey().equals("score")) {
                try {
                    score = (double) value;
                } catch (ClassCastException ignored) {
                    score = 0;
                }
            } else if (0 == this.resultCount && column.getKey().equals("total")) {
                try {
                    this.resultCount = (long) value;
                } catch (ClassCastException ignored) {}
            }
        }

        if (null != node) {
            node.put("searchScore", score);
            this.addEntry(node);
        }
    }

    @Override
    public void collectResult(Result result)
    throws CypherException
    {
        while ( result.hasNext()) {
            Map<String,Object> row = result.next();
            this.rowHandler(row);
        }
    }

    @Override
    public String toString() {
        return this.indexName +" "+ super.toString();
    }
}
