import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { GraphTidytreeComponent } from 'app/graph-tidytree/graph-tidytree.component';
import { AfterViewInit, ViewChild } from '@angular/core';

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


type WikiDataClass = {
  type: string; 
  value: string; // e.g uri
};

type WikiDataClassLabel = {
  "xml:lang": string; // language
  type: string; 
  value: string; // classname
}

type WikiDataHierarchy = {
  class: WikiDataClass;
  classLabel: WikiDataClassLabel;
  superclass: WikiDataClass
  superclassLabel: WikiDataClassLabel;
}


@Component({
  selector: 'jhi-annotation-service-ui',
  templateUrl: './annotation-service-ui.component.html',
  styleUrls: ['./annotation-service-ui.component.scss']
})
export class AnnotationServiceUIComponent implements OnInit {
  textToAnnotate = new FormControl('');
  msg = "";

  private annotation: AnnotationResponse = {entities:[], relations:[], hierarchy:{} as unknown as HierarchyTree};

  @ViewChild(GraphTidytreeComponent)
  private graph!: GraphTidytreeComponent;

 

  constructor() { }

  ngOnInit(): void {
  }

  ngAfterViewInit():void {
    //setTimeout(() => this.seconds = () => this.timerComponent.seconds, 0);
  }

  async annotate(): Promise<void> {
    try {
      // 👇️ const response: Response
      const response = await fetch('http://localhost:8080/api/annotation/wikidata', {
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
    this.graph.clear();
  }
}
