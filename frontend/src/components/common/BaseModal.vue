<script setup lang="ts">
import { computed, ref } from 'vue'
import { NModal, NCard, NButton, NIcon } from 'naive-ui'
import { Close } from '@vicons/ionicons5'

const props = withDefaults(defineProps<{
  /** Whether the modal is visible */
  visible: boolean
  /** Modal title */
  title?: string
  /** Modal width */
  width?: number | string
  /** Whether the modal is loading */
  loading?: boolean
  /** Whether to show close button */
  showClose?: boolean
  /** Whether to show footer */
  showFooter?: boolean
  /** Whether to show cancel button */
  showCancel?: boolean
  /** Cancel text */
  cancelText?: string
  /** Confirm text */
  confirmText?: string
  /** Whether the confirm button is loading */
  confirmLoading?: boolean
  /** Whether the confirm button is disabled */
  confirmDisabled?: boolean
  /** Custom class */
  class?: string
}>(), {
  loading: false,
  showClose: true,
  showFooter: true,
  showCancel: true,
  cancelText: '取消',
  confirmText: '确定',
  confirmLoading: false,
  confirmDisabled: false,
})

const emit = defineEmits<{
  'update:visible': [visible: boolean]
  close: []
  cancel: []
  confirm: []
}>()

const classes = computed(() => [
  'base-modal',
  props.class,
])

function handleClose() {
  emit('update:visible', false)
  emit('close')
}

function handleCancel() {
  emit('cancel')
  handleClose()
}

function handleConfirm() {
  emit('confirm')
}
</script>

<template>
  <n-modal
    :show="visible"
    :class="classes"
    @update:show="emit('update:visible', $event)"
  >
    <n-card
      :title="title"
      :width="width"
      :bordered="false"
      :closable="showClose"
      @close="handleClose"
    >
      <template v-if="showClose" #header-extra>
        <n-button quaternary circle size="small" @click="handleClose">
          <template #icon>
            <n-icon :component="Close" />
          </template>
        </n-button>
      </template>
      
      <div class="base-modal__content">
        <slot />
      </div>
      
      <template v-if="showFooter" #footer>
        <div class="base-modal__footer">
          <slot name="footer">
            <n-button v-if="showCancel" @click="handleCancel">
              {{ cancelText }}
            </n-button>
            <n-button
              type="primary"
              :loading="confirmLoading"
              :disabled="confirmDisabled"
              @click="handleConfirm"
            >
              {{ confirmText }}
            </n-button>
          </slot>
        </div>
      </template>
    </n-card>
  </n-modal>
</template>

<style scoped>
.base-modal {
  max-width: 90vw;
}

.base-modal__content {
  min-height: 100px;
}

.base-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--sp-3);
}
</style>