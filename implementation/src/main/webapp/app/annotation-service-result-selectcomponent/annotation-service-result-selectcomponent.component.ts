import { Component, ElementRef } from '@angular/core';
import { AnnotationServiceSelectionComponent } from 'app/annotation-service-selection/annotation-service-selection.component';
import { ResultsGraphTidytreeComponent } from 'app/results-graph-tidytree/results-graph-tidytree.component';


interface Entity {
  label: string;
  id: string;
  URI: string;
  source: string;
  classes: string;
  
  description?: string;
  imageUrl?: string;
}


@Component({
  selector: 'jhi-annotation-service-result-selectcomponent',
  templateUrl: './annotation-service-result-selectcomponent.component.html',
  styleUrls: ['./annotation-service-result-selectcomponent.component.scss']
})
export class AnnotationServiceResultSelectcomponentComponent {

  private static targetOffsetPx = 25;

  public selected?: Entity;

  private graphElement: HTMLElement|null|undefined;

  constructor(private elRef: ElementRef) {
    window.addEventListener("resize", () => this.adjustPosition());
    window.addEventListener("scroll", () => this.adjustPosition());
    
    this.elRef.nativeElement.addEventListener("select-node", (data: any) => {
      this.selected = data?.detail;

      this.elRef.nativeElement.classList.add("active");

      setTimeout(() => this.adjustPosition(), 0);
    });
    this.elRef.nativeElement.addEventListener("deselect-node", () => {
      this.elRef.nativeElement.classList.remove("active");
    });
  }

  public async copyId(): Promise<void> {
    if(!this.selected) {
      return;
    }

    await navigator.clipboard.writeText(this.selected.id.toString() ?? "");

    ResultsGraphTidytreeComponent.markCopied();

    AnnotationServiceSelectionComponent.select(this.selected);
  }

  public view(): void {
    if(!this.selected) { return };
    // TODO
    window.open(this.selected.URI, "_blank");
  }

  private adjustPosition(): void {
    if(!this.graphElement) {
      this.graphElement = document.querySelector("jhi-results-graph-tidytree")?.parentElement;
    }
    
    const graphPosition = this.graphElement?.getBoundingClientRect();
    const x = Math.max(AnnotationServiceResultSelectcomponentComponent.targetOffsetPx,
              (graphPosition?.left ?? 0)
              - this.elRef.nativeElement.offsetWidth - AnnotationServiceResultSelectcomponentComponent.targetOffsetPx);
    const y = Math.max(AnnotationServiceResultSelectcomponentComponent.targetOffsetPx * 3,
              (graphPosition?.top ?? 0));

    this.elRef.nativeElement.style.left = `${x}px`;
    this.elRef.nativeElement.style.top = `${y}px`;
  }

}