package bsuapi.dbal.script;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.resource.Util;
import bsuapi.service.ScriptOverseer;
import bsuapi.service.ScriptStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CypherScriptFile extends CypherScriptAbstract
{
    public static CypherScriptFile instance(Cypher c, CypherScript script)
    {
        ScriptStatus existing = ScriptOverseer.get(script.name());
        if (existing instanceof CypherScriptFile) {
            return (CypherScriptFile) existing;
        }

        CypherScriptFile created = new CypherScriptFile(c, script);
        ScriptOverseer.ready(script.name(), created);
        return created;
    }

    protected ArrayList<CypherQuery> commandLoader()
    throws Exception
    {
        ArrayList<CypherQuery> commands = new ArrayList<>();
        String sourceFileData = Util.readResourceFile(this.script.filename());
        for (String cmd : sourceFileData.trim().split(";")) {
            cmd = cmd.trim();
            if (cmd.length() < 5) {continue;}
            commands.add(new CypherScriptCommandSingle(cmd));
        }

        return commands;
    }

    protected CypherScriptFile(Cypher c, CypherScript script)
    {
        this.boot(c, script);
    }

    public void handleCommandResult(CypherQuery command)
    throws CypherException
    {
        this.results.put(Util.jsonArrayFirst(command.exec(this.c)));
        this.countCompleted++;
    }
}
