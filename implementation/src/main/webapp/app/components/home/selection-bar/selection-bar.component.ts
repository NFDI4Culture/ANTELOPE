import { Component, ElementRef, OnInit } from '@angular/core';
import { EntitySelectService } from 'app/services/entity-select/entity-select.service';
import { IEntity } from 'app/interfaces/IEntity';
import { FileExportsService } from 'app/services/file-exports/file-exports.service';
import { ResultsService } from 'app/services/results/results.service';


@Component({
  selector: 'jhi-selection-bar',
  templateUrl: './selection-bar.component.html',
  styleUrls: ['./selection-bar.component.scss'],
  providers:  [ EntitySelectService ]
})
export class SelectionBarComponent implements OnInit {

  public selectedEntities: Set<IEntity> = new Set();

  private isActive = false;
  private spacers: {
    el: HTMLElement;
    whenInactive: string;
    whenActive: string;
  }[] = [];

  constructor(private elRef: ElementRef) { }

  ngOnInit(): void {
    window.addEventListener("scroll", () => this.updateVisibility());
    this.updateVisibility();

    this.spacers = Array.from(document.querySelectorAll<HTMLElement>("*[sidebar-selection-space]"))
    .map((el: HTMLElement) => {
      const attr: string[] = (el.getAttribute("sidebar-selection-space") ?? "").split("-");
      return {
        el,

        whenInactive: attr[0],
        whenActive: attr[1]
      };
    });

    EntitySelectService.on("copy", (entity: IEntity) => {
      (this.select(entity) && !this.isActive)
      && this.elRef.nativeElement.querySelector(".indicator").classList.add("notify");
    });

    document.addEventListener("collapse", () => {
      this.selectedEntities.forEach((entity: IEntity) => {
        EntitySelectService.copy(entity);
      });
    });
  }

  toggle(): void {
    this.isActive = !this.isActive;

    this.elRef.nativeElement.classList.toggle("active");
    this.elRef.nativeElement.querySelector(".indicator").classList.remove("notify");
    
    this.updateVisibility();

    this.spacers
    .forEach((spacer) => {
      spacer.el.classList.add(`col-md-${this.isActive ? spacer.whenActive : spacer.whenInactive}`);
      spacer.el.classList.remove(`col-md-${this.isActive ? spacer.whenInactive : spacer.whenActive}`);
    });
  }

  exportSelectionJSON() {
    FileExportsService.exportJSON(Array.from(this.selectedEntities));
  }

  exportSelectionCSV() {
    FileExportsService.exportCSV(Array.from(this.selectedEntities));
  }

  exportAllJSON() {
    console.log(ResultsService.get());
    FileExportsService.exportJSON(ResultsService.get());
  }

  exportAllCSV() {
    FileExportsService.exportCSV(ResultsService.get());
  }

  unselectEntity(entity: IEntity): void {
    this.selectedEntities.delete(entity);

    EntitySelectService.uncopy(entity);
  }

  unselectAll(): void {
    this.selectedEntities.forEach((entitiy: IEntity) => this.unselectEntity(entitiy));
  }

  hasResults(): boolean {
    return !!ResultsService.get().length;
  }

  private updateVisibility(): void {
    const navbar = document.querySelector("jhi-navbar") as HTMLElement|undefined;
    const offset: number = navbar ? navbar.offsetHeight : 50;
    (this.isActive || (window.scrollY > offset))
    ? this.elRef.nativeElement.classList.add("show")
    : this.elRef.nativeElement.classList.remove("show");
  }

  private select(entity: IEntity): boolean {
    if(Array.from(this.selectedEntities).map((e: IEntity) => e.id).includes(entity.id)) {
      return false;
    }

    this.selectedEntities.add(entity);

    return true;
  }

}
