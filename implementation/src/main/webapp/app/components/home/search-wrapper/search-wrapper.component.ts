import { Component,ElementRef, OnInit, ViewChild } from '@angular/core';
import { LoadingBarService } from '@ngx-loading-bar/core';
import { FormControl, FormArray, FormGroup, FormBuilder } from '@angular/forms';
import * as XLSX from 'xlsx';
import { IEntity } from 'app/interfaces/IEntity';
import { ResultsGraphTidytreeComponent } from 'app/components/home/results-graph-tidytree/results-graph-tidytree.component';
import { ResultsGraphBarchartComponent } from '../results-graph-barchart/results-graph-barchart.component';
import { ResultsTableComponent } from 'app/components/home/results-table/results-table.component'
import { EntitySelectService } from 'app/services/entity-select/entity-select.service';
import { HttpClient } from '@angular/common/http';
import { ResultsService } from 'app/services/results/results.service';
import { MatTabGroup } from '@angular/material/tabs';

const EXCEL_EXTENSION = '.xlsx';

// data model of the RESTful annotationService API result
type AnnotationResponse = {
  entities: IEntity[];
  relations: [];
  hierarchy: HierarchyTree;
};

// data model of the RESTful Vecner entity linking API result
type VecnerResponse = {
  json: string;
  html: string;
};

// data model of the RESTful annotationService API result
/* type ts4tibCollectionsResponse = {
  collections: [];
}; */

type ts4tibOntology = 
    {
    collections?: string[];
    label: string;
    paramValue: string;
    };

// data model for initialising the d3 tree graph
type HierarchyTree = {
  id: string;
  name: string;
  link: string;
  children: [];
}

@Component({
  selector: 'jhi-search-wrapper',
  templateUrl: './search-wrapper.component.html',
  styleUrls: ['./search-wrapper.component.scss'],
  providers: [ EntitySelectService ]
})
export class AnnotationServiceUIComponent implements OnInit {
  loader = this.loadingBar.useRef();
  textTerminologySearch = new FormControl('Vincent van Gogh');
  textEntityLinking = new FormControl('Vincent van Gogh was a dutch post-impressionist painter');
  el_user_dict_examples = [
    '"michelangelo", "van gogh"',
    '{"artist": ["michelangelo","van gogh"]}',
    '{"entity1": {"label":"artist", "patterns":["michelangelo", "van gogh"], "kb_id":"entity1", "kb_url":"entity1_url"}}'
  ];
  el_user_dict_list = new FormControl(this.el_user_dict_examples[0]);
  el_user_dict_simple = new FormControl(this.el_user_dict_examples[1]);
  el_user_dict_full = new FormControl(this.el_user_dict_examples[2]);
  selectedUserDictTabIndex = 0;
  selectedDictSourceTabIndex = 0;
  el_threshold = 1.0;
  image_el_threshold = 0.0;
  image_el_threshold_result_count = "";
  ts4tibOntologies = [
    {id: "NONE", name:"loading ontologies...", collection:"-"}
  ]
  iartImageModels = [
    {name:"loading image models...", type:"-"}
  ]

  initArray:FormControl[] = [];
  initArrayCollections:FormControl[] = [];
  selectedSources = new FormArray(this.initArray);
  selectedTs4tibOntologies= [];// [{name: 'All'}];
  selectedIartImageModels = "ClipClassification";
  imageUrl : string | null = "content/images/landscape.jpg";
  imageText = new FormControl('Die Vertreibung Heliodors aus dem Tempel');
  selectedFile: File | null = null;
  msg = "";
  err = "";
  
  showResultContainer = false;
  showTs4tibOntologySelect = false;
  allowDuplicates = false;
  resultLimit = 20; // service parameter. number of results, fetched per datasource
  resultCount = 0;
  
  @ViewChild('result_table') resultTableRef: ElementRef = {} as ElementRef;

  @ViewChild('resultTypeTabGroup', { static: false })
  resultTabGroup!: MatTabGroup;
  
  // create a FormGroup to select the datasources checkboxes state
  sourcesForm: FormGroup;

  // init, which datasources should be preselected in the checkbox group
  datasources: Array<any> = [
    { name: 'WIKIDATA', value: 'wikidata', checked: false, disabled: true, shownTS: true, shownER: false },
    { name: 'WIKIDATA + DBpedia', value: 'wikidata_dbpedia', checked: true, disabled: false, shownTS: true, shownER: false},
    { name: 'ICONCLASS', value: 'iconclass', checked: true, disabled: false , shownTS: true, shownER: true},
    { name: 'GND (Gemeinsame Normdatei)', value: 'gnd', checked: false, disabled: false, shownTS: true, shownER: false},
    { name: 'TIB Terminology Service', value: 'ts4tib', checked: false, disabled: false, shownTS: true, shownER: false}
  ];
  
  dropdownSettings = {};
  
  public annotation: AnnotationResponse = {entities:[], relations:[], hierarchy:{} as unknown as HierarchyTree};
  public el_result: VecnerResponse = {json:'', html:''};
  public iart_result = "";
  

  @ViewChild(ResultsGraphTidytreeComponent)
  private hierarchyGraph!: ResultsGraphTidytreeComponent;
  @ViewChild(ResultsTableComponent)
  private table!: ResultsTableComponent;
  @ViewChild(ResultsGraphBarchartComponent)
  private imageELgraph!: ResultsGraphBarchartComponent;

  // init a custom loadingbar to show progress while waiting for the annotationService result and creating the d3 graph
  constructor(private loadingBar: LoadingBarService, fb: FormBuilder, private elRef: ElementRef,private http: HttpClient) {

    // init datasource checkboxes
    const initialSources = new FormArray(this.initArray)
      this.datasources.forEach((element) => {
        if( element.checked ) {   
          initialSources.push( new FormControl( element.value ) )
        }
    });
    
    // init the selected datasources (e.g. wikidata, dbpedia, etc.)
    this.sourcesForm = fb.group({
      selectedSources:  initialSources
    });
    this.selectedSources = initialSources
    // const ts4tibCollections = this.getTs4tibCollections().subscribe;
     
  }

  el_similarity_label(value: number): string {
    return value.toString();
  }
  

  ngOnInit():any {
    this.getTs4tibOntologies();

    this.getIartImageModels();
    this.setDefaultFile();

    // Material bug workaround
    const workaroundMatTabChangeScroll = (): void => {
      this.elRef.nativeElement.querySelectorAll("mat-tab-group")
      .forEach((tab: HTMLElement) => {
        tab.style.minHeight = "auto";
        tab.style.minHeight = `${tab.offsetHeight}px`;
      });
    };
    workaroundMatTabChangeScroll();
    document.addEventListener("collapse", workaroundMatTabChangeScroll);
    document.addEventListener("uncollapse", workaroundMatTabChangeScroll);
    window.addEventListener("scroll", workaroundMatTabChangeScroll);

  }

  startLoading():void {
    this.loadingBar.useRef().start();
  }

  stopLoading():void
  {
    this.loadingBar.useRef().complete();
  }

  public dictSourceTabChange(index:number|null):void
  {
    if( index != null){
      this.selectedDictSourceTabIndex = index;
    }
  } 

  public userDictTabChange(index:number|null):void
  {
    if( index != null){
      this.selectedUserDictTabIndex = index;
    }
  } 

  setDefaultFile():void {
    // Create a default file object
    const defaultImageBytes = this.convertDataURIToBinary('content/images/landscape.jpg');
    const defaultFile = new File([defaultImageBytes], 'content/images/landscape.jpg', { type: 'image/jpg' });
    this.selectedFile = defaultFile;
  }

  onFileSelected(event: any):void {
    const file: File = event.target.files[0];
    this.selectedFile = file;

    // Display a preview of the selected image
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.imageUrl = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  onCheckboxChange(event: any): void {  
    const selectedSources = (this.sourcesForm.controls['selectedSources'] as FormArray);
    if (event.target.checked) {
      this.selectedSources.push(new FormControl<boolean[]>(event.target.value));
      if( event.target.value === 'wikidata'){
        this.datasources[1].disabled = true;
      } else if( event.target.value === 'wikidata_dbpedia'){
        this.datasources[0].disabled = true;
      } else if( event.target.value === 'ts4tib'){
        this.showTs4tibOntologySelect = true;
      }
      
    } else {
      const index = selectedSources.controls.findIndex(x => x.value === event.target.value);
      this.selectedSources.removeAt(index);
      if( event.target.value === 'wikidata'){
        this.datasources[1].disabled = false;
      } else if( event.target.value === 'wikidata_dbpedia'){
        this.datasources[0].disabled = false;
      } else if( event.target.value === 'ts4tib'){
        this.showTs4tibOntologySelect = false;
      }
    }
  }

  onImageElThresholdChange(): void {
    if( this.annotation.entities.length > 0 ){ 
      this.imageELgraph.clear();
      const filtered = this.annotation.entities.filter((entity) => entity.score > this.image_el_threshold);
      this.imageELgraph.createChartFromClassificationResult(filtered);
    }
  }

  onImageElThresholdInput(event: any): void {
    this.image_el_threshold = event.value;
    this.updateImageElThresholdLabel();
  }

  updateImageElThresholdLabel():void {
    const filtered = this.annotation.entities.filter((entity) => entity.score > this.image_el_threshold);
    this.image_el_threshold_result_count = "(show: "+filtered.length.toString() + " of " + this.annotation.entities.length.toString()+" results)";
  }

  submit():void { 
    this.startLoading();
    // this.annotate();
    this.stopLoading();
  }



  async entityRecognition(): Promise<void> {
    // call the java backend service (falcon)
    // return this.callAnnotationService("entities");

    // call the python rest service (VecNER)
    await this.callVecnerServiceViaBackend();
    // return this.callVecnerServiceDirect();
  }

  async imageEL(): Promise<void> {  // TODO: Use factory-style public method as listener entry for all search tabs
    this.loader.start();
    this.iart_result = "";
    (document.getElementById("imageELresultContainer") as HTMLElement).style.display = 'none';
    (document.getElementById("terminologysearchResultContainer") as HTMLElement).style.display = 'none';
    this.hierarchyGraph.svg.nativeElement.style.display = 'none';
    const url = 'api/annotation/entitylinking/image?model='+this.selectedIartImageModels;
    this.el_result.html = "";

    if (this.selectedFile) {
      const formData = new FormData();
      formData.append('image', this.selectedFile);
      
      if( this.selectedIartImageModels === "ClipClassification" ) {
      let user_dict = {}
      let dict_param = "";
      
      if( this.selectedDictSourceTabIndex === 0) {
        if(this.selectedUserDictTabIndex === 2 && this.el_user_dict_full.value) {
          user_dict = JSON.parse(this.el_user_dict_full.value);
          dict_param = JSON.stringify({fullDictionary:user_dict, dictionaryType: "FullDictionary"} );
          // console.log("full");
        } else if(this.selectedUserDictTabIndex === 1 && this.el_user_dict_simple.value) {
          user_dict = JSON.parse(this.el_user_dict_simple.value);
          dict_param = JSON.stringify({simpleDictionary:user_dict, dictionaryType: "SimpleDictionary"} );
          // console.log("simple");
        } else if(this.selectedUserDictTabIndex === 0 && this.el_user_dict_list.value) {
          user_dict = JSON.parse("["+this.el_user_dict_list.value+"]");
          dict_param = JSON.stringify({ listOfWords:user_dict, dictionaryType: "ListOfWords"} );
          // console.log("list");
        } 
        // formData.append('dictionary',dict_param);
        formData.append('dictionary', new Blob([dict_param], { type: 'application/json' }));
        formData.append('threshold',this.el_threshold.toString());
        formData.append('text', this.imageText.value as string);
        
        // console.log(this.imageText.value);
      } else {
        // console.log("predefined dict");
        // TODO: add dict        
        formData.append('text', this.imageText.value as string);
        formData.append('threshold',this.el_threshold.toString());
      
      }
      }

      const requestOptions: RequestInit = {
        method: "POST",
        body: formData
      };
      
      await fetch(url, requestOptions)
        .then(response => response.json())
        .then(result => {
          this.annotation.entities = result[0];
          
          ResultsService.set(this.annotation.entities);

          this.imageELgraph.clear();
          const filtered = this.annotation.entities.filter((entity) => entity.score > this.image_el_threshold);
          this.imageELgraph.createChartFromClassificationResult(filtered);
          // this.imageELgraph.svg.nativeElement.style.display = 'block';
          this.hierarchyGraph.svg.nativeElement.style.display = 'none';
          (document.getElementById("imageELresultContainer") as HTMLElement).style.display = 'block';
          this.updateImageElThresholdLabel();
        })
        .catch(error => this.iart_result = "error"+(error.toString() as string))
    }
    
    // finish loading bar
    this.loader.complete();
    this.msg =  "";
    // update graph

    this.showResultContainer = true;
  }


  async callVecnerServiceViaBackend():Promise<void>{
    this.err = "";
    this.msg = "";
    this.hierarchyGraph.clear();
    this.showResultContainer = false;
    (document.getElementById("imageELresultContainer") as HTMLElement).style.display = 'none';
    (document.getElementById("terminologysearchResultContainer") as HTMLElement).style.display = 'none';
    this.hierarchyGraph.svg.nativeElement.style.display = 'none';
   
    if( this.textEntityLinking.value === "") {
      this.err = 'Search text cannot be empty';
      return;
    }
    // start the loading bar
    this.loader.start();
    try {
      
      // url of the annotationService api (restful service with json payload)
      const url = 'api/annotation/entitylinking/text?allowDuplicates=' + JSON.stringify(this.allowDuplicates) + '&';
        
      
      this.el_result.html = ""
      let user_dict = {};
      let dict_param = {};
      
      
      if( this.selectedDictSourceTabIndex === 0) {
        if(this.selectedUserDictTabIndex === 2 && this.el_user_dict_full.value) {
          user_dict = JSON.parse(this.el_user_dict_full.value);
          dict_param = {fullDictionary:user_dict, dictionaryType: "FullDictionary"} ;
          // console.log("full");
        } else if(this.selectedUserDictTabIndex === 1 && this.el_user_dict_simple.value) {
          user_dict = JSON.parse(this.el_user_dict_simple.value);
          dict_param = {simpleDictionary:user_dict, dictionaryType: "SimpleDictionary"};
          // console.log("simple");
        } else if(this.selectedUserDictTabIndex === 0 && this.el_user_dict_list.value) {
          user_dict = JSON.parse("["+this.el_user_dict_list.value+"]");
          dict_param = { listOfWords:user_dict, dictionaryType: "ListOfWords"};
          // console.log("list");
        } 
      } else {
        // console.log("predefined dict");
        // TODO: add dict        
      }
      // console.log(dict_param);
      const request_body = JSON.stringify({text:this.textEntityLinking.value, dictionary:dict_param, threshold:this.el_threshold} );
      const body = request_body;
      // console.log(body);
      const response = await fetch(url, {
        method: "POST",
        body,
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
      } 

      // get response and save
      const result = (await response.json()) ;
      
      // display as string
      // this.msg = JSON.stringify(result, null, 4);
      // this.annotation = '';
      this.el_result = result as VecnerResponse;
      // set link target to new tab for html result
      this.el_result.html = this.el_result.html.replace(/href/g, 'target="_blank" href')
      this.hierarchyGraph.svg.nativeElement.style.display = 'none';
      this.table.createTable([]);
      // this.imageELgraph.svg.nativeElement.style.display = 'none';
      this.annotation = result;
      // console.log(result);
      
      // finish loading bar
      this.loader.complete();

      this.msg =  "";
      
      this.showResultContainer = true;


    } catch (error) {
      if (error instanceof Error) {
        this.err = error.message;
        this.loader.stop();
        this.loader.set(0);
      } else {
        this.err = 'An unexpected error occurred';
      }
    }

  }


  // Helper function to convert Data URI to binary data
  convertDataURIToBinary(dataURI: string): Uint8Array {
    // dataURI = btoa(dataURI);
    // const base64Index = dataURI.indexOf(';base64,') + ';base64,'.length;
    // const base64 = dataURI.substring(base64Index);
    // const raw = atob(base64);
    const binaryString = new Array(dataURI.length);
    for (let i = 0; i < dataURI.length; i++) {
      binaryString[i] = dataURI.charCodeAt(i);
    }
    return new Uint8Array(binaryString);
  }

  getStringValue(value: any): string {
    return String(value);
  }

  /* getTs4tibCollections():any {
    const url = 'api/annotation/parameterOptions/ts4tib_collection';
    return this.http.get<ts4tibCollectionsResponse>(url);
  }*/

  compareOntologies = (item:any, selected:any):any => {
    if (selected.collection && item.collection) {
        return item.collection === selected.collection;
    }
    if (item.name && selected.name) {
        return item.name === selected.name;
    }
    return false;
  };

  public ngSearchts4tibOntologies = (searchTerm: string, item: any):boolean => {
    if (item.collection) {
      return item.collection.toLowerCase().indexOf(searchTerm) > -1 || item.name.toLowerCase().indexOf(searchTerm) > -1;
    } else {
      return item.name.toLowerCase().indexOf(searchTerm) > -1;
    }
  }

   async getTs4tibOntologies(): Promise<void> {
    const url = 'api/annotation/parameterOptions/ts4tib_ontology';
    
    const response = await fetch(url, {
      method: "GET",
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
    });
    if (!response.ok) {
      // deactivate ts4tib datasource in the frontend
      this.datasources[3].checked = false;
      this.datasources[3].disabled = true;
      this.datasources[3].name = 'TIB Terminology Service (currently not available)';
      this.showTs4tibOntologySelect = false;
      const index = this.selectedSources.controls.findIndex(x => x.value === 'ts4tib');
      this.selectedSources.removeAt(index);
    } 

    // get response and save
    const ontologiesResponse = await response.json();
    const result = []
    
    for( let i=0; i<ontologiesResponse.ontologies.length; i++) {
      const actOntoEntry = ontologiesResponse.ontologies[i] as ts4tibOntology;
     

      if( !actOntoEntry.collections ) {
        result.push( {id: actOntoEntry.paramValue, name: actOntoEntry.label +" ("+actOntoEntry.paramValue+")", collection: ""} );
      } else {
        if( actOntoEntry.collections.length === 0 ) {
          result.push( {id: actOntoEntry.paramValue, name: actOntoEntry.label +" ("+actOntoEntry.paramValue+")", collection: ""} );
        }
        for( let x=0; x<actOntoEntry.collections.length; x++) {
          const actCollection = actOntoEntry.collections[x];
          result.push( {id: actOntoEntry.paramValue,  name: actOntoEntry.label+" ("+actOntoEntry.paramValue+")", collection: actCollection} );
        }
      }

    }
    this.ts4tibOntologies = result;
  }

  async getIartImageModels(): Promise<void> {
    
      const url = 'api/annotation/parameterOptions/imagemodels';
      const response = await fetch(url, {
        method: "GET",
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
      });
      if (!response.ok) {
       // add warning message
       console.error("unable to fetch image models from iart api")
       return;
      } 
    // console.log(response);
    // get response and save
    const responsejson = await response.json();
    // console.log(responsejson);
    
    this.iartImageModels = responsejson.models;
    // console.log("valid iart models:");
    // console.log(this.iartImageModels);
  }

  async terminologySearch(): Promise<void> {
    return this.callAnnotationService("terminology");
  }

  toggleSettings(): void {
    document.querySelector("jhi-sidebar#settings")?.dispatchEvent(new CustomEvent("toggle"));

    this.elRef.nativeElement.querySelector(".btn-settings")
    .classList.toggle("active");
  }

  // start the annotation process when user submit the request form
  async callAnnotationService(endpoint:string): Promise<void> {
    this.err = "";
    this.msg = "";
    this.hierarchyGraph.clear();
    this.showResultContainer = false;
    (document.getElementById("imageELresultContainer") as HTMLElement).style.display = 'none';
    this.el_result.html = "";  

    if( this.textTerminologySearch.value === "") {
      this.err = 'Search text cannot be empty';
      return;
    }
    // start the loading bar
    this.loader.start();
    try {
      // force utf 8 encoding of text
      
      // url of the annotationService api (restful service with json payload)
      let url = 'api/annotation/' + endpoint + '?allowDuplicates=' + JSON.stringify(this.allowDuplicates) + '&';
      
      // add datasource parameters (optional) to url e.g. wikidata=true, based on the checkbox formgroup
      let ts4tibSelected;
      ts4tibSelected = false;
      this.selectedSources.controls.forEach((element: FormControl) => {
        // check, if this datasource is valid for the endpoint, if valid and checked, add it as a url parameter
        if(
          ( endpoint === "entities" && this.datasources.find((i:any) => i.value === element.value).shownER === true) ||
          ( endpoint === "terminology" && this.datasources.find((i:any) => i.value === element.value).shownTS === true)) {
            url += this.getStringValue(element.value)+"=true&";  
        }
        if(element.value === "ts4tib") {
          ts4tibSelected = true;
        }
      });

      // if ts4Tib is selected as a datasource, add selected ontologies as parameters
      if( ts4tibSelected === true && this.selectedTs4tibOntologies.length > 0) {
        url += "ts4tib_ontology="+this.selectedTs4tibOntologies.join()+"&";
      }
        
      let response;
      if( endpoint === "terminology") {

        url += "searchtext="+String(this.textTerminologySearch.value);
        response = await fetch(url, {
          method: "GET",
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
        });
        
      } else {
        
        const body = JSON.stringify(
          // this.textToAnnotate.value?.split(".") // to split sentences (may fail with terms like "alan M. turing" !)
          [this.textTerminologySearch.value?.trim()]
          );

          response = await fetch(url, {
          method: "POST",
          body,
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
        });
        
      }

      if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
      } 

      // get response and save
      const result = (await response.json()) as AnnotationResponse; // TODO: Add type {class, instance}
      
      // display as string
      // this.msg = JSON.stringify(result, null, 4);
      this.annotation = result;

      ResultsService.set(this.annotation.entities);

      (document.getElementById("imageELresultContainer") as HTMLElement).style.display = 'none';
      (document.getElementById("terminologysearchResultContainer") as HTMLElement).style.display = 'block';
      this.hierarchyGraph.svg.nativeElement.style.display = 'block';

      this.resultCount = this.annotation.hierarchy.children
      .reduce((a, c: { children: unknown[] }) => a + c.children.length, 0);

      // finish loading bar
      this.loader.complete();

      if( result.entities.length > 0) {
        this.msg = "";
        this.showResultContainer = true;
        // update graph
        this.hierarchyGraph.clear();
        this.hierarchyGraph.createTreeFromWikiDataHierarchy(this.annotation.hierarchy);
        // this.table.createTableFromWikiDataHierarchy(this.annotation.entities); // TODO: Delivers wrong IDs (uses label instead)
        // TEMPORARY WORKAROUND:
        this.table.createTable([]
          .concat(...(this.annotation.hierarchy
            .children
            .map((child: any) => child.children)))  // eslint-disable-line
          .map((rawEntity: any) => {                // eslint-disable-line
            const refinedEntity: any = {
              ...rawEntity,
              label: rawEntity.name
            };
            delete refinedEntity.name;              // eslint-disable-line
            return refinedEntity;                   // eslint-disable-line
          })
        );
        
        document.dispatchEvent(new CustomEvent("collapse"));
      } else {
        this.msg = "No Results found";
        this.showResultContainer = false;
      }

    } catch (error) {
      if (error instanceof Error) {
        this.err = error.message;
        this.loader.stop();
        this.loader.set(0);
      } else {
        this.err = 'An unexpected error occurred';
      }
    }
    
  }

  // remove the graph and clear all input fields
  clearAll(): void {
    this.resultTabGroup.selectedIndex = 0; // select graph tab in result section. otherwise, submit button doesnt work anymore (lost focus to subcomponent?)
    this.msg = "";
    this.err = "";
    this.annotation = {"entities":[], "relations":[], hierarchy:{} as unknown as HierarchyTree};
    this.textTerminologySearch.setValue("");
    this.selectedSources = new FormArray(this.initArray);
    this.hierarchyGraph.clear();
    this.loader.stop();
    this.loader.set(0);
    this.showResultContainer = false;
    (document.getElementById("imageELresultContainer") as HTMLElement).style.display = 'none';
    this.el_result.html="";

    EntitySelectService.unselect();

    ResultsService.clear();

    document.dispatchEvent(new CustomEvent("uncollapse"));
  }

  // save content to file and download it
  writeContents(content:string, fileName:string, contentType:string):void {
    const a = document.createElement('a');
    const file = new Blob([content], {type: contentType});
    a.href = URL.createObjectURL(file);
    a.download = fileName;
    a.click();
  }

  // get a html table element reference and write it to xls file
  public exportTableElmToExcel(element: ElementRef, fileName: string): void {
    const ws: XLSX.WorkSheet = XLSX.utils.table_to_sheet(element.nativeElement);
    // generate workbook and add the worksheet
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, ws, 'Sheet1');
    // save to file
    XLSX.writeFile(workbook, `${fileName}${EXCEL_EXTENSION}`);
    

  }

   // get a html table element reference and write it to csv file
   public exportTableElmToCsv(element: ElementRef, fileName: string): void {
    const ws: XLSX.WorkSheet = XLSX.utils.table_to_sheet(element.nativeElement);
    // generate workbook and add the worksheet
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, ws, 'Sheet1');
    const csvOutput: string = XLSX.utils.sheet_to_csv(ws);
    // save to file
    this.writeContents(csvOutput, fileName, 'text/csv');
  }

  public onTabChanged(): void {
    EntitySelectService.unselect();
  }

  
}
