package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import bsuapi.dbal.Node;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicSharedRelations;
import org.json.JSONArray;
import org.json.JSONObject;

public class Related extends Behavior
{
    private JSONObject related;

    public Related(Topic topic) { super(topic); }

    @Override
    public String getBehaviorKey() { return "related"; }

    @Override
    public Object getBehaviorData() { return this.related; }

    @Override
    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        this.related = new JSONObject();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                this.related.put(n.labelName(), TopicSharedRelations.params(topic, n).exec(cypher));
            }
        }
        super.resolveBehavior(cypher);
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/related/{TOPIC}/{VALUE}",
            "Find all (TOPIC)s with an indexed value matching (VALUE), along with a" +
            "collection of closely related Topics, and a collection of Artwork which references that Topic. "
        );

        desc.arg("topic", "All lowercase, a-z. Search all topics: 'topic'");
        desc.arg("value", "URL-encoded string. Must start with a letter, a-zA-Z.");

        return desc;
    }


}
