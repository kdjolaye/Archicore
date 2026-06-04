import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  /* 
    On utilise un "signal" Angular pour gérer l'état du thème.
    C'est la nouvelle façon performante de gérer la réactivité dans Angular.
  */
  isDarkMode = signal<boolean>(false);

  constructor() {
    // Au chargement, on vérifie si l'utilisateur avait déjà choisi le mode sombre
    const theme = localStorage.getItem('theme');
    if (theme === 'dark') {
      this.enableDarkMode();
    }
  }

  /* Active le mode sombre */
  enableDarkMode() {
    this.isDarkMode.set(true);
    document.documentElement.classList.add('app-dark');
    localStorage.setItem('theme', 'dark');
  }

  /* Désactive le mode sombre (mode clair) */
  disableDarkMode() {
    this.isDarkMode.set(false);
    document.documentElement.classList.remove('app-dark');
    localStorage.setItem('theme', 'light');
  }

  /* Bascule entre les deux modes */
  toggleTheme() {
    if (this.isDarkMode()) {
      this.disableDarkMode();
    } else {
      this.enableDarkMode();
    }
  }
}
