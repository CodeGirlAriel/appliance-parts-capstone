import { Component, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ApiService } from '../api';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'app-compare',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './compare.html'
})
export class CompareComponent {

  partId: string = '';
  comparison: any = null;
  quantities: { [key: number]: number } = {};
  savingToCart: { [key: number]: boolean } = {}; // Track saving to cart state per button
  savingAsQuote: { [key: number]: boolean } = {}; // Track saving as quote state per button
  loading: boolean = true;
  error: string = '';
  successMessage: string = '';
  successAction: 'cart' | 'quote' | null = null; // Track which action was successful

  constructor(
    private route: ActivatedRoute, 
    private api: ApiService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.partId = this.route.snapshot.paramMap.get('partId') || '';
    console.log('CompareComponent initialized with partId:', this.partId);

    this.loadComparison();
  }

  loadComparison(sortMode?: string) {
    this.loading = true;
    this.error = '';
    this.comparison = null;
    
    console.log('Loading comparison for partId:', this.partId, 'sortMode:', sortMode);
    
    this.api.comparePart(this.partId, sortMode).subscribe({
      next: (data) => {
        console.log('Comparison data received:', data);
        console.log('Data type:', typeof data);
        console.log('Raw data:', JSON.stringify(data, null, 2));
        console.log('Options:', data?.options);
        console.log('Options length:', data?.options?.length);
        
        this.comparison = data;
        this.loading = false;
        
        // Initialize quantities to 1 for each option
        if (data && data.options && Array.isArray(data.options)) {
          data.options.forEach((option: any) => {
            if (!this.quantities[option.partSupplierId]) {
              this.quantities[option.partSupplierId] = 1;
            }
          });
          console.log('Initialized quantities for', data.options.length, 'options');
        }
        
        this.cdr.detectChanges(); // Force change detection
        console.log('After update - comparison:', this.comparison);
        console.log('After update - loading:', this.loading);
      },
      error: (err) => {
        console.error('Error loading comparison:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Error body:', err.error);
        
        if (err.status === 0 || err.message?.includes('Failed to fetch')) {
          this.error = 'Cannot connect to backend. Please make sure the Spring Boot server is running.';
        } else if (err.status === 404) {
          this.error = 'Part not found. Please check the part ID.';
        } else if (err.status === 500) {
          this.error = 'Server error: ' + (err.error?.message || 'Internal server error');
        } else {
          this.error = 'Failed to load comparison: ' + (err.error?.message || err.message || 'Unknown error');
        }
        this.loading = false;
        this.comparison = null;
        this.cdr.detectChanges(); // Force change detection
      }
    });
  }

  onSortChange(event: any) {
    const sortMode = event.target.value;
    this.loadComparison(sortMode || undefined);
  }

  addToCart(partSupplierId: number) {
    const quantity = this.quantities[partSupplierId];
    
    if (!quantity || quantity <= 0) {
      this.error = 'Please enter a valid quantity.';
      this.cdr.detectChanges();
      return;
    }

    // Set saving state for this specific button
    this.savingToCart[partSupplierId] = true;
    this.error = '';
    this.successMessage = '';
    this.successAction = null;

    console.log('Adding to cart - partSupplierId:', partSupplierId, 'quantity:', quantity);

    this.api.saveQuote({
      partSupplierId: partSupplierId,
      quantity: quantity,
      isCartItem: true
    }).subscribe({
      next: (response) => {
        console.log('Item added to cart successfully:', response);
        this.successMessage = `Item added to cart! Cart Item ID: ${response.orderId}`;
        this.successAction = 'cart';
        this.savingToCart[partSupplierId] = false;
        // Clear the quantity input after saving
        this.quantities[partSupplierId] = 1;
        this.cdr.detectChanges();
        
        // Clear success message after 5 seconds
        setTimeout(() => {
          this.successMessage = '';
          this.successAction = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Error adding to cart:', err);
        console.error('Error status:', err.status);
        console.error('Error body:', err.error);
        this.error = err.error?.message || err.message || 'Failed to add to cart. Please try again.';
        this.savingToCart[partSupplierId] = false;
        this.cdr.detectChanges();
      }
    });
  }

  saveAsQuote(partSupplierId: number) {
    const quantity = this.quantities[partSupplierId];
    
    if (!quantity || quantity <= 0) {
      this.error = 'Please enter a valid quantity.';
      this.cdr.detectChanges();
      return;
    }

    // Set saving state for this specific button
    this.savingAsQuote[partSupplierId] = true;
    this.error = '';
    this.successMessage = '';
    this.successAction = null;

    console.log('Saving as quote - partSupplierId:', partSupplierId, 'quantity:', quantity);

    this.api.saveQuote({
      partSupplierId: partSupplierId,
      quantity: quantity,
      isCartItem: false
    }).subscribe({
      next: (response) => {
        console.log('Quote saved successfully:', response);
        this.successMessage = `Quote saved successfully! Quote ID: ${response.orderId}`;
        this.successAction = 'quote';
        this.savingAsQuote[partSupplierId] = false;
        // Clear the quantity input after saving
        this.quantities[partSupplierId] = 1;
        this.cdr.detectChanges();
        
        // Clear success message after 5 seconds
        setTimeout(() => {
          this.successMessage = '';
          this.successAction = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Error saving quote:', err);
        console.error('Error status:', err.status);
        console.error('Error body:', err.error);
        this.error = err.error?.message || err.message || 'Failed to save quote. Please try again.';
        this.savingAsQuote[partSupplierId] = false;
        this.cdr.detectChanges();
      }
    });
  }
}