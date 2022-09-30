package org.tib.osl.annotationservice.service;

import java.util.Map;

import org.json.JSONArray;

public abstract class HierarchyFetcher implements Runnable {
    protected Map<String, String> resultsByEntity;
    protected JSONArray falconResultsToProcess;

    public HierarchyFetcher(Map<String, String> resultContainer, JSONArray falconResultsToProcess) {
        resultsByEntity = resultContainer;
        this.falconResultsToProcess = falconResultsToProcess;
    }    
}
