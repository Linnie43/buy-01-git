import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface DialogData {
  title: string;
  message: string;
}

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
    template: `
    <div class="confirmation-dialog">
       <h1 class="dialog-title">{{ data.title }}</h1>
          <div class="dialog-body">
            <p>{{ data.message }}</p>
          </div>
          <div class="dialog-actions">
            <button type="button" class="btn btn-primary" (click)="onCancel()">Cancel</button>
            <button type="button" class="btn btn-danger" [mat-dialog-close]="true" cdkFocusInitial>Delete</button>
          </div>
    </div>
    `
  })

export class ConfirmationDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
