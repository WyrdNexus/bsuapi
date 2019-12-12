package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;

public class TopicTopFiltered extends CypherQuery {

    protected String filterField, filterValue;

    /**
     * 1: Topic label
     * 2: label to use for rel count ":Topic"
     * 3: artwork filter field
     * 4:         filter field value
     */
    protected static String query =
        "MATCH (x:Artwork) WHERE x.%3$s=\"%4$s\" " +
        "MATCH p=(x)-[]->("+ CypherQuery.resultColumn +":%1$s)-[]->(:%2$s) " +
        "WITH "+ CypherQuery.resultColumn +", count(p) as n " +
        "RETURN "+ CypherQuery.resultColumn +" ORDER BY n DESC "
        ;

    public TopicTopFiltered(NodeType target, String field, String value) {
        this.filterField = field;
        this.filterValue = value;
        this.target = target;
        this.initQuery = query;
        this.resultQuery = query;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.target.labelName(),
            NodeType.TOPIC.labelName(),
            this.filterField,
            this.filterValue
        ) + this.getPageLimitCmd();
    }
}
