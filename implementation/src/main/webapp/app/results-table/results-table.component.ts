import { Component, ElementRef } from '@angular/core';
import { ResultsGraphTidytreeComponent } from 'app/results-graph-tidytree/results-graph-tidytree.component';

type Entity = {
  id : string;
  URI: string;
  label: string;
  source: string;
  classes: string

  description?: string;
  imageUrl?: string;
}

@Component({
  selector: 'jhi-results-table',
  templateUrl: './results-table.component.html',
  styleUrls: ['./results-table.component.scss']
})
export class ResultsTableComponent {

  public entities?: Entity[];

  constructor(private elRef: ElementRef) {}

  createTableFromWikiDataHierarchy(data: Entity[]): void {
    this.entities = data;
  }

  public async copyId(id: string): Promise<void> {
    await navigator.clipboard.writeText(id.toString());

    // TODO: Table copied UI feedback (reflection)
  }

  public view(URI: string): void {
    // TODO
    window.open(URI, "_blank");
  }
  
}