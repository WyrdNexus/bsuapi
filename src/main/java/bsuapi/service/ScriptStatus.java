package bsuapi.service;

import org.json.JSONObject;

import java.time.Duration;

public interface ScriptStatus {
    public boolean isReady();
    public boolean isRunning();
    public boolean isComplete();
    public int countActionsTotal();
    public int countActionsComplete();
    public Duration runtime();
    public String toString();
    public JSONObject statusReport();
    public void end();
}
