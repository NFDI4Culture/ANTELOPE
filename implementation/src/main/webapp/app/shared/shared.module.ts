import { NgModule } from '@angular/core';

import { SharedLibsModule } from './shared-libs.module';
import { FindLanguageFromKeyPipe } from './language/find-language-from-key.pipe';
import { TranslateDirective } from './language/translate.directive';
import { AlertComponent } from './alert/alert.component';
import { AlertErrorComponent } from './alert/alert-error.component';
import { HasAnyAuthorityDirective } from './auth/has-any-authority.directive';
import { DurationPipe } from './date/duration.pipe';
import { FormatMediumDatetimePipe } from './date/format-medium-datetime.pipe';
import { FormatMediumDatePipe } from './date/format-medium-date.pipe';
import { SortByDirective } from './sort/sort-by.directive';
import { SortDirective } from './sort/sort.directive';
import { ItemCountComponent } from './pagination/item-count.component';
import { FilterComponent } from './filter/filter.component';
import { OverlayComponent } from 'app/overlay/overlay.component';
import { AnnotationServiceUIComponent } from 'app/annotation-service-ui/annotation-service-ui.component';
import { AnnotationserviceResultSelectcomponentComponent } from 'app/annotationservice-result-selectcomponent/annotationservice-result-selectcomponent.component';
import { ResultsGraphTidytreeComponent } from 'app/results-graph-tidytree/results-graph-tidytree.component';
import { ResultsTableComponent } from 'app/results-table/results-table.component';
import { LoadingBarModule } from '@ngx-loading-bar/core';
import { MaterialModule } from "./material/material.module";
import { ClipboardModule } from '@angular/cdk/clipboard'; 
import { NgSelectModule } from '@ng-select/ng-select';


@NgModule({
  imports: [SharedLibsModule, LoadingBarModule, MaterialModule, ClipboardModule, NgSelectModule],
  declarations: [
    FindLanguageFromKeyPipe,
    TranslateDirective,
    AlertComponent,
    AlertErrorComponent,
    HasAnyAuthorityDirective,
    DurationPipe,
    FormatMediumDatetimePipe,
    FormatMediumDatePipe,
    SortByDirective,
    SortDirective,
    ItemCountComponent,
    FilterComponent,
    OverlayComponent,
    AnnotationServiceUIComponent,
    AnnotationserviceResultSelectcomponentComponent,
    ResultsGraphTidytreeComponent,
    ResultsTableComponent
  ],
  exports: [
    SharedLibsModule,
    FindLanguageFromKeyPipe,
    TranslateDirective,
    AlertComponent,
    AlertErrorComponent,
    HasAnyAuthorityDirective,
    DurationPipe,
    FormatMediumDatetimePipe,
    FormatMediumDatePipe,
    SortByDirective,
    SortDirective,
    ItemCountComponent,
    FilterComponent,
    OverlayComponent,
    AnnotationServiceUIComponent,
    AnnotationserviceResultSelectcomponentComponent,
    ResultsGraphTidytreeComponent,
    ResultsTableComponent,
    MaterialModule,
    ClipboardModule
  ],
})
export class SharedModule {}
