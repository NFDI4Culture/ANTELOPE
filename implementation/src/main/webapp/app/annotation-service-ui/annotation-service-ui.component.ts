import { Component,ElementRef, OnInit } from '@angular/core';
import { FormControl, FormArray, FormGroup, FormBuilder } from '@angular/forms';
import { ResultsGraphTidytreeComponent } from 'app/results-graph-tidytree/results-graph-tidytree.component';
import { ResultsTableComponent } from 'app/results-table/results-table.component'
// import { AnnotationServiceResultSelectcomponentComponent } from 'app/annotation-service-result-selectcomponent/annotation-service-result-selectcomponent.component';
import { ViewChild } from '@angular/core';
import { LoadingBarService } from '@ngx-loading-bar/core';


import * as XLSX from 'xlsx';


const EXCEL_EXTENSION = '.xlsx';

interface ENTITIES {
  id : string;
  URI: string;
  label: string;
  source: string;
  classes: string
} // TODO: Shared types

// data model of the RESTful annotationService API result
type AnnotationResponse = {
  entities: ENTITIES[];
  relations: [];
  hierarchy: HierarchyTree;
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
  selector: 'jhi-annotation-service-ui',
  templateUrl: './annotation-service-ui.component.html',
  styleUrls: ['./annotation-service-ui.component.scss']
})
export class AnnotationServiceUIComponent implements OnInit{
  loader = this.loadingBar.useRef();
  textToAnnotate = new FormControl('');
  ts4tibOntologies = [
    {id: "NONE", name:"loading ontologies...", collection:"-"}
   
  ]

  initArray:FormControl[] = [];
  initArrayCollections:FormControl[] = [];
  selectedSources = new FormArray(this.initArray);
  selectedTs4tibOntologies= [];// [{name: 'All'}];
  msg = "";
  err = "";
  showResultContainer = false;
  showTs4tibOntologySelect = false;
  allowDuplicates = false;
  resultLimit = 20; // service parameter. number of results, fetched per datasource
  resultCount = 0;
  
  @ViewChild('result_table') resultTableRef: ElementRef = {} as ElementRef;
  
  // create a FormGroup to select the datasources checkboxes state
  sourcesForm: FormGroup;

  // init, which datasources should be preselected in the checkbox group
  datasources: Array<any> = [
    { name: 'WIKIDATA', value: 'wikidata', checked: false, disabled: true, shownTS: true, shownER: true },
    { name: 'WIKIDATA + DBpedia', value: 'wikidata_dbpedia', checked: true, disabled: false, shownTS: true, shownER: true},
    { name: 'ICONCLASS', value: 'iconclass', checked: true, disabled: false , shownTS: true, shownER: false},
    { name: 'GND (Gemeinsame Normdatei)', value: 'gnd', checked: false, disabled: false, shownTS: true, shownER: false},
    { name: 'TIB Terminology Service', value: 'ts4tib', checked: false, disabled: false, shownTS: true, shownER: false}
  ];
  
  dropdownSettings = {};
  
  public annotation: AnnotationResponse = {entities:[], relations:[], hierarchy:{} as unknown as HierarchyTree};
  
  @ViewChild(ResultsGraphTidytreeComponent)
  private graph!: ResultsGraphTidytreeComponent;
  @ViewChild(ResultsTableComponent)
  private table!: ResultsTableComponent;
  
  // init a custom loadingbar to show progress while waiting for the annotationService result and creating the d3 graph
  constructor(private loadingBar: LoadingBarService, fb: FormBuilder, private elRef: ElementRef) {
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

  ngOnInit():any {
    this.getTs4tibOntologies();
  }

  startLoading():void {
    this.loadingBar.useRef().start();
  }

  stopLoading():void
  {
    this.loadingBar.useRef().complete();
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

  submit():void { 
    this.startLoading();
    // this.annotate();
    this.stopLoading();
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

  async terminologySearch(): Promise<void> {
    return this.callAnnotationService("terminology");
  }

  async entityRecognition(): Promise<void> {
    return this.callAnnotationService("entities");
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
    this.graph.clear();
    this.showResultContainer = false;

    if( this.textToAnnotate.value === "") {
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

        url += "searchtext="+String(this.textToAnnotate.value);
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
          [this.textToAnnotate.value?.trim()]
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

      this.resultCount = this.annotation.hierarchy.children
      .reduce((a, c: { children: unknown[] }) => a + c.children.length, 0);

      // finish loading bar
      this.loader.complete();

      if( result.entities.length > 0) {
        this.msg = "";
        this.showResultContainer = true;
        // update graph
        this.graph.clear();
        this.graph.createTreeFromWikiDataHierarchy(this.annotation.hierarchy);
        this.table.createTableFromWikiDataHierarchy(this.annotation.entities);
        
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
    // console.log("test")
    this.msg = "";
    this.err = "";
    this.annotation = {"entities":[], "relations":[], hierarchy:{} as unknown as HierarchyTree};
    this.textToAnnotate.setValue("");
    this.selectedSources = new FormArray(this.initArray);
    this.graph.clear();
    this.loader.stop();
    this.loader.set(0);
    this.showResultContainer = false;

    document.querySelector("jhi-annotation-service-result-selectcomponent")
    ?.dispatchEvent(new CustomEvent("deselect-node", {
      bubbles: false
    }));
  }

  saveJson():void{
    this.writeContents(JSON.stringify(this.annotation, null, 2), 'antelope_result'+'.json', 'text/plain');
  }

  saveTableCSV():void{
    this.exportTableElmToCsv(this.resultTableRef, 'antelope_result');
  }

  saveTableXLS():void{
    this.exportTableElmToExcel(this.resultTableRef, 'antelope_result');
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
    document.querySelector("jhi-annotation-service-result-selectcomponent")
    ?.dispatchEvent(new CustomEvent("deselect-node", {
      bubbles: false
    }));
  }

  
}
