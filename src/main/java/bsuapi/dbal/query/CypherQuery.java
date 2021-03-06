package bsuapi.dbal.query;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Node;
import bsuapi.dbal.NodeType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.Result;
import org.neo4j.helpers.collection.Iterators;

import java.util.Iterator;

abstract public class CypherQuery
{
    protected String initQuery;
    protected String resultQuery;
    protected JSONArray results;
    public int limit = 20;
    public int page = 0;
    public NodeType target;

    public static final String limitParam = "limit";
    public static final String pageParam = "page";

    public CypherQuery (String query)
    {
        this.initQuery = query;
        this.resultQuery = query;
    }

    protected CypherQuery() {}

    abstract public String getCommand();

    public void setLimit(String limit)
    {
        int lim = this.limit;

        try {
            lim = Integer.parseInt(limit);
            lim = Math.abs(lim);
        } catch (NumberFormatException e) {
            lim = this.limit;
        } finally {
            this.setLimit(lim);
        }
    }

    public void setLimit(int limit)
    {
        if (limit == 0) limit = 1000;
        this.limit = limit;
    }

    public void setPage(String page)
    {
        int p = this.page;
        try {
            p = Integer.parseInt(page);
            p = Math.abs(p);
        } catch (NumberFormatException e) {
            p = this.page;
        } finally {
            this.setPage(p);
        }
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    public String getPageLimitCmd()
    {
        String result = "";
        if (this.page > 1) {
            result += " SKIP " + ((this.page -1) * this.limit);
        }

        return result + " LIMIT " + this.limit + " ";
    }

    public String toString()
    {
        return this.getClass().getSimpleName() +": \""+ this.initQuery +"\"";
    }

    protected void addEntry(Object entry)
    {
        if (this.results == null) {
            this.results = new JSONArray();
        }

        this.results.put(entry);
    }

    public JSONArray exec(Cypher c)
    throws CypherException
    {
        if (this instanceof QueryResultCollector) {
            c.query((QueryResultCollector) this);
        } else {
            throw new CypherException(this.getClass().getSimpleName() +" CypherQuery does not implement a result collector.");
        }

        return this.results;
    }



    //-- QueryResultSingleColumn --//
    // These should only be in children implementing QueryResultSingleColumn, but I'm avoiding code duplication
    // haven't yet decided on one of two possible patterns here:
    //     distinct collector objects
    //     CypherQuery <-- CollectionType <-- Query
    public void collectResult(Result result)
    throws CypherException
    {
        Iterator<Object> resultIterator = result.columnAs(QueryResultSingleColumn.resultColumn);
        for (Object entry : Iterators.asIterable(resultIterator)) {
            this.entryHandler(entry);
        }
    }

    public void entryHandler(String entry) {
        this.addResultEntry(entry);
    }

    public void entryHandler(Object entry) {
        if (entry instanceof org.neo4j.graphdb.Node) {
            this.addResultEntry(new Node((org.neo4j.graphdb.Node) entry));
        } else {
            this.addResultEntry(entry.toString());
        }
    }

    public void addResultEntry(Node entry)
    {
        if (null != this.target && null == entry.type) {
            entry.type = this.target;
        }

        this.addResultEntry(entry.toJsonObject());
    }

    public void addResultEntry(String entry) { this.addEntry(entry); }

    public void addResultEntry(JSONObject entry) { this.addEntry(entry); }
}
