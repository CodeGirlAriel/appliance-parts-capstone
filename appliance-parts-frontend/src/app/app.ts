import { Component, signal } from '@angular/core';
import { RouterOutlet, Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('appliance-parts-frontend');
  
  constructor(public router: Router) {}
}
