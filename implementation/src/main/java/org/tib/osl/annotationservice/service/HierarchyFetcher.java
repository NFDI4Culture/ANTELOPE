package org.tib.osl.annotationservice.service;

import java.util.Map;
import org.json.JSONObject;

public abstract class HierarchyFetcher implements Runnable {
    protected Map<String, String> resultsByEntity;
    protected JSONObject entitiesToProcess;

    public HierarchyFetcher(Map<String, String> resultContainer, JSONObject entitiesToProcess) {
        resultsByEntity = resultContainer;
        this.entitiesToProcess = entitiesToProcess;
    }    
}
