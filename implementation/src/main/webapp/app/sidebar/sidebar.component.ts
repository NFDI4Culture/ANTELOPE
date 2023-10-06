import { Component, ElementRef } from '@angular/core';


@Component({
  selector: 'jhi-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent {

  constructor(private elRef: ElementRef) {
    this.elRef.nativeElement.addEventListener("toggle", () => this.toggle());
  }

  private toggle() {
    this.elRef.nativeElement.classList.toggle("active");
  }
  
}
