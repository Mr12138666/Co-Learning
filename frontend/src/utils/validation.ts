/**
 * Validation utility functions.
 */

export interface ValidationRule {
  required?: boolean
  min?: number
  max?: number
  pattern?: RegExp
  message: string
}

export interface ValidationResult {
  valid: boolean
  errors: string[]
}

/**
 * Validate value against rules.
 *
 * @param value Value to validate
 * @param rules Validation rules
 * @returns Validation result
 */
export function validate(value: unknown, rules: ValidationRule[]): ValidationResult {
  const errors: string[] = []
  
  for (const rule of rules) {
    // Required check
    if (rule.required && (value === null || value === undefined || value === '')) {
      errors.push(rule.message)
      continue
    }
    
    if (value === null || value === undefined || value === '') {
      continue
    }
    
    // Min length/value check
    if (rule.min !== undefined) {
      if (typeof value === 'string' && value.length < rule.min) {
        errors.push(rule.message)
      } else if (typeof value === 'number' && value < rule.min) {
        errors.push(rule.message)
      }
    }
    
    // Max length/value check
    if (rule.max !== undefined) {
      if (typeof value === 'string' && value.length > rule.max) {
        errors.push(rule.message)
      } else if (typeof value === 'number' && value > rule.max) {
        errors.push(rule.message)
      }
    }
    
    // Pattern check
    if (rule.pattern && typeof value === 'string' && !rule.pattern.test(value)) {
      errors.push(rule.message)
    }
  }
  
  return {
    valid: errors.length === 0,
    errors,
  }
}

/**
 * Common validation rules.
 */
export const rules = {
  required: (message: string = '此字段为必填项'): ValidationRule => ({
    required: true,
    message,
  }),
  
  minLength: (min: number, message?: string): ValidationRule => ({
    min,
    message: message || `最少输入${min}个字符`,
  }),
  
  maxLength: (max: number, message?: string): ValidationRule => ({
    max,
    message: message || `最多输入${max}个字符`,
  }),
  
  email: (message: string = '请输入有效的邮箱地址'): ValidationRule => ({
    pattern: /^[^s@]+@[^s@]+.[^s@]+$/,
    message,
  }),
  
  phone: (message: string = '请输入有效的手机号码'): ValidationRule => ({
    pattern: /^1[3-9]\d{9}$/,
    message,
  }),
  
  url: (message: string = '请输入有效的URL'): ValidationRule => ({
    pattern: /^https?:\/\/.+/,
    message,
  }),
  
  numeric: (message: string = '请输入数字'): ValidationRule => ({
    pattern: /^\d+$/,
    message,
  }),
  
  alpha: (message: string = '只能输入字母'): ValidationRule => ({
    pattern: /^[a-zA-Z]+$/,
    message,
  }),
  
  alphanumeric: (message: string = '只能输入字母和数字'): ValidationRule => ({
    pattern: /^[a-zA-Z0-9]+$/,
    message,
  }),
  
  minValue: (min: number, message?: string): ValidationRule => ({
    min,
    message: message || `最小值为${min}`,
  }),
  
  maxValue: (max: number, message?: string): ValidationRule => ({
    max,
    message: message || `最大值为${max}`,
  }),
}