<script setup lang="ts">
import { computed, ref } from 'vue'
import { NForm, NFormItem, NInput, NInputNumber, NSelect, NSwitch, NDatePicker } from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'

const props = withDefaults(defineProps<{
  /** Form model */
  model: Record<string, unknown>
  /** Form rules */
  rules?: FormRules
  /** Whether the form is loading */
  loading?: boolean
  /** Whether the form is disabled */
  disabled?: boolean
  /** Form label placement */
  labelPlacement?: 'left' | 'top'
  /** Form label width */
  labelWidth?: number | string
  /** Whether to show required mark */
  showRequireMark?: boolean
  /** Custom class */
  class?: string
}>(), {
  loading: false,
  disabled: false,
  labelPlacement: 'left',
  showRequireMark: true,
})

const emit = defineEmits<{
  submit: [model: Record<string, unknown>]
  reset: []
}>()

const formRef = ref<FormInst | null>(null)

const classes = computed(() => [
  'base-form',
  `base-form--${props.labelPlacement}`,
  props.class,
])

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    emit('submit', props.model)
  } catch (errors) {
    // Validation failed
    console.error('Form validation failed:', errors)
  }
}

function handleReset() {
  formRef.value?.restoreValidation()
  emit('reset')
}

function validate() {
  return formRef.value?.validate()
}

function restoreValidation() {
  formRef.value?.restoreValidation()
}

defineExpose({
  validate,
  restoreValidation,
  handleSubmit,
  handleReset,
})
</script>

<template>
  <n-form
    ref="formRef"
    :model="model"
    :rules="rules"
    :label-placement="labelPlacement"
    :label-width="labelWidth"
    :show-require-mark="showRequireMark"
    :disabled="disabled || loading"
    :class="classes"
  >
    <slot />
    <div v-if="$slots.actions" class="base-form__actions">
      <slot name="actions" :submit="handleSubmit" :reset="handleReset" :loading="loading" />
    </div>
  </n-form>
</template>

<style scoped>
.base-form {
  width: 100%;
}

.base-form--left :deep(.n-form-item-label) {
  text-align: right;
  padding-right: var(--sp-3);
}

.base-form--top :deep(.n-form-item-label) {
  text-align: left;
  margin-bottom: var(--sp-1);
}

.base-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--sp-3);
  margin-top: var(--sp-6);
  padding-top: var(--sp-4);
  border-top: 1px solid var(--border-color);
}
</style>