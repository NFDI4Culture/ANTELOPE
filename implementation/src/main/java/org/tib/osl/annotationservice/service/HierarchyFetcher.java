package org.tib.osl.annotationservice.service;

import java.util.Map;
import org.json.JSONObject;

public abstract class HierarchyFetcher implements Runnable {
    protected Map<String, String> resultsByEntity;
    protected JSONObject falconResultsToProcess;

    public HierarchyFetcher(Map<String, String> resultContainer, JSONObject falconResultsToProcess) {
        resultsByEntity = resultContainer;
        this.falconResultsToProcess = falconResultsToProcess;
    }    
}
