package org.tib.osl.annotationservice.service;



import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchyFetcherLobidGND extends HierarchyFetcher{
    
    private static Logger log = LoggerFactory.getLogger(HierarchyFetcherOLS.class);

    public HierarchyFetcherLobidGND(Map<String, String> resultContainer, JSONObject lobidGndEntityToProcess) {
        super(resultContainer, lobidGndEntityToProcess);
    }    


    @Override
    public void run() {
        
        log.debug( "GND_fetch:");
        try {
            JSONArray resultArr = new JSONArray();
            //super.entitiesToProcess.put("label", super.entitiesToProcess.getString("label"));
            getParentClasses( super.entitiesToProcess.getJSONObject("obj"), resultArr );
        
        
        
            super.resultsByEntity.put( super.entitiesToProcess.toString(), resultArr.toString());
            System.out.println(resultsByEntity);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
       
    private void getParentClasses(JSONObject entity, JSONArray result) {
        log.debug( "getparentCLasses");
        
        //FIXME: process also instances
        boolean isSubjectHeading = false;
        JSONArray entityTypes = entity.optJSONArray("type");
        if( entityTypes != null) {
            for( int i = 0; i< entityTypes.length(); i++) {
                String actType = entityTypes.getString(i);
                if( actType.contains("SubjectHeading") ) {
                    isSubjectHeading = true;
                }
            }
        }
        if (!isSubjectHeading) {
            // log.debug( "skip GND entity, no subject heading" );
            //return;
        }
        String entityId = entity.getString("gndIdentifier");
        String entityLabel = entity.getString("preferredName");
        //if( entitylabel == preferredNameForTheSubjectHeading)
        String entityUrl = entity.getString("id");

        JSONArray parentEntitiesJsonArr = new JSONArray();
        if ( entity.has( "broaderTerm" )) {
            parentEntitiesJsonArr.putAll(entity.optJSONArray("broaderTerm"));
        }
        
        if ( entity.has( "broaderTermGeneral" )) {
            parentEntitiesJsonArr.putAll(entity.optJSONArray("broaderTermGeneral"));
        }

        if ( entity.has( "broaderTermInstantial" )) {
            parentEntitiesJsonArr.putAll(entity.optJSONArray("broaderTermInstantial"));
        }

        if ( entity.has( "broaderTermGeneric" )) {
            parentEntitiesJsonArr.putAll(entity.optJSONArray("broaderTermGeneric"));
        }

        if ( entity.has( "broaderTermPartitive" )) {
            parentEntitiesJsonArr.putAll(entity.optJSONArray("broaderTermPartitive"));
        }
        
        if ( entity.has( "broaderTermWithMoreThanOneElement" )) {
            parentEntitiesJsonArr.putAll(entity.optJSONArray("broaderTermWithMoreThanOneElement"));
        }

        if ( entity.has( "topic" )) {
            parentEntitiesJsonArr.putAll(entity.optJSONArray("topic"));
        }

        if ( entity.has( "type" )) {
           // parentEntitiesJsonArr.putAll(entity.optJSONArray("type"));
        }

        // add subjectCategory as superclass node (but don't further evolve it as SCs have no further hiearchy)
        if ( entity.has( "gndSubjectCategory" )) { 
            JSONArray parentCategories = entity.optJSONArray("gndSubjectCategory");
            for(int i = 0; i< parentCategories.length(); i++) {
                JSONObject actParentCategory = parentCategories.getJSONObject(i);
                JSONObject obj = new JSONObject();
                obj.put("class", entityUrl);
                obj.put("superclass", actParentCategory.getString("id"));
                obj.put("classLabel", isSubjectHeading ? "SH:" + entityLabel : entityLabel);
                obj.put("superclassLabel", "SC:" + actParentCategory.getString("label"));
                result.put(obj);
            }
            
        }
      
        
        
        for( int i=0; i<parentEntitiesJsonArr.length(); i++){ 
            log.debug(parentEntitiesJsonArr.get(i).toString());
            JSONObject actParent = parentEntitiesJsonArr.getJSONObject(i);
            String url = actParent.getString("id");
            String[] urlParts = url.split("/");
            String actParentEntityId = urlParts[urlParts.length-1];
            url = "https://lobid.org/gnd/"+ URLEncoder.encode(actParentEntityId, StandardCharsets.UTF_8)+".json";
            HttpGet get = null;
            try {
                get = new HttpGet(new URI(url));
            } catch (Exception e) {
                log.error( "unable init lobid-gnd url error: "+e.getMessage() );
            }
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(get)) {

                String responseStr = EntityUtils.toString(response.getEntity());
                JSONObject actParentEntity = new JSONObject(responseStr);
                // log.debug(actParentEntity.toString());

                boolean isActParentSubjectHeading = false;
                JSONArray parentEntityTypes = actParentEntity.optJSONArray("type");
                if( parentEntityTypes != null) {
                    for( int j = 0; j< parentEntityTypes.length(); j++) {
                        String actType = parentEntityTypes.getString(j);
                        if( actType.contains("SubjectHeading") ) {
                            isActParentSubjectHeading = true;
                        }
                    }
                }
                String parentEnitityType = ""; // super.entitiesToProcess.getString("type");
                String parentEntityId = actParentEntity.getString("gndIdentifier");
                String parentEntityLabel = actParentEntity.getString("preferredName");
                String parentEntityUrl = actParentEntity.getString("id");

                // create a new json object to add to results
                JSONObject obj = new JSONObject();
                obj.put("class", entityUrl);
                obj.put("superclass", parentEntityUrl);
                obj.put("classLabel", isSubjectHeading ? "SH:" + entityLabel : entityLabel);
                obj.put("superclassLabel", isActParentSubjectHeading ? "SH:" + parentEntityLabel : parentEntityLabel);

                // add json object to result json array
                result.put(obj);
                
                 // Create a new JSONArray instance for each recursive call
                JSONArray subResult = new JSONArray();
                getParentClasses(actParentEntity, subResult);

                // Add the subResult to the current level's result array
                result.putAll(subResult);
            }catch ( Exception e) {
            log.error( "unable to receive ols Classes for '"+super.entitiesToProcess.toString()+"' error: "+e.getMessage() );
            
            e.printStackTrace();
        }
            
            log.debug("ols result fetched sucessfully");
        }
    
    }

    
    
}
