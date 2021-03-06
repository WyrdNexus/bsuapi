package bsuapi.resource;

import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.Cypher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/topic-assets")
public class TopicAssetsResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    @Path("/{topic: [a-z]*}/{value}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiAssets(
        @PathParam("topic") String topic,
        @PathParam("value") String value,
        @Context UriInfo uriInfo
    ){
        Response response = this.prepareResponse(uriInfo);
        String searchVal = URLCoder.decode(value);
        String searchTopic = topic.substring(0, 1).toUpperCase() + topic.substring(1); // upper first
        response.setTopic(searchTopic, searchVal);

        if (searchVal == null)
        {
            return response.badRequest("Required method parameters missing: topic label, topic name");
        }

        return this.handleBehavior(BehaviorType.ASSETS);
    }
}
