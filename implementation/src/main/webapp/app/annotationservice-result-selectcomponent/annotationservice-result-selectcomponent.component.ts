import { Component, ElementRef } from '@angular/core';
import { ResultsGraphTidytreeComponent } from 'app/results-graph-tidytree/results-graph-tidytree.component';


interface INode {
  name: string;
  id: string;
  link: string;

  description?: string;
  imageUrl?: string;
}


@Component({
  selector: 'jhi-annotationservice-result-selectcomponent',
  templateUrl: './annotationservice-result-selectcomponent.component.html',
  styleUrls: ['./annotationservice-result-selectcomponent.component.scss']
})
export class AnnotationserviceResultSelectcomponentComponent {

  private static targetOffsetPx = 25;

  public selected?: INode;

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
    await navigator.clipboard.writeText(this.selected?.id.toString() ?? "");

    ResultsGraphTidytreeComponent.markCopied();
  }

  public view(): void {
    if(!this.selected) { return };
    // TODO
    window.open(this.selected.link, "_blank");
  }

  private adjustPosition(): void {
    if(!this.graphElement) {
      this.graphElement = document.querySelector("jhi-results-graph-tidytree")?.parentElement;
    }
    
    const graphPosition = this.graphElement?.getBoundingClientRect();
    const x = Math.max(AnnotationserviceResultSelectcomponentComponent.targetOffsetPx,
              (graphPosition?.left ?? 0)
              - this.elRef.nativeElement.offsetWidth - AnnotationserviceResultSelectcomponentComponent.targetOffsetPx);
    const y = Math.max(AnnotationserviceResultSelectcomponentComponent.targetOffsetPx * 3,
              (graphPosition?.top ?? 0));

    this.elRef.nativeElement.style.left = `${x}px`;
    this.elRef.nativeElement.style.top = `${y}px`;
  }

}