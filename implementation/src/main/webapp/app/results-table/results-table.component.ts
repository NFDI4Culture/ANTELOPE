import { Component, ElementRef } from '@angular/core';
import { AnnotationServiceSelectionComponent } from 'app/annotation-service-selection/annotation-service-selection.component';
import { EntitySelectService } from 'app/core/entity-select/entity-select.service';
import { IEntity } from 'app/interfaces/IEntity';


@Component({
  selector: 'jhi-results-table',
  templateUrl: './results-table.component.html',
  styleUrls: ['./results-table.component.scss'],
  providers: [ EntitySelectService ]
})
export class ResultsTableComponent {

  public entities?: IEntity[];

  constructor(private elRef: ElementRef) {}

  createTableFromWikiDataHierarchy(data: IEntity[]): void {
    this.entities = data;
  }

  public async copyId(entity: IEntity): Promise<void> {
    await navigator.clipboard.writeText(entity.id.toString());

    EntitySelectService.select(entity);

    // TODO: Table copied UI feedback (reflection)
  }

  public view(URI: string): void {
    // TODO
    window.open(URI, "_blank");
  }
  
}