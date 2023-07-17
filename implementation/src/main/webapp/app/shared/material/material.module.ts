import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule} from '@angular/material/tabs';
import { MatTooltipModule} from '@angular/material/tooltip';
import { MatInputModule} from '@angular/material/input';
import { MatSelectModule} from '@angular/material/select';
import {MatExpansionModule} from '@angular/material/expansion';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    MatTabsModule,
    MatTooltipModule,
    MatInputModule,
    MatSelectModule,
    MatExpansionModule
  ],
  exports: [
    MatTabsModule, 
    MatTooltipModule,
    MatInputModule,
    MatSelectModule,
    MatExpansionModule
  ]
})
export class MaterialModule { }
