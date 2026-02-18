import { Routes } from '@angular/router';
import { SearchComponent } from './search/search';
import { CompareComponent } from './compare/compare';
import { QuotesComponent } from './quotes/quotes';
import { ReportsComponent } from './reports/reports';
import { CartComponent } from './cart/cart';

export const routes: Routes = [
  { path: '', component: SearchComponent },
  { path: 'compare/:partId', component: CompareComponent },
  { path: 'quotes', component: QuotesComponent },
  { path: 'cart', component: CartComponent },
  { path: 'reports', component: ReportsComponent }
];