import { Component, ElementRef } from '@angular/core';
import { EntitySelectService } from 'app/core/entity-select/entity-select.service';
import { IEntity } from 'app/interfaces/IEntity';


@Component({
  selector: 'jhi-annotation-service-result-selectcomponent',
  templateUrl: './annotation-service-result-selectcomponent.component.html',
  styleUrls: ['./annotation-service-result-selectcomponent.component.scss'],
  providers:  [ EntitySelectService ]
})
export class AnnotationServiceResultSelectcomponentComponent {

  private static targetOffsetPx = 25;

  public selectedEntity: IEntity|null = null;
  
  private graphElement: HTMLElement|null|undefined;

  constructor(private elRef: ElementRef) {
    window.addEventListener("resize", () => this.adjustPosition());
    window.addEventListener("scroll", () => this.adjustPosition());
    
    EntitySelectService.on("select", (entity: IEntity) => {
      this.selectedEntity = entity;

      this.elRef.nativeElement.classList.add("active");

      setTimeout(() => this.adjustPosition(), 0);
    });
    EntitySelectService.on("unselect", () => {
      this.selectedEntity = null;

      this.elRef.nativeElement.classList.remove("active");
    });
  }

  public async copyId(): Promise<void> {
    if(!this.selectedEntity) {
      return;
    }

    await navigator.clipboard.writeText(this.selectedEntity.id.toString());
    
    EntitySelectService.copy(this.selectedEntity);
  }

  public view(): void {
    if(!this.selectedEntity) { return };
    // TODO
    window.open(this.selectedEntity.URI, "_blank");
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