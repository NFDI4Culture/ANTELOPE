import { Component, ElementRef } from '@angular/core';
import { doc } from 'prettier';

@Component({
  selector: 'jhi-overlay',
  templateUrl: './overlay.component.html',
  styleUrls: ['./overlay.component.scss']
})
export class OverlayComponent {

  private isOpen: boolean = false;

  constructor(private elRef: ElementRef) {
    this.elRef.nativeElement.addEventListener("toggle", () => this.toggle());
  }

  public toggle() {
    this.isOpen = !this.isOpen;

    document.body.style.overflow = this.isOpen ? "hidden" : "auto";

    this.elRef.nativeElement.classList.toggle("active");
  }

}
