/**
 * Form validation utility.
 */

export interface FormField {
  value: unknown
  rules: ValidationRule[]
  valid: boolean
  errors: string[]
}

export interface ValidationRule {
  type: 'required' | 'min' | 'max' | 'pattern' | 'custom'
  value?: unknown
  message: string
  validator?: (value: unknown) => boolean
}

export interface FormValidationResult {
  valid: boolean
  fields: Record<string, FormField>
  errors: string[]
}

/**
 * Validate a single field.
 *
 * @param value Field value
 * @param rules Validation rules
 * @returns Validation result
 */
export function validateField(value: unknown, rules: ValidationRule[]): { valid: boolean; errors: string[] } {
  const errors: string[] = []
  
  for (const rule of rules) {
    switch (rule.type) {
      case 'required':
        if (value === null || value === undefined || value === '') {
          errors.push(rule.message)
        }
        break
        
      case 'min':
        if (typeof value === 'string' && value.length < (rule.value as number)) {
          errors.push(rule.message)
        } else if (typeof value === 'number' && value < (rule.value as number)) {
          errors.push(rule.message)
        }
        break
        
      case 'max':
        if (typeof value === 'string' && value.length > (rule.value as number)) {
          errors.push(rule.message)
        } else if (typeof value === 'number' && value > (rule.value as number)) {
          errors.push(rule.message)
        }
        break
        
      case 'pattern':
        if (typeof value === 'string' && !(rule.value as RegExp).test(value)) {
          errors.push(rule.message)
        }
        break
        
      case 'custom':
        if (rule.validator && !rule.validator(value)) {
          errors.push(rule.message)
        }
        break
    }
  }
  
  return {
    valid: errors.length === 0,
    errors,
  }
}

/**
 * Validate form fields.
 *
 * @param fields Form fields
 * @returns Form validation result
 */
export function validateForm(fields: Record<string, FormField>): FormValidationResult {
  const validatedFields: Record<string, FormField> = {}
  const allErrors: string[] = []
  let valid = true
  
  for (const [name, field] of Object.entries(fields)) {
    const result = validateField(field.value, field.rules)
    
    validatedFields[name] = {
      ...field,
      valid: result.valid,
      errors: result.errors,
    }
    
    if (!result.valid) {
      valid = false
      allErrors.push(...result.errors)
    }
  }
  
  return {
    valid,
    fields: validatedFields,
    errors: allErrors,
  }
}

/**
 * Create form field.
 *
 * @param value Initial value
 * @param rules Validation rules
 * @returns Form field
 */
export function createFormField(value: unknown, rules: ValidationRule[] = []): FormField {
  return {
    value,
    rules,
    valid: true,
    errors: [],
  }
}

/**
 * Common validation rules.
 */
export const validationRules = {
  required: (message: string = '此字段为必填项'): ValidationRule => ({
    type: 'required',
    message,
  }),
  
  min: (min: number, message?: string): ValidationRule => ({
    type: 'min',
    value: min,
    message: message || `最小值为${min}`,
  }),
  
  max: (max: number, message?: string): ValidationRule => ({
    type: 'max',
    value: max,
    message: message || `最大值为${max}`,
  }),
  
  minLength: (min: number, message?: string): ValidationRule => ({
    type: 'min',
    value: min,
    message: message || `最少输入${min}个字符`,
  }),
  
  maxLength: (max: number, message?: string): ValidationRule => ({
    type: 'max',
    value: max,
    message: message || `最多输入${max}个字符`,
  }),
  
  pattern: (pattern: RegExp, message: string): ValidationRule => ({
    type: 'pattern',
    value: pattern,
    message,
  }),
  
  email: (message: string = '请输入有效的邮箱地址'): ValidationRule => ({
    type: 'pattern',
    value: /^[^s@]+@[^s@]+.[^s@]+$/,
    message,
  }),
  
  phone: (message: string = '请输入有效的手机号码'): ValidationRule => ({
    type: 'pattern',
    value: /^1[3-9]d{9}$/,
    message,
  }),
  
  custom: (validator: (value: unknown) => boolean, message: string): ValidationRule => ({
    type: 'custom',
    validator,
    message,
  }),
}