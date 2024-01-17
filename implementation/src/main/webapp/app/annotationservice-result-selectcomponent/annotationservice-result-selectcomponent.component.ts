import { Component, ElementRef } from '@angular/core';
import { GraphTidytreeComponent } from 'app/graph-tidytree/graph-tidytree.component';


interface INode {
  name: string;
  id: string;
  link: string;
  clickPos: {
    x: number;
    y: number;
  };

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

  private graphElement: HTMLElement|null|undefined;

  public selected?: INode;

  constructor(private elRef: ElementRef) {
    window.addEventListener("resize", () => this.adjustPosition());
    
    this.elRef.nativeElement.addEventListener("select-node", (data: any) => {
      this.selected = data?.detail;

      this.elRef.nativeElement.classList.add("active");

      setTimeout(() => this.adjustPosition(), 0);
    });
    this.elRef.nativeElement.addEventListener("deselect-node", () => {
      this.elRef.nativeElement.classList.remove("active");
    });
  }

  private adjustPosition() {
    if(!this.graphElement) {
      this.graphElement = document.querySelector("jhi-graph-tidytree")?.parentElement;
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

  public async copyId() {
    await navigator.clipboard.writeText(this.selected?.id.toString() ?? "");

    GraphTidytreeComponent.markCopied();
  }

  public view() {
    if(!this.selected) return;
    // TODO
    window.open(this.selected.link, "_blank");
  }
  
}