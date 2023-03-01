import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatTabsModule} from '@angular/material/tabs';
import {MatTooltipModule} from '@angular/material/tooltip';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    MatTabsModule,
    MatTooltipModule,
    BrowserAnimationsModule
  ],
  exports: [
    MatTabsModule, 
    MatTooltipModule, 
    BrowserAnimationsModule]
})
export class MaterialModule { }
