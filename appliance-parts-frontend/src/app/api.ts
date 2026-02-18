import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, timeout, catchError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private baseUrl = 'https://appliance-parts-backend.onrender.com';

  constructor(private http: HttpClient) {}

  searchParts(query: string): Observable<any[]> {
    const encodedQuery = encodeURIComponent(query);
    const url = `${this.baseUrl}/parts/search?query=${encodedQuery}`;
    console.log('Making request to:', url);
    
    return this.http.get<any[]>(url).pipe(
      timeout(10000), // 10 second timeout
      catchError((error: any) => {
        console.error('HTTP Error:', error);
        if (error.name === 'TimeoutError' || error.message?.includes('timeout')) {
          return throwError(() => new Error('Request timed out. Please check if the backend is running.'));
        }
        return throwError(() => error);
      })
    );
  }

  comparePart(partId: string, sort?: string): Observable<any> {
    let url = `${this.baseUrl}/parts/${partId}/compare`;
    if (sort) {
      url += `?sort=${sort}`;
    }
    return this.http.get(url);
  }

  saveQuote(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/quotes`, payload);
  }

  getQuotes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/quotes`);
  }

  getCartItems(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/cart`);
  }

  deleteQuote(orderId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/quotes/${orderId}`);
  }

  updateQuoteSupplier(orderId: number, newPartSupplierId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/quotes/${orderId}/supplier`, {
      newPartSupplierId: newPartSupplierId
    });
  }

  checkout(orderId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/orders/${orderId}/checkout`, {});
  }

  getAllOrders(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/orders`);
  }

  getOrdersByStatus(status: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/orders/status/${status}`);
  }
}