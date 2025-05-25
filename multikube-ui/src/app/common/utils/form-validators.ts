import {FormGroup, ValidationErrors} from '@angular/forms';

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
}
