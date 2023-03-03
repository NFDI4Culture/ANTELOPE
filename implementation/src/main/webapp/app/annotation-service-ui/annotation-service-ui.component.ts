import { Component, OnInit,ElementRef } from '@angular/core';
import { FormControl, FormArray, FormGroup, FormBuilder } from '@angular/forms';
import { GraphTidytreeComponent } from 'app/graph-tidytree/graph-tidytree.component';
import { AnnotationserviceResultSelectcomponentComponent } from 'app/annotationservice-result-selectcomponent/annotationservice-result-selectcomponent.component';
import { ViewChild } from '@angular/core';
import { LoadingBarService } from '@ngx-loading-bar/core';
import { MatTabGroup } from '@angular/material/tabs';
import { MatTab } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTooltip } from '@angular/material/tooltip';
import * as XLSX from 'xlsx';

const EXCEL_EXTENSION = '.xlsx';

interface ENTITIES {
  
  URI: String;
  "surface form": String;
  "notationName":String;

}

// data model of the RESTful annotationService API result
type AnnotationResponse = {
  entities: ENTITIES[];
  relations: [];
  hierarchy: HierarchyTree;
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

export class AnnotationServiceUIComponent implements OnInit {
  loader = this.loadingBar.useRef();
  textToAnnotate = new FormControl('');
  initArray:FormControl[] = [];
  selectedSources = new FormArray(this.initArray);
  msg = "";
  err = "";
  showResultContainer:boolean = false;
  
  @ViewChild('result_table') resultTableRef: ElementRef = {} as ElementRef;
  
  // create a FormGroup to select the datasources checkboxes state
  sourcesForm: FormGroup;

  // init, which datasources should be preselected in the checkbox group
  datasources: Array<any> = [
    { name: 'WIKIDATA', value: 'wikidata', checked: false, disabled: true },
    { name: 'WIKIDATA + DBpedia', value: 'wikidata_dbpedia', checked: true, disabled: false},
    { name: 'ICONCLASS', value: 'iconclass', checked: true, disabled: false }
  ];
  selectedItems = [];
  dropdownSettings = {};
  
  public annotation: AnnotationResponse = {entities:[], relations:[], hierarchy:{} as unknown as HierarchyTree};
  
  @ViewChild(GraphTidytreeComponent)
  private graph!: GraphTidytreeComponent;
  
  // init a custom loadingbar to show progress while waiting for the annotationService result and creating the d3 graph
  constructor(private loadingBar: LoadingBarService, fb: FormBuilder) {
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
  }

  startLoading():void {
    this.loadingBar.useRef().start();
  }

  stopLoading():void
  {
    this.loadingBar.useRef().complete();
  }

  ngOnInit(): void {

  }

  onCheckboxChange(event: any): void {  
    const selectedSources = (this.sourcesForm.controls['selectedSources'] as FormArray);
    if (event.target.checked) {
      this.selectedSources.push(new FormControl<boolean[]>(event.target.value));
      if( event.target.value === 'wikidata'){
        this.datasources[1].disabled = true;
      } else if( event.target.value === 'wikidata_dbpedia'){
        this.datasources[0].disabled = true;
      } 
      
    } else {
      const index = selectedSources.controls.findIndex(x => x.value === event.target.value);
      this.selectedSources.removeAt(index);
      if( event.target.value === 'wikidata'){
        this.datasources[1].disabled = false;
      } else if( event.target.value === 'wikidata_dbpedia'){
        this.datasources[0].disabled = false;
      } 
    }
  }

  submit():void { 
    this.startLoading();
    //this.annotate();
    this.stopLoading();
  }

  getStringValue(value: any): string {
    return String(value);
  }

  async terminologySearch(): Promise<void> {
    return this.callAnnotationService("terminology", "GET");
  }

  async entityRecognition(): Promise<void> {
    return this.callAnnotationService("entities", "POST");
  }

  // start the annotation process when user submit the request form
  async callAnnotationService(endpoint:string, method:string): Promise<void> {
    this.err = "";
    this.msg = "";
    this.graph.clear();
    this.showResultContainer = false;

    if( this.textToAnnotate.value == "") {
      this.err = 'Search text cannot be empty';
      return;
    }
    // start the loading bar
    this.loader.start();
    try {

      // url of the annotationService api (restful service with json payload)
      let url = 'api/annotation/'+endpoint+'?';
      
      // add datasource parameters (optional) to url e.g. wikidata=true, based on the checkbox formgroup
      this.selectedSources.controls.forEach((element:FormControl) => {
        url += this.getStringValue(element.value)+"=true&";  
      });
        
      var response;
      if( endpoint == "terminology") {

        url += "searchtext="+this.textToAnnotate.value;
        response = await fetch(url, {
          method: "GET",
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
        });
        
      } else {

        let body = JSON.stringify(
          [this.textToAnnotate.value]
          );

          response = await fetch(url, {
          method: "POST",
          
          body: body,
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
        });
        
      }

      if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
      } 
      console.log(url);

      // get response and save
      const result = (await response.json()) as AnnotationResponse;
      
      // display as string
      //this.msg = JSON.stringify(result, null, 4);
      this.annotation = result;
      //console.log(result);
      
      // finish loading bar
      this.loader.complete();
      

      if( result.entities.length > 0) {
        this.msg = "Results are displayed below. Use the mousewheel to zoom in/out. Use drag and drop to move the graph.";
        this.showResultContainer = true;
        // update graph
        this.graph.clear();
        this.graph.createTreeFromWikiDataHierarchy(this.annotation.hierarchy);

      } else {
        this.msg = "No Results found";
        this.showResultContainer = false;
      }

    } catch (error) {
      if (error instanceof Error) {
        this.err = error.message;
      } else {
        this.err = 'An unexpected error occurred';
      }
    }
    
  }

  // remove the graph and clear all input fields
  clearAll(): void {
    console.log("test")
    this.msg = "";
    this.err = "";
    this.annotation = {"entities":[], "relations":[], hierarchy:{} as unknown as HierarchyTree};
    this.textToAnnotate.setValue("");
    this.selectedSources = new FormArray(this.initArray);
    this.graph.clear();
    this.loader.stop();
    this.loader.set(0);
    this.showResultContainer = false;
  }


  saveJson(){
    this.writeContents(JSON.stringify(this.annotation, null, 2), 'antelope_result'+'.json', 'text/plain');
  }

  saveTableCSV(){
    this.exportTableElmToCsv(this.resultTableRef, 'antelope_result');
  }

  saveTableXLS(){
    this.exportTableElmToExcel(this.resultTableRef, 'antelope_result');
  }

  //save content to file and download it
  writeContents(content:string, fileName:string, contentType:string) {
    var a = document.createElement('a');
    var file = new Blob([content], {type: contentType});
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
    this.writeContents(csvOutput, fileName, 'text/csv')
    

  }

  
}
