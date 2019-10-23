package bsuapi.resource;

import bsuapi.behavior.Behavior;
import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Topic;
import org.json.JSONObject;

import java.util.Map;

public class Response {
    private Request q;
    private Map<String, String> params;

    public static Response prepare(Request q)
    {
        Response r = new Response();
        r.q = q;
        r.params = q.getQueryParameters();
        return r;
    }

    public String getParam(String key)
    {
        if (!this.params.containsKey(key)) {
            return null;
        }

        return this.params.get(key);
    }

    private JSONObject appendToken(JSONObject data)
    {
        String requestToken = this.params.getOrDefault("requestToken", null);
        if (null != requestToken) {
            data.put("requestToken", requestToken);
        }

        return data;
    }

    private JSONObject buildResponse(boolean success, String message)
    {
        JSONObject responseObject = JsonResponse.responseObject(success, message);
        return this.appendToken(responseObject);
    }

    public javax.ws.rs.core.Response plain (JSONObject data)
    {
        return JsonResponse.OK(this.appendToken(data));
    }

    public javax.ws.rs.core.Response data (JSONObject data, String message)
    {
        JSONObject res = this.buildResponse(true, message);
        res.put("data", data);
        return JsonResponse.OK(res);
    }

    public javax.ws.rs.core.Response exception (Exception e)
    {
        JSONObject res = this.buildResponse(false, e.getMessage());
        res.put("data", e.toString());
        res.put("stack", JsonResponse.exceptionStack(e));
        return JsonResponse.SERVER_ERROR(res);
    }

    public javax.ws.rs.core.Response badRequest (String reason)
    {
        JSONObject res = this.buildResponse(false, reason);
        return JsonResponse.NOT_ACCEPTABLE(res);
    }

    public javax.ws.rs.core.Response notFound (String message)
    {
        JSONObject res = this.buildResponse(false, message);
        return JsonResponse.NOT_FOUND(res);
    }

    public javax.ws.rs.core.Response notFound ()
    {
        return this.notFound("Requested resource not found. No matching indexed node for that value. Isn't art an emotional abstraction? Just imagine it.");
    }

    public javax.ws.rs.core.Response behavior(BehaviorType behaviorType, Topic t, Cypher c)
    throws CypherException
    {
        Behavior b = behaviorType.compose(t, c, this.params);

        if (null == b) {
            return this.notFound("Could not resolve related behavior.");
        }

        return this.data(b.toJson(), b.getMessage());
    }

    public String buildUri(String path)
    {
        return q.getBaseUri() + path;
    }
}
