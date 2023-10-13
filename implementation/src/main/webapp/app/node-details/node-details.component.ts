import { Component, ElementRef } from '@angular/core';


interface INode {
  name: string;
  id: string;
  link: string;

  description?: string;
  imageUrl?: string;
}


@Component({
  selector: 'jhi-node-details',
  templateUrl: './node-details.component.html',
  styleUrls: ['./node-details.component.scss'],
})
export class NodeDetailsComponent {

  private static targetOffsetPx = 25;
  public selected?: INode;

  constructor(private elRef: ElementRef) {
    window.addEventListener("resize", () => this.adjustPosition());
    
    this.elRef.nativeElement.addEventListener("select-node", (data: any) => {
      this.selected = data.detail;
      
      this.adjustPosition();

      this.elRef.nativeElement.classList.add("active");
    });
    this.elRef.nativeElement.addEventListener("deselect-node", () => {
      this.elRef.nativeElement.classList.remove("active");
    });
  }

  private adjustPosition() {
    const graphElement = document.querySelector("jhi-graph-tidytree");

    if(!graphElement) return;

    const graphPosition = graphElement.parentElement!.getBoundingClientRect();
    const x = graphPosition.left - this.elRef.nativeElement.offsetWidth - NodeDetailsComponent.targetOffsetPx;
    const y = graphPosition.top * 25;  // TODO

    this.elRef.nativeElement.style.left = `${x}px`;
    this.elRef.nativeElement.style.top = `${y}px`;
  }

  public copyId() {
    // TODO
  }

  public view() {
    if(!this.selected) return;
    // TODO
    window.open(this.selected.link, "_blank");
  }
  
}
