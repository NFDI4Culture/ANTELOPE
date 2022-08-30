package org.tib.osl.annotationservice.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.tib.osl.annotationservice.web.api.AnnotationApiDelegate;
import org.tib.osl.annotationservice.web.api.StatusApiDelegate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AnnotationService implements StatusApiDelegate, AnnotationApiDelegate {
    private Logger log = LoggerFactory.getLogger(AnnotationService.class);

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return StatusApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("Service is running", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getWikiDataAnnotation(List<String> requestBody) {
        
        // get Entity Recognition from Falcon API
        List<String> falconResults;
        try {
            falconResults = getFalconResults(requestBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request Falcon API", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        // get all superclasses for the falcon entities
        Map<String, String> wikidataResultsByEntity = new HashMap<>();
        try {
            wikidataResultsByEntity = getWikiDataClasses(falconResults);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request WikiData API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // build hierarchy
        JSONObject finalResult = null;
        try {
            finalResult = combineFalconAndWikidataResults(falconResults, wikidataResultsByEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to build category tree", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // return result
        return new ResponseEntity<String>( finalResult.toString(), HttpStatus.OK );
    }

    /**
     * connects to the Falcon web API and retrieve results for every text in requestText
     * @param requestText list of texts, used for entity recognition with falcon
     * @return falcon specific json result containing result arrays under the keys "entities_wikidata" and "relations_wikidata"
     * @throws Exception
     */
    protected List<String> getFalconResults( List<String> requestText) throws Exception {
        List<String> falconResults = new ArrayList<>();
       
        // init connection to falcon api
        for( String actText : requestText) {
            String result = "";
            HttpPost post = new HttpPost(new URI("https://labs.tib.eu/falcon/falcon2/api?mode=long&db=1&k=10"));
            post.addHeader("content-type", "application/json");

            StringBuilder json = new StringBuilder();
            json.append("{");     
            json.append("\"text\":\""+actText+"\"");
            json.append("}");

            
            // send a JSON data
            post.setEntity(new StringEntity(json.toString()));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {

                result = EntityUtils.toString(response.getEntity());
                falconResults.add(result);
            }
        }
        System.out.println("falconResult:"+falconResults);
        return falconResults;
    }



    /**
     * connecting the wikidata sparql web api. get all superclasses for every recognized entity within the falconResult parameter List
     * @param falconResults Every element contains a json object in falcon specific format. one json object per requested entity
     * @return map of wikidata results, grouped by entity name in wikidata specific json format
     * @throws Exception
     */
    protected Map<String, String> getWikiDataClasses( List<String> falconResults ) throws Exception {
        Map<String, String> wikidataResultsByEntity = new HashMap<>();
        List<JSONArray> falconResultsToProcess = new ArrayList<>();
        
        try {
            // get superclass triples from wikiData for each entity that falcon delivered (get class tree)
            for( String actFalconResult : falconResults) {
                JSONObject obj = new JSONObject(actFalconResult);
                if( obj.has("entities_wikidata")) {
                    JSONArray entities = obj.getJSONArray("entities_wikidata");
                    falconResultsToProcess.add(entities);
                }
                if( obj.has("entities_dbpedia")) {
                    JSONArray entities = obj.getJSONArray("entities_dbpedia");
                    falconResultsToProcess.add(entities);
                }
                if( obj.has("relations_wikidata")) {
                    JSONArray relations = obj.getJSONArray("relations_wikidata");
                    falconResultsToProcess.add(relations);
                }
                if( obj.has("relations_dbpedia")) {
                    JSONArray relations = obj.getJSONArray("relations_dbpedia");
                    falconResultsToProcess.add(relations);
                }
                for( JSONArray objects : falconResultsToProcess) {
                    for( Object actObject : objects) {
                        
                        //String entityLabel = ((JSONArray)actEntity).get(0).toString();
                        String url = ((JSONArray)actObject).get(1).toString();
                        String[] urlParts = url.replace(">", "").split("/");
                        String objId = urlParts[ urlParts.length-1 ];
                        // init connection to wikiData api
                        String result = "";
                        String sparqlQuery = "SELECT ?class ?classLabel ?superclass ?superclassLabel WHERE { wd:"+objId+" wdt:P31*/wdt:P279* ?class. ?class wdt:P31/wdt:P279 ?superclass. SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } } ";
                        //String sparqlQuery = "SELECT ?class ?classLabel ?superclass ?superclassLabel WHERE { wd:"+entityId+" wdt:P31/wdt:P279* ?class. ?class wdt:P31/wdt:P279 ?superclass. SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } } ";
                        HttpGet get = new HttpGet(new URI("https://query.wikidata.org/sparql?format=json&query="+URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8)+""));
                        //get.addHeader("content-type", "application/json");
                        //System.out.println(sparqlQuery);
                        result = null;
                        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                            CloseableHttpResponse response = httpClient.execute(get)) {

                            result = EntityUtils.toString(response.getEntity());
                            wikidataResultsByEntity.put(actObject.toString(), result);
                            //System.out.println( "Entity:"+actEntity+" result:"+ result );
                            
                        } catch ( Exception e) {
                            log.warn( "unable to receive wikiData Classes for '"+actObject.toString()+"' error: "+e.getMessage() );
                        }
                        
                        
                    }
                } 
            } 
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println( "error in getWikiDataClasses:" + e.getMessage() );
        }    
        return wikidataResultsByEntity;
    }

     /**
     * connecting the wikidata sparql web api. get all superclasses for every recognized entity within the falconResult parameter List
     * @param falconResults Every element contains a json object in falcon specific format. one json object per requested entity
     * @return map of wikidata results, grouped by entity name in wikidata specific json format
     * @throws Exception
     */
    //FIXME: mockup method
    protected Map<String, String> getDbPediaClasses( List<String> falconResults ) throws Exception {
        Map<String, String> dbpediaResultsByEntity = new HashMap<>();
        List<JSONArray> falconResultsToProcess = new ArrayList<>();
        
        try {
            // get superclass triples from wikiData for each entity that falcon delivered (get class tree)
            for( String actFalconResult : falconResults) {
                JSONObject obj = new JSONObject(actFalconResult);
                if( obj.has("entities_dbpedia")) {
                    JSONArray entities = obj.getJSONArray("entities_dbpedia");
                    falconResultsToProcess.add(entities);
                }
                if( obj.has("relations_dbpedia")) {
                    JSONArray relations = obj.getJSONArray("relations_dbpedia");
                    falconResultsToProcess.add(relations);
                }
                for( JSONArray objects : falconResultsToProcess) {
                    for( Object actObject : objects) {
                        
                        //String entityLabel = ((JSONArray)actEntity).get(0).toString();
                        String url = ((JSONArray)actObject).get(1).toString();
                        String[] urlParts = url.replace(">", "").split("/");
                        String objId = urlParts[ urlParts.length-1 ];
                        // init connection to wikiData api
                        String result = "";
                        String sparqlQuery = "SELECT ?class ?classLabel ?superclass ?superclassLabel WHERE { wd:"+objId+" wdt:P31*/wdt:P279* ?class. ?class wdt:P31/wdt:P279 ?superclass. SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } } ";
                        //String sparqlQuery = "SELECT ?class ?classLabel ?superclass ?superclassLabel WHERE { wd:"+entityId+" wdt:P31/wdt:P279* ?class. ?class wdt:P31/wdt:P279 ?superclass. SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } } ";
                        HttpGet get = new HttpGet(new URI("https://query.wikidata.org/sparql?format=json&query="+URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8)+""));
                        //get.addHeader("content-type", "application/json");
                        //System.out.println(sparqlQuery);
                        result = null;
                        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                            CloseableHttpResponse response = httpClient.execute(get)) {

                            result = EntityUtils.toString(response.getEntity());
                            dbpediaResultsByEntity.put(actObject.toString(), result);
                            //System.out.println( "Entity:"+actEntity+" result:"+ result );
                            
                        } catch ( Exception e) {
                            log.warn( "unable to receive wikiData Classes for '"+actObject.toString()+"' error: "+e.getMessage() );
                        }
                        
                        
                    }
                } 
            } 
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println( "error in getWikiDataClasses:" + e.getMessage() );
        }    
        return dbpediaResultsByEntity;
    }

    /**
     * combine results from falcon and wikidata and build a category tree datastructure. 
     * @param falconResults
     * @param wikidataResultsByEntity 
     * @return jsonobject with the keys "entities", "relations" and "hierarchy"
     * @throws Exception
     */
    private JSONObject combineFalconAndWikidataResults(List<String> falconResults, Map<String,String> wikidataResultsByEntity) throws Exception {
    
        // combine falcon and Wikidata Results
        JSONObject finalResult = new JSONObject();

        //log.debug( wikidataResultsByEntity.toString() );
        // add all entities from falcon
        JSONArray entitiesArr = new JSONArray();
        for (String actFalconResult : falconResults){
            JSONObject actFalconJson = new JSONObject( actFalconResult );
            JSONArray falconEntities = actFalconJson.optJSONArray("entities_wikidata");
            entitiesArr.putAll( falconEntities );
        }
        finalResult.put("entities", entitiesArr);

        // add all relations from falcon
        JSONArray relationsArr = new JSONArray();
        for (String actFalconResult : falconResults){
            JSONObject actFalconJson = new JSONObject( actFalconResult );
            JSONArray falconRels = actFalconJson.optJSONArray("relations_wikidata");
            relationsArr.putAll( falconRels );
        }
        finalResult.put("relations", relationsArr);

        // build hierarchy from Wikidata
        JSONObject hierarchy = buildCategoryTree( wikidataResultsByEntity );
        finalResult.put( "hierarchy", hierarchy );

        return finalResult;
    }

    private JSONObject buildCategoryTree(Map<String,String> wikidataResultsByEntity) {

        Map<String, JSONArray> hierarchyMap = new TreeMap<>(); // key: entity, result: a childClass -> Superclass entry
        Set<String> childClasses = new HashSet<>();
        Set<String> superClasses = new HashSet<>();
        Set<String> rootClasses = new HashSet<>();


        for( Map.Entry<String, String> entry : wikidataResultsByEntity.entrySet()) {
            String actEntity = entry.getKey();
            String actResult = entry.getValue();
            JSONObject actWDJson = new JSONObject( actResult );
            JSONArray wdHierarchy = actWDJson.getJSONObject("results").optJSONArray("bindings") ;
            //System.out.println( wdHierarchy );
            // add results for act entity
            hierarchyMap.put(actEntity, wdHierarchy);   
        
            // extract and save childclasses and superclasses from act hierarchy
            for ( int i = 0; i < wdHierarchy.length(); i++ ) {
                JSONObject actObj = wdHierarchy.getJSONObject(i);
                String childClassJson = actObj.get("class").toString();
                String superClassJson = actObj.get("superclass").toString();
                
                childClasses.add( childClassJson );
                superClasses.add( superClassJson );
            }
        }

        // identify root childclasses
        for( String actChildClass : childClasses) {
            rootClasses.add( actChildClass );
            for( Map.Entry<String, String> entry : wikidataResultsByEntity.entrySet()) {
                String actEntity = entry.getKey();
                String actResult = entry.getValue();
                JSONObject actWDJson = new JSONObject( actResult );
                JSONArray wdHierarchy = actWDJson.getJSONObject("results").optJSONArray("bindings") ;
                for ( int i = 0; i < wdHierarchy.length(); i++ ) {
                    JSONObject actObj = wdHierarchy.getJSONObject(i);
                    String superClassJson = actObj.get("superclass").toString();

                    // actChildClass cannot be a root class, if it is registered here as a superclass
                    if( superClassJson.equals( actChildClass ) ) {
                        if( rootClasses.contains(actChildClass) ) {
                            rootClasses.remove( actChildClass );
                        }
                    }
                }
            }
        }

        // add Entries for root Childclasses (connect to common "root" node)
        JSONObject rootNode = new JSONObject();
        rootNode.put("type", "uri");
        rootNode.put("value", "");

        JSONObject rootNodeLabel = new JSONObject();
        rootNodeLabel.put("xml:lang", "en");
        rootNodeLabel.put("type", "literal");
        rootNodeLabel.put("value", "");

        for( String actRootClass : rootClasses ) {
            for( Map.Entry<String, String> entry : wikidataResultsByEntity.entrySet()) {
                JSONArray actEntity = new JSONArray(entry.getKey());
                String actResult = entry.getValue();
                JSONObject actWDJson = new JSONObject( actResult );
                JSONArray wdHierarchy = actWDJson.getJSONObject("results").optJSONArray("bindings") ;

                // Create Entries for Entity Node for later use
                JSONObject entityNode = new JSONObject();
                entityNode.put("type", "uri");
                String link = actEntity.getString(1);
                entityNode.put("value", link);

                JSONObject entityNodeLabel = new JSONObject();
                entityNodeLabel.put("xml:lang", "en");
                entityNodeLabel.put("type", "literal");
                entityNodeLabel.put("value", actEntity.get(0));

                for ( int i = 0; i < wdHierarchy.length(); i++ ) {
                    JSONObject actObj = wdHierarchy.getJSONObject(i);
                    
                    JSONObject classNodeLabel = actObj.getJSONObject("classLabel");
                    JSONObject classNode = actObj.getJSONObject("class");

                    // if this entry has the actClass as childClass. get all the childclass infos from it to create new link to the root
                    // the new connection will be: rootNode ---> entityNode ---> classNode
                    if( classNode.toString().equals( actRootClass ) ) {
                        JSONArray hierarchyEntries = hierarchyMap.get( actEntity.toString() );

                        // first create Entry for Link root --> Entity
                        JSONObject rootToEntity = new JSONObject();
                        rootToEntity.put("superclassLabel", entityNodeLabel);
                        rootToEntity.put("superclass", entityNode);
                        rootToEntity.put("classLabel", rootNodeLabel);
                        rootToEntity.put("class", rootNode);
                        
                        hierarchyEntries.put(rootToEntity);


                        // now create Entry for Link Entity --> classNode
                        JSONObject entityToClass = new JSONObject();
                        entityToClass.put("superclassLabel", classNodeLabel);
                        entityToClass.put("superclass", classNode);
                        entityToClass.put("classLabel", entityNodeLabel);
                        entityToClass.put("class", entityNode);
                        hierarchyEntries.put(entityToClass);
                        //System.out.println( "add: "+newEntry.toString() );
                    }
                }
            }
        }

        // eliminate duplicates
        TreeSet<JSONObject> noDuplicates = new TreeSet<>( 
            new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject s1, JSONObject s2) {
                    return s1.toString().compareTo(s2.toString());
                }
        });
        for( Map.Entry<String, JSONArray> actEntry : hierarchyMap.entrySet() ) {
            //JSONArray entryArr = new JSONArray(actEntry.getKey());
            //String entityName = entryArr.getString(0);
            //String entityUri = entryArr.getString(1);
            JSONArray hierarchyEntries = actEntry.getValue();
            for( Object o : hierarchyEntries ) {
                noDuplicates.add((JSONObject)o);
            }
            JSONArray newHierarchy = new JSONArray();
            newHierarchy.putAll( noDuplicates );
            hierarchyMap.put(actEntry.getKey(), newHierarchy);
            
        }

        //now all links are created in tabular format. create hierarchy in json tree format
        JSONObject result = createTreeStructureFromEntityToSuperclass( hierarchyMap );
        return result;
    }

    /**
     * 
     * @param hierarchyMap tabular struture (all links of a tree)
     * @return json Tree structure that can be used in d3.hierarchy method:
     * {
        name: "root",
        children: [
            {name: "child #1"},
            {
            name: "child #2",
            children: [
                {name: "grandchild #1"},
                {name: "grandchild #2"},
                {name: "grandchild #3"}
            ]
            }
        ]
        }
     */
    private JSONObject createTreeStructureFromEntityToSuperclass(Map<String, JSONArray> hierarchyMap) {
        
        JSONObject rootNode = new JSONObject();
        rootNode.put("id", "0");
        rootNode.put("name", "results");
        rootNode.put("link", "");
        rootNode.put("children", new JSONArray());

        // for each requested entity, build a tree and add it to the result
        int startId = 1;
        Map<String, JSONArray> newHierarchyMap = new HashMap<>();
        for( Map.Entry<String, JSONArray> actEntry : hierarchyMap.entrySet() ) {
            JSONArray arr = new JSONArray(actEntry.getKey());
            String entityName = arr.getString(0);
            String entityUri = arr.getString(1);
            entityUri = entityUri.replace("<", "").replace(">","");

            // save hierarchy again under entityName
            newHierarchyMap.put(entityName, actEntry.getValue());

            JSONObject childNode = new JSONObject();
            childNode.put("id", ""+startId);
            childNode.put("name", entityName);
            childNode.put("link", entityUri);
            childNode.put("children", new JSONArray());

            rootNode.getJSONArray("children").put(childNode);

            startId += 10000; // define a wide id range for each entity class tree
        }

        for( Object o : rootNode.getJSONArray("children")) {
            JSONObject actChild = (JSONObject)o;
            addChildTree(rootNode, actChild, newHierarchyMap.get(actChild.getString("name")));
        }
        
        return rootNode;
    }

    private void addChildTree(JSONObject parentTree, JSONObject startNode, JSONArray hierarchyEntries) {
        int actId = startNode.getInt("id");
        String startNodeUri = startNode.getString("link");
        
        
        System.out.println( "call getTree() for startNode: "+startNodeUri );
        //System.out.println( hierarchyEntries );
        // get all nodes, that have the startNodeUri as childclass (key "class")
        List<JSONObject> hierarchyEntriesOfStartNode = new ArrayList<>();
        for( Object o : hierarchyEntries ) {
            JSONObject j = (JSONObject)o;
            
            String actNodeName = j.getJSONObject("classLabel").getString("value");
            String actNodeUri = j.getJSONObject("class").getString("value");
            String actSuperClassUri = j.getJSONObject("superclass").getString("value");
            String actSuperClassName = j.getJSONObject("superclassLabel").getString("value");
            if( actNodeUri.equals(startNodeUri) ) {
                System.out.println("found child entry: "+actNodeName+" ("+actNodeUri+") --> "+actSuperClassName+" ("+actSuperClassUri+")");
                hierarchyEntriesOfStartNode.add( j );
            }
        }
        
        // add children for resultNode
        for( JSONObject j : hierarchyEntriesOfStartNode) {
            
            String actClassUri = j.getJSONObject("class").getString("value");
            String actSuperClassUri = j.getJSONObject("superclass").getString("value");
            String actSuperClassName = j.getJSONObject("superclassLabel").getString("value");
            System.out.println("get subtree for startNode: "+actSuperClassName+" ("+actSuperClassUri+")");
            actId++;
            int maxNodeCount = 1000;
            if( actId > maxNodeCount) {
                //log.info("max Number of Nodes reached ("+maxNodeCount+") skip further nodes processing. Results will not be displayed!");
               // continue;
            }
            // handle reflexivity: check if class and superclass are not equal to avoid infinite recursion(circle) 
            System.out.println("check for equality: "+actClassUri+" and "+actSuperClassUri);
            if( actClassUri.equals( actSuperClassUri ) ) {
                log.info("act node is equal to superclass node. skip!");
                continue;
            }

            //create new Node for current child
            JSONObject childNode = new JSONObject();
            childNode.put("id", ""+actId);
            childNode.put("name", actSuperClassName);
            childNode.put("link", actSuperClassUri);
            childNode.put("children", new JSONArray());
            
            // check if the act ChildNode uri already exist in the parent tree (if so, dont add the child)
            String childAlreadyExistSequence = "\"link\":\""+actSuperClassUri+"\"";
            //System.out.println( childAlreadyExistSequence );
            if( !parentTree.toString().contains( childAlreadyExistSequence )) {
                log.debug("put "+childNode.get("name")+" as child of "+startNode.getString("name"));
                startNode.getJSONArray("children").put(childNode);
                addChildTree( parentTree, childNode, hierarchyEntries );
                
            } else {
                System.out.println( "Child already exist, skip: "+actSuperClassName+" - "+actSuperClassUri );
            }
            
            

        }
        
    }

    

    
   

}


