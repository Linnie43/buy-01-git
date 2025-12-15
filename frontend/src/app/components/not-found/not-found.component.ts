import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="not-found-container">
      <h1>404</h1>
      <h2>Page Not Found</h2>
      <p>The page you are looking for does not exist or has been moved.</p>
      <a routerLink="/" class="btn">Go to Homepage</a>
    </div>
  `,
  styles: [`
    .not-found-container {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      height: 70vh;
      text-align: center;
    }
    .not-found-container h1 {
      font-size: 6rem;
      font-weight: bold;
      margin: 0;
    }
    .not-found-container h2 {
      font-size: 1.75rem;
      margin-bottom: 1rem;
    }
    .not-found-container p {
      margin-bottom: 2rem;
    }
  `]
})
export class NotFoundComponent { }
