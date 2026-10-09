import { Component, Input, forwardRef, HostListener, ElementRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export interface SelectOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-custom-select',
  standalone: true,
  templateUrl: './custom-select.component.html',
  styleUrl: './custom-select.component.css',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => CustomSelectComponent),
    multi: true,
  }],
})
export class CustomSelectComponent implements ControlValueAccessor {
  @Input() options: SelectOption[] = [];
  @Input() placeholder = 'Select…';

  value = '';
  open = false;
  disabled = false;

  private onChange: (v: string) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private el: ElementRef) {}

  @HostListener('document:click', ['$event.target'])
  onDocumentClick(target: EventTarget | null): void {
    if (!this.el.nativeElement.contains(target)) this.open = false;
  }

  get selectedLabel(): string {
    return this.options.find(o => o.value === this.value)?.label ?? '';
  }

  toggle(): void {
    if (!this.disabled) this.open = !this.open;
  }

  select(value: string): void {
    this.value = value;
    this.open = false;
    this.onChange(value);
    this.onTouched();
  }

  writeValue(val: string | null): void { this.value = val ?? ''; }
  registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(disabled: boolean): void { this.disabled = disabled; }
}
