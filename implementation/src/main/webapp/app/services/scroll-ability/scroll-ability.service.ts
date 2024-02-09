import { Injectable } from "@angular/core";

const wheelEvent: string = "onwheel" in document ? "wheel" : "mousewheel";


function preventDefault(e: Event) {
  e.preventDefault();
}


@Injectable({
  providedIn: "root"
})
export class ScrollAbilityService {

  constructor() { }

  static enable() {
    window.removeEventListener("DOMMouseScroll", preventDefault, false);
    window.removeEventListener(wheelEvent, preventDefault, false); 
    window.removeEventListener("touchmove", preventDefault, false);
  }

  static disable() {
    window.addEventListener("DOMMouseScroll", preventDefault, false); // older FF
    window.addEventListener(wheelEvent, preventDefault, false); // modern desktop
    window.addEventListener("touchmove", preventDefault, false); // mobile
  }

}
