import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule} from '@angular/material/tabs';
import { MatTooltipModule} from '@angular/material/tooltip';
import { MatInputModule} from '@angular/material/input';
import { MatSelectModule} from '@angular/material/select';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    MatTabsModule,
    MatTooltipModule,
    MatInputModule,
    MatSelectModule
  ],
  exports: [
    MatTabsModule, 
    MatTooltipModule,
    MatInputModule,
    MatSelectModule
  ]
})
export class MaterialModule { }
