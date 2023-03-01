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
import { AnnotationServiceUIComponent } from 'app/annotation-service-ui/annotation-service-ui.component';
import { AnnotationserviceResultSelectcomponentComponent } from 'app/annotationservice-result-selectcomponent/annotationservice-result-selectcomponent.component';
import { GraphTidytreeComponent } from 'app/graph-tidytree/graph-tidytree.component';
import { LoadingBarModule } from '@ngx-loading-bar/core';
import { MaterialModule } from "./material/material.module";


@NgModule({
  imports: [SharedLibsModule, LoadingBarModule, MaterialModule],
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
    AnnotationServiceUIComponent,
    AnnotationserviceResultSelectcomponentComponent,
    GraphTidytreeComponent
    

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
    AnnotationServiceUIComponent,
    AnnotationserviceResultSelectcomponentComponent,
    GraphTidytreeComponent,
    MaterialModule

  ],
})
export class SharedModule {}
