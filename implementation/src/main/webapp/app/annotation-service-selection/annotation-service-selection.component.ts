import { Component, ElementRef, OnInit } from '@angular/core';
import { EntitySelectService } from 'app/core/entity-select/entity-select.service';
import { IEntity } from 'app/interfaces/IEntity';


@Component({
  selector: 'jhi-annotation-service-selection',
  templateUrl: './annotation-service-selection.component.html',
  styleUrls: ['./annotation-service-selection.component.scss'],
  providers:  [ EntitySelectService ]
})
export class AnnotationServiceSelectionComponent implements OnInit {

  public entities: Set<IEntity> = new Set();

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
      this.entities.forEach((entity: IEntity) => {
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

  exportJSON(): void {
    this.downloadFile(`antelope-${Date.now()}`, "JSON", JSON.stringify(Array.from(this.entities)));
  }

  exportCSV(): void {
    this.downloadFile(`antelope-${Date.now()}`, "CSV", `${
      Object.keys(Array.from(this.entities)[0]).join(",").trim()
    }\n${
      Array.from(this.entities)
      .map((e: IEntity) => Object.values(e).join(",").trim())
      .join("\n")
    }`.trim());
  }

  unselectEntity(entity: IEntity): void {
    this.entities.delete(entity);

    EntitySelectService.uncopy(entity);
  }

  unselectAll(): void {
    this.entities.forEach((entitiy: IEntity) => this.unselectEntity(entitiy));
  }

  private select(entity: IEntity): boolean {
    if(Array.from(this.entities).map((entity: IEntity) => entity.id).includes(entity.id)) {
      return false;
    }

    this.entities.add(entity);

    return true;
  }

  private updateVisibility(): void {
    const navbar = document.querySelector("jhi-navbar") as HTMLElement|undefined;
    const offset: number = navbar ? navbar.offsetHeight : 50;
    (this.isActive || (window.scrollY > offset))
    ? this.elRef.nativeElement.classList.add("show")
    : this.elRef.nativeElement.classList.remove("show");
  }

  private downloadFile(fileName: string, fileExtension: string, contents: string): void {
    const downloadAnchor = document.createElement("a");
    downloadAnchor.setAttribute("href", `data:text/${fileExtension.toLowerCase()};charset=utf-8,${
      encodeURIComponent(contents)
    }`);
    downloadAnchor.setAttribute("download", `${fileName}.${fileExtension.toLowerCase()}`);
    document.body.appendChild(downloadAnchor);
    setTimeout(() => {
      downloadAnchor.click();
      
      downloadAnchor.remove();
    }, 0);
  }

}
