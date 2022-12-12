import { Component, OnInit } from '@angular/core';
import { FormControl, FormArray, FormGroup, FormBuilder } from '@angular/forms';
import { GraphTidytreeComponent } from 'app/graph-tidytree/graph-tidytree.component';
import { ViewChild } from '@angular/core';
import { LoadingBarService } from '@ngx-loading-bar/core';

type AnnotationResponse = {
  entities: [];
  relations: [];
  hierarchy: HierarchyTree;
};

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
  selectedSources = new FormArray([]);
  msg = "";
  sourcesForm: FormGroup;
  datasources: Array<any> = [
    { name: 'WIKIDATA', value: 'wikidata', checked: false, disabled: true },
    { name: 'WIKIDATA + DBpedia', value: 'wikidata_dbpedia', checked: true, disabled: false},
    { name: 'ICONCLASS', value: 'iconclass', checked: true, disabled: false }
  ];
  selectedItems = [];
  dropdownSettings = {};
  
  private annotation: AnnotationResponse = {entities:[], relations:[], hierarchy:{} as unknown as HierarchyTree};
  
  @ViewChild(GraphTidytreeComponent)
  private graph!: GraphTidytreeComponent;
  
  
  constructor(private loadingBar: LoadingBarService, fb: FormBuilder) {
    const initialSources = new FormArray([])
      this.datasources.forEach((element) => {
        if( element.checked ) {   
          initialSources.push( new FormControl( element.value ) )
        }
    });
    
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
      this.selectedSources.push(new FormControl(event.target.value));
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
    this.annotate();
    this.stopLoading();
  }

  getStringValue(value: any): string {
    return String(value);
  }
  async annotate(): Promise<void> {
    this.loader.start();
    try {
      // 👇️ const response: Response
      let url = 'http://localhost:8080/api/annotation/entities?';

      // add datasource parameters (optional) to url e.g. wikidata=true, based on the checkbox formgroup
      this.selectedSources.controls.forEach((element) => {
        url += this.getStringValue(element.value)+"=true&";  
      });
        
      const response = await fetch(url, {
        method: 'POST',
        
        body: JSON.stringify(
          [this.textToAnnotate.value]
          ),
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
      }

      // get response and save
      const result = (await response.json()) as AnnotationResponse;
      
      // display as string
      this.msg = JSON.stringify(result, null, 4);
      this.annotation = result;
      
      this.loader.complete();
      
      // update graph
      this.graph.clear();
      this.graph.createTreeFromWikiDataHierarchy(this.annotation.hierarchy);

    } catch (error) {
      if (error instanceof Error) {
        this.msg = error.message;
      } else {
        this.msg = 'An unexpected error occurred';
      }
    }
    
  }

  clearAll(): void {
    this.msg = "";
    this.annotation = {"entities":[], "relations":[], hierarchy:{} as unknown as HierarchyTree};
    this.textToAnnotate.setValue("");
    this.selectedSources.setValue([]);
    this.graph.clear();
    this.loadingBar.useRef().complete();
  }
}
