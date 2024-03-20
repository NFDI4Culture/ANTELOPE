import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule} from '@angular/material/tabs';
import { MatTooltipModule} from '@angular/material/tooltip';
import { MatInputModule} from '@angular/material/input';
import { MatSelectModule} from '@angular/material/select';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatSliderModule} from '@angular/material/slider';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatButtonToggleModule} from '@angular/material/button-toggle';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    MatTabsModule,
    MatTooltipModule,
    MatInputModule,
    MatSelectModule,
    MatExpansionModule,
    MatSliderModule,
    MatSlideToggleModule,
    MatButtonToggleModule
  ],
  exports: [
    MatTabsModule, 
    MatTooltipModule,
    MatInputModule,
    MatSelectModule,
    MatExpansionModule,
    MatSliderModule,
    MatSlideToggleModule,
    MatButtonToggleModule
  ]
})
export class MaterialModule { }
