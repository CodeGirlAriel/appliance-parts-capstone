import { Component, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../api';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'app-cart',
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './cart.html'
})
export class CartComponent {

  cartItems: any[] = []; // QUOTE status orders
  loading: boolean = true;
  error: string = '';
  successMessage: string = '';
  deleting: { [key: number]: boolean } = {}; // Track deleting state per item
  updating: { [key: number]: boolean } = {}; // Track updating state per item
  editingItem: { [key: number]: boolean } = {}; // Track which item is being edited
  supplierOptions: { [key: number]: any[] } = {}; // Store supplier options for each item
  selectedSupplier: { [key: number]: number | null } = {}; // Track selected supplier for each item
  checkingOut: { [key: number]: boolean } = {}; // Track checkout state per item
  checkingOutAll: boolean = false; // Track checkout all state

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {
    this.loadCart();
  }

  loadCart() {
    this.loading = true;
    this.error = '';
    this.cartItems = [];
    
    console.log('Loading cart items...');
    
    this.api.getCartItems().subscribe({
      next: (data) => {
        console.log('Cart items received:', data);
        this.cartItems = Array.isArray(data) ? data : [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading cart:', err);
        if (err.status === 0 || err.message?.includes('Failed to fetch')) {
          this.error = 'Cannot connect to backend. Please make sure the Spring Boot server is running.';
        } else if (err.status === 500) {
          this.error = 'Server error: ' + (err.error?.message || 'Internal server error');
        } else {
          this.error = 'Failed to load cart: ' + (err.error?.message || err.message || 'Unknown error');
        }
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  deleteItem(orderId: number) {
    if (!confirm(`Are you sure you want to remove this item from your cart?`)) {
      return;
    }

    this.deleting[orderId] = true;
    this.error = '';
    this.successMessage = '';

    console.log('Deleting cart item:', orderId);

    this.api.deleteQuote(orderId).subscribe({
      next: () => {
        console.log('Cart item deleted successfully');
        this.successMessage = `Item removed from cart.`;
        this.deleting[orderId] = false;
        this.loadCart();
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (err) => {
        console.error('Error deleting cart item:', err);
        if (err.status === 404) {
          this.error = 'Item not found. It may have already been removed.';
        } else if (err.status === 500) {
          this.error = 'Server error: ' + (err.error?.message || 'Internal server error');
        } else {
          this.error = 'Failed to remove item: ' + (err.error?.message || err.message || 'Unknown error');
        }
        this.deleting[orderId] = false;
        this.cdr.detectChanges();
      }
    });
  }

  startEditItem(item: any) {
    const orderId = item.orderId;
    this.editingItem[orderId] = true;
    this.error = '';
    this.successMessage = '';
    
    const partId = item.items && item.items.length > 0 ? item.items[0].part.partId : null;
    
    if (partId) {
      this.api.comparePart(partId).subscribe({
        next: (comparison) => {
          this.supplierOptions[orderId] = comparison.options || [];
          if (item.items && item.items.length > 0) {
            const currentSupplierId = item.items[0].partSupplierId || item.items[0].selectedSupplier?.partSupplierId;
            this.selectedSupplier[orderId] = currentSupplierId || (comparison.options && comparison.options.length > 0 ? comparison.options[0].partSupplierId : null);
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error loading supplier options:', err);
          this.error = 'Failed to load supplier options. Please try again.';
          this.editingItem[orderId] = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  cancelEditItem(orderId: number) {
    this.editingItem[orderId] = false;
    this.supplierOptions[orderId] = [];
    this.selectedSupplier[orderId] = null;
    this.error = '';
    this.cdr.detectChanges();
  }

  updateItemSupplier(item: any) {
    const orderId = item.orderId;
    const newPartSupplierId = this.selectedSupplier[orderId];
    
    if (!newPartSupplierId) {
      this.error = 'Please select a supplier.';
      this.cdr.detectChanges();
      return;
    }
    
    const currentSupplierId = item.items && item.items.length > 0 
      ? (item.items[0].partSupplierId || item.items[0].selectedSupplier?.partSupplierId)
      : null;
    
    if (currentSupplierId === newPartSupplierId) {
      this.error = 'Please select a different supplier.';
      this.cdr.detectChanges();
      return;
    }

    this.updating[orderId] = true;
    this.error = '';
    this.successMessage = '';

    this.api.updateQuoteSupplier(orderId, newPartSupplierId).subscribe({
      next: (updatedItem) => {
        console.log('Cart item updated successfully:', updatedItem);
        this.successMessage = `Cart item updated successfully.`;
        this.updating[orderId] = false;
        this.editingItem[orderId] = false;
        this.supplierOptions[orderId] = [];
        this.selectedSupplier[orderId] = null;
        this.loadCart();
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (err) => {
        console.error('Error updating cart item:', err);
        this.error = err.error?.message || err.message || 'Failed to update item.';
        this.updating[orderId] = false;
        this.cdr.detectChanges();
      }
    });
  }

  checkoutItem(orderId: number) {
    if (!confirm(`Are you sure you want to purchase this item? This will place your order.`)) {
      return;
    }

    this.checkingOut[orderId] = true;
    this.error = '';
    this.successMessage = '';

    console.log('Checking out item:', orderId);

    this.api.checkout(orderId).subscribe({
      next: (order) => {
        console.log('Item checked out successfully:', order);
        this.successMessage = `Order #${orderId} placed successfully! Status: ${order.status}`;
        this.checkingOut[orderId] = false;
        this.loadCart(); // Reload to remove purchased item from cart
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Error checking out:', err);
        if (err.status === 404) {
          this.error = 'Item not found.';
        } else if (err.status === 500) {
          this.error = 'Server error: ' + (err.error?.message || 'Internal server error');
        } else {
          this.error = 'Failed to checkout: ' + (err.error?.message || err.message || 'Unknown error');
        }
        this.checkingOut[orderId] = false;
        this.cdr.detectChanges();
      }
    });
  }

  checkoutAll() {
    if (this.cartItems.length === 0) {
      this.error = 'Your cart is empty.';
      return;
    }

    if (!confirm(`Are you sure you want to purchase all ${this.cartItems.length} item(s) in your cart?`)) {
      return;
    }

    this.checkingOutAll = true;
    this.error = '';
    this.successMessage = '';

    // Checkout all items sequentially
    let completed = 0;
    const total = this.cartItems.length;

    this.cartItems.forEach((item) => {
      this.api.checkout(item.orderId).subscribe({
        next: () => {
          completed++;
          if (completed === total) {
            this.successMessage = `All ${total} item(s) purchased successfully!`;
            this.checkingOutAll = false;
            this.loadCart();
            setTimeout(() => {
              this.successMessage = '';
              this.cdr.detectChanges();
            }, 5000);
          }
        },
        error: (err) => {
          console.error('Error checking out item:', item.orderId, err);
          this.error = `Failed to checkout item #${item.orderId}: ${err.error?.message || err.message}`;
          this.checkingOutAll = false;
          this.cdr.detectChanges();
        }
      });
    });
  }

  calculateTotal(): number {
    return this.cartItems.reduce((sum, item) => sum + parseFloat(item.totalAmount || 0), 0);
  }

  getCartItemCount(): number {
    return this.cartItems.length;
  }
}

