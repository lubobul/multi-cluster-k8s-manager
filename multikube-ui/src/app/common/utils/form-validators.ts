import {AbstractControl, FormGroup, ValidationErrors, ValidatorFn} from '@angular/forms';

export const FormValidators = {
    /**
     * Validates if two passwords match
     * @param key1 - the key for the control containing first value
     * @param key2 - the key for the control containing second value
     */
    matchPasswords: function (key1: string, key2: string): Function {

        return (control: FormGroup): ValidationErrors | null => {

            const control1 = control.controls[key1];
            const control2 = control.controls[key2];
            const val1 = control1.value;
            const val2 = control2.value;

            if ((val1 === val2)) {
                control2.setErrors(null);
                return null;
            }

            if (val1 !== val2) {
                control2.setErrors({"passwordDoesntMatch": true});
            }

            return null;
        };
    },

    dnsCompliantValidator: function (): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            // Get the value from the form control.
            const value = control.value;

            // Don't validate empty values, let the 'required' validator handle that.
            if (!value) {
                return null;
            }

            // The regex for DNS-compliant names (e.g., Kubernetes namespaces, services).
            const dnsRegex = /^[a-z0-9]([-a-z0-9]*[a-z0-9])?$/;

            // Test the value against the regex.
            const isValid = dnsRegex.test(value);

            // If the value is valid, return null. Otherwise, return the specific error object.
            return isValid ? null : { dnsIncompliant: true };
        };
    }
}
