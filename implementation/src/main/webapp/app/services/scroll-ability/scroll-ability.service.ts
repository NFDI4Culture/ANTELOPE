import { Injectable } from "@angular/core";

const wheelEvent: string = "onwheel" in document ? "wheel" : "mousewheel";


function preventDefault(e: Event):void {
  e.preventDefault();
}


@Injectable({
  providedIn: "root"
})
export class ScrollAbilityService {

  static enable():void {
    window.removeEventListener("DOMMouseScroll", preventDefault, false);
    window.removeEventListener(wheelEvent, preventDefault, false); 
    window.removeEventListener("touchmove", preventDefault, false);
  }

  static disable():void {
    window.addEventListener("DOMMouseScroll", preventDefault, false); // older FF
    window.addEventListener(wheelEvent, preventDefault, false); // modern desktop
    window.addEventListener("touchmove", preventDefault, false); // mobile
  }

}
