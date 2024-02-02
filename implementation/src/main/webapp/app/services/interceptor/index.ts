import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { AuthInterceptor } from 'app/services/interceptor/auth.interceptor';
import { AuthExpiredInterceptor } from 'app/services/interceptor/auth-expired.interceptor';
import { ErrorHandlerInterceptor } from 'app/services/interceptor/error-handler.interceptor';
import { NotificationInterceptor } from 'app/services/interceptor/notification.interceptor';

export const httpInterceptorProviders = [
  {
    provide: HTTP_INTERCEPTORS,
    useClass: AuthInterceptor,
    multi: true,
  },
  {
    provide: HTTP_INTERCEPTORS,
    useClass: AuthExpiredInterceptor,
    multi: true,
  },
  {
    provide: HTTP_INTERCEPTORS,
    useClass: ErrorHandlerInterceptor,
    multi: true,
  },
  {
    provide: HTTP_INTERCEPTORS,
    useClass: NotificationInterceptor,
    multi: true,
  },
];
