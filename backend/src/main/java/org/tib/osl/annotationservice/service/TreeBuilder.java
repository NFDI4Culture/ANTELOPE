package org.tib.osl.annotationservice.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeBuilder {
    private static Logger log = LoggerFactory.getLogger(TreeBuilder.class);

    /**
     * take the result of a NER (named entity recognition) process and create a hierarchy tree from it.
     * expects the json result format by entity according to the falcon2 resultset
     * @param wikidataResultsByEntity
     * @param allowDuplicates set true to allow the occurence of an entities superclass multiple times within the hierarchy tree.
     * @return
     */
    public static JSONObject buildCategoryTree(Map<String,String> nerResultsByEntity, int rootNodeId, String rootNodeName, String rootNodeLink, String keyOfEntityNameInJson, String keyOfEntityUrlInJson, boolean allowDuplicates) {

        Map<String, JSONArray> hierarchyMap = new TreeMap<>(); // key: entity, result: a childClass -> Superclass entry
        Set<String> childClasses = new HashSet<>();
        Set<String> superClasses = new HashSet<>();
       
        // extract results and add to processing map by entity
        for( Map.Entry<String, String> entry : nerResultsByEntity.entrySet()) {
            String actEntity = entry.getKey();
            String actResult = entry.getValue();
            JSONArray wdHierarchy = new JSONArray( actResult );
            //JSONObject actWDJson = new JSONObject( actResult );
            //JSONArray wdHierarchy = actWDJson.getJSONObject("results").optJSONArray("bindings") ;
            hierarchyMap.put(actEntity, wdHierarchy);  
        }

        // identify childclasses and superclasses for later tree building
        sortChildAndSuperClasses(childClasses, superClasses, nerResultsByEntity);

        // identify root classes
        Set<String> rootClasses = identifyRootClasses(childClasses, nerResultsByEntity);

        // add Entries for root Childclasses (connect to common "root" node)
        insertRootNode(rootClasses, nerResultsByEntity, hierarchyMap, rootNodeName, rootNodeLink, keyOfEntityNameInJson, keyOfEntityUrlInJson);

        // eliminate duplicates (process every entity only once. this doesnt affect if an entity can occur multiple times within the hierarchy tree)
        eliminateDuplicates(hierarchyMap);
        
        
        //now all links are created in tabular format. create hierarchy in json tree format
        JSONObject result = createTreeStructureFromEntityToSuperclass( hierarchyMap, rootNodeId, rootNodeName, rootNodeLink, keyOfEntityNameInJson, keyOfEntityUrlInJson, allowDuplicates );
        return result;
    }
/*
 * add Entries for root Childclasses (connect to common "root" node)
 */
    //FIXME: nerResultsByEntity and hierarchyMap containing nearly the same information (nerResult... contains the links additionally)
    private static void insertRootNode(
        Set<String> rootClasses, 
        Map<String,String> nerResultsByEntity, 
        Map<String, JSONArray> hierarchyMap, 
        String rootNodeName, 
        String rootNodeLink, 
        String keyOfEntityNameInJson, 
        String keyOfEntityUrlInJson) {

        for( String actRootClass : rootClasses ) {
            for( Map.Entry<String, String> entry : nerResultsByEntity.entrySet()) {
                JSONObject actEntity = new JSONObject(entry.getKey());
                String actResult = entry.getValue();
                JSONArray wdHierarchy = new JSONArray( actResult );
                //JSONObject actWDJson = new JSONObject( actResult );
                //JSONArray wdHierarchy = actWDJson.getJSONObject("results").optJSONArray("bindings") ;

                // Create Entries for Entity Node for later use
                String entityNode = actEntity.getString(keyOfEntityUrlInJson);
                String entityNodeLabel = actEntity.getString(keyOfEntityNameInJson);

                for ( int i = 0; i < wdHierarchy.length(); i++ ) {
                    JSONObject actObj = wdHierarchy.getJSONObject(i);
                    
                    String classNodeLabel = actObj.getString("classLabel");
                    String classNode = actObj.getString("class");

                    // if this entry has the actClass as childClass. get all the childclass infos from it to create new link to the root
                    // the new connection will be: rootNode ---> entityNode ---> classNode
                    if( classNode.toString().equals( actRootClass ) ) {
                        JSONArray hierarchyEntries = hierarchyMap.get( actEntity.toString() );

                        // first create Entry for Link root --> Entity
                        JSONObject rootToEntity = new JSONObject();
                        rootToEntity.put("superclassLabel", entityNodeLabel);
                        rootToEntity.put("superclass", entityNode);
                        rootToEntity.put("classLabel", rootNodeName);
                        rootToEntity.put("class", rootNodeLink);
                        
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
    }

    private static void sortChildAndSuperClasses(Set<String> childClassesContainer, Set<String> superClassesContainer,  Map<String,String> nerResultsByEntity ) {
        for( Map.Entry<String, String> entry : nerResultsByEntity.entrySet()) {
            String actResult = entry.getValue();
            JSONArray wdHierarchy = new JSONArray( actResult );
            //JSONObject actWDJson = new JSONObject( actResult );
            //JSONArray wdHierarchy = actWDJson.getJSONObject("results").optJSONArray("bindings") ;
            //System.out.println( wdHierarchy );
            
        
            // extract and save childclasses and superclasses from act hierarchy
            for ( int i = 0; i < wdHierarchy.length(); i++ ) {
                JSONObject actObj = wdHierarchy.getJSONObject(i);
                String childClassJson = actObj.get("class").toString();
                String superClassJson = actObj.get("superclass").toString();
                
                childClassesContainer.add( childClassJson );
                superClassesContainer.add( superClassJson );
            }
        }
    }

    private static void eliminateDuplicates(Map<String, JSONArray> hierarchyMap) {
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
    }

    private static Set<String> identifyRootClasses(Set<String> childClasses, Map<String,String> nerResultsByEntity) {
        Set<String> rootClasses = new HashSet<>();
        // identify root childclasses
        for( String actChildClass : childClasses) {
            rootClasses.add( actChildClass );
            for( Map.Entry<String, String> entry : nerResultsByEntity.entrySet()) {
                //String actEntity = entry.getKey();
                String actResult = entry.getValue();
                JSONArray wdHierarchy = new JSONArray( actResult );
                //JSONObject actWDJson = new JSONObject( actResult );
                //JSONArray wdHierarchy = actWDJson.getJSONObject("results").optJSONArray("bindings") ;
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
        return rootClasses;
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
    private static JSONObject createTreeStructureFromEntityToSuperclass(Map<String, JSONArray> hierarchyMap, int rootId, String rootNodeName, String rootNodeLink, String keyOfEntityNameInJson, String keyOfEntityUrlInJson, boolean allowDuplicates ) {
        
        JSONObject rootNode = new JSONObject();
        rootNode.put("id", rootId);
        rootNode.put("name", rootNodeName);
        rootNode.put("link", rootNodeLink);
        rootNode.put("children", new JSONArray());

        // for each requested entity, build a tree and add it to the result
        int startId = rootId + 1;
        Map<String, JSONArray> newHierarchyMap = new HashMap<>();
        for( Map.Entry<String, JSONArray> actEntry : hierarchyMap.entrySet() ) {
            JSONObject obj = new JSONObject(actEntry.getKey());
            String entityName = obj.getString( keyOfEntityNameInJson );
            String entityUri = obj.getString( keyOfEntityUrlInJson );
            entityUri = entityUri.replace("<", "").replace(">","");

            // save hierarchy again under entityName
            newHierarchyMap.put(entityName, actEntry.getValue());

            JSONObject childNode = new JSONObject();
            childNode.put("id", ""+startId);
            childNode.put("name", entityName);
            childNode.put("link", entityUri);
            childNode.put("children", new JSONArray());

            rootNode.getJSONArray("children").put(childNode);

            startId += 1000; // define a wide id range for each entity class tree
        }

        for( Object o : rootNode.getJSONArray("children")) {
            JSONObject actChild = (JSONObject)o;
            addChildTree(rootNode, rootNode.getString("link"), actChild, newHierarchyMap.get(actChild.getString("name")), allowDuplicates);
        }
        
        return rootNode;
    }

    private static void addChildTree(JSONObject parentTree, String parentBranch, JSONObject startNode, JSONArray hierarchyEntries, boolean allowDuplicates) {
        int actId = startNode.getInt("id");
        String startNodeUri = startNode.getString("link");
        log.debug(parentBranch);
        
        System.out.println( "call getTree() for startNode: "+startNodeUri );
        //System.out.println( hierarchyEntries );
        // get all nodes, that have the startNodeUri as childclass (key "class")
        List<JSONObject> hierarchyEntriesOfStartNode = new ArrayList<>();
        for( Object o : hierarchyEntries ) {
            JSONObject j = (JSONObject)o;
            //System.out.println(j.toString());
            String actNodeName = j.getString("classLabel");
            String actNodeUri = j.getString("class");
            String actSuperClassUri = j.getString("superclass");
            String actSuperClassName = j.getString("superclassLabel");
            if( actNodeUri.equals(startNodeUri) ) {
                //System.out.println("found child entry: "+actNodeName+" ("+actNodeUri+") --> "+actSuperClassName+" ("+actSuperClassUri+")");
                hierarchyEntriesOfStartNode.add( j );
            }
        }
        
        // add children for resultNode
        for( JSONObject j : hierarchyEntriesOfStartNode) {
            
            String actClassUri = j.getString("class");
            String actSuperClassUri = j.getString("superclass");
            String actSuperClassName = j.getString("superclassLabel");
            //System.out.println("get subtree for startNode: "+actSuperClassName+" ("+actSuperClassUri+")");
            actId++;

            // handle reflexivity: check if class and superclass are not equal to avoid infinite recursion(circle) 
           //System.out.println("check for equality: "+actClassUri+" and "+actSuperClassUri);
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
            
            boolean skipNode = false;
            // check if child occurs in same branch (that would lead to infinite loop)

            if( allowDuplicates && parentBranch.contains( actSuperClassUri ) ){
                skipNode = true;
            } else if( !allowDuplicates){
                // check if the act ChildNode uri already exist in the parent tree (if so, dont add the child)
                String childAlreadyExistSequence = "\"link\":\""+actSuperClassUri+"\"";
                if( parentTree.toString().contains( childAlreadyExistSequence ) ){
                    skipNode = true;
                }

            }            //System.out.println( childAlreadyExistSequence );

            if( !skipNode ) {
                log.debug("put "+childNode.get("name")+" as child of "+startNode.getString("name"));
                startNode.getJSONArray("children").put(childNode);
                parentBranch += "-->"+actClassUri;
                addChildTree( parentTree, parentBranch, childNode, hierarchyEntries, allowDuplicates );
                
            } else {
                System.out.println( "Child already exist, skip: "+actSuperClassName+" - "+actSuperClassUri );
            }
        }
    }
}