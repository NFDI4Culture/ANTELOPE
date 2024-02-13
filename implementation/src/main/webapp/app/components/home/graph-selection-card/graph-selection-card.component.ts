import { Component, ElementRef } from '@angular/core';
import { EntitySelectService } from 'app/services/entity-select/entity-select.service';
import { IEntity } from 'app/interfaces/IEntity';


@Component({
  selector: 'jhi-graph-selection-card',
  templateUrl: './graph-selection-card.component.html',
  styleUrls: ['./graph-selection-card.component.scss'],
  providers:  [ EntitySelectService ]
})
export class GraphSelectionCardComponent {

  private static targetOffsetPx = 25;

  public selectedEntity: IEntity|null = null;
  private graphElement: HTMLElement|null|undefined;
  private unselectTimeout: ReturnType<typeof setTimeout>|undefined;

  constructor(private elRef: ElementRef) {
    window.addEventListener("resize", () => this.adjustPosition());
    window.addEventListener("scroll", () => this.adjustPosition());
    
    EntitySelectService.on("select", (entity: IEntity) => this.open(entity));
    EntitySelectService.on("unselect", () => this.close());
  }

  public open(entity: IEntity) {
    clearTimeout(this.unselectTimeout);

    this.selectedEntity = entity;

    this.elRef.nativeElement.classList.add("active");

    setTimeout(() => this.adjustPosition(), 0);
  }

  public close() {
    this.unselectTimeout = setTimeout(() => {
      this.selectedEntity = null;
    }, 800);
    
    this.elRef.nativeElement.classList.remove("active");
  }

  public async copyId(): Promise<void> {
    if(!this.selectedEntity) {
      return;
    }

    EntitySelectService.copy(this.selectedEntity);

    await navigator.clipboard.writeText(this.selectedEntity.id.toString());
  }

  public view(): void {
    if(!this.selectedEntity) { return };
    
    window.open(this.selectedEntity.URI, "_blank");
  }

  private adjustPosition(): void {
    if(!this.graphElement) {
      this.graphElement = document.querySelector("jhi-results-graph-tidytree")?.parentElement;
    }
    
    const graphPosition = this.graphElement?.getBoundingClientRect();
    const x = Math.max(GraphSelectionCardComponent.targetOffsetPx,
              (graphPosition?.left ?? 0)
              - this.elRef.nativeElement.offsetWidth - GraphSelectionCardComponent.targetOffsetPx);
    const y = Math.max(GraphSelectionCardComponent.targetOffsetPx * 3,
              (graphPosition?.top ?? 0));

    this.elRef.nativeElement.style.left = `${x}px`;
    this.elRef.nativeElement.style.top = `${y}px`;
  }

}