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
import { AnnotationServiceUIComponent } from 'app/annotation-service-ui/annotation-service-ui.component';
<<<<<<< HEAD

@NgModule({
  imports: [SharedLibsModule],
=======
import { GraphTidytreeComponent } from 'app/graph-tidytree/graph-tidytree.component';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
import { LoadingBarModule } from '@ngx-loading-bar/core';

@NgModule({
  imports: [
    SharedLibsModule,
    NgMultiSelectDropDownModule.forRoot(),
    LoadingBarModule
  ],
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa
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
<<<<<<< HEAD
    AnnotationServiceUIComponent
=======
    AnnotationServiceUIComponent,
    GraphTidytreeComponent
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa
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
<<<<<<< HEAD
    AnnotationServiceUIComponent
=======
    AnnotationServiceUIComponent,
   GraphTidytreeComponent
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa
  ],
})
export class SharedModule {}
