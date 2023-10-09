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

  public selected?: INode;

  constructor(private elRef: ElementRef) {
    this.elRef.nativeElement.addEventListener("select-node", (data: any) => {
      this.selected = data.detail;

      this.elRef.nativeElement.classList.add("active");
    });
    this.elRef.nativeElement.addEventListener("deselect-node", () => {
      this.elRef.nativeElement.classList.remove("active");
    });
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
