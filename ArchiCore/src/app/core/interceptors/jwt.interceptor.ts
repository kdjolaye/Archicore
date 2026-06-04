import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { inject, Injector } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { LoginModalService } from '../../shared/login-modal/login-modal';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take, Observable } from 'rxjs';

let isRefreshing = false;
let refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

// ─── URLs à ne jamais intercepter ────────────────────────────────────────────
const AUTH_URLS = [
  '/auth/login',
  '/auth/refresh',
  '/auth/logout',
  '/auth/me'
];

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const injector = inject(Injector);

  // Toutes les requêtes partent avec withCredentials pour les cookies HttpOnly
  const authReq = req.clone({ withCredentials: true });

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {

      // Ne pas intercepter les routes d'auth pour éviter les boucles infinies
      const isAuthUrl = AUTH_URLS.some(url => authReq.url.includes(url));
      if (isAuthUrl) {
        return throwError(() => error);
      }

      // Gestion du 401 : tentative de refresh
      if (error.status === 401) {
        const authService = injector.get(AuthService);
        const modalService = injector.get(LoginModalService);
        return handle401Error(authReq, next, authService, modalService);
      }

      return throwError(() => error);
    })
  );
};

function handle401Error(
  request: HttpRequest<any>,
  next: HttpHandlerFn,
  authService: AuthService,
  modalService: LoginModalService
): Observable<HttpEvent<any>> {

  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshToken().pipe(
      switchMap((response: any) => {
        isRefreshing = false;
        refreshTokenSubject.next(response);
        // Rejoue la requête originale avec le nouveau cookie
        return next(request.clone({ withCredentials: true }));
      }),
      catchError((err) => {
  isRefreshing = false;
  authService.clearSession();
  modalService.show(true); // ← true = session expirée
  return throwError(() => err);
})
    );

  } else {
    // Un refresh est déjà en cours → attendre qu'il se termine puis rejouer
    return refreshTokenSubject.pipe(
      filter(token => token != null),
      take(1),
      switchMap(() => next(request.clone({ withCredentials: true })))
    );
  }
}