import { Component, ElementRef, OnInit } from '@angular/core';

type Entity = {
  id : string;
  URI: string;
  label: string;
  source: string;
  classes: string

  description?: string;
  imageUrl?: string;
}

@Component({
  selector: 'jhi-annotation-service-selection',
  templateUrl: './annotation-service-selection.component.html',
  styleUrls: ['./annotation-service-selection.component.scss']
})
export class AnnotationServiceSelectionComponent implements OnInit {

  public entities: Set<Entity> = new Set();

  private isActive = false;
  private spacer?: HTMLElement;

  constructor(private elRef: ElementRef) { }

  static select(entity: Entity): void {
    document.querySelector("jhi-annotation-service-selection")
    ?.dispatchEvent(new CustomEvent("select-entity", {
      detail: entity,
      bubbles: false
    }));
  }

  ngOnInit(): void {
    window.addEventListener("scroll", () => this.updateVisibility());
    this.updateVisibility();

    this.spacer = document.querySelector(`#${
      String(this.elRef.nativeElement.getAttribute("spacer-id"))
    }`) as HTMLElement;

    this.elRef.nativeElement.addEventListener("select-entity", (data: any) => {
      (this.select(data.detail) && !this.isActive)
      && this.elRef.nativeElement.querySelector(".indicator").classList.add("notify");
    });
  }

  toggle(): void {
    this.isActive = !this.isActive;

    this.elRef.nativeElement.classList.toggle("active");
    this.elRef.nativeElement.querySelector(".indicator").classList.remove("notify");
    
    this.updateVisibility();

    if(!this.spacer) {
      return;
    }
    if(this.isActive) {
      this.spacer.style.width = `${12.5}%`;
    } else {
      this.spacer.style.removeProperty("width");
    }
  }

  exportJSON(): void {
    this.downloadFile(`antelope-${Date.now()}`, "JSON", JSON.stringify(Array.from(this.entities)));
  }

  exportCSV(): void {
    this.downloadFile(`antelope-${Date.now()}`, "CSV", `${
      Object.keys(Array.from(this.entities)[0]).join(",").trim()
    }\n${
      Array.from(this.entities)
      .map((e: Entity) => Object.values(e).join(",").trim())
      .join("\n")
    }`.trim());
  }

  deselectEntity(entity: Entity): void {
    this.entities.delete(entity);
  }

  deselectAll(): void {
    this.entities.clear();
  }

  private select(entity: Entity): boolean {
    if(this.entities.has(entity)) {
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
