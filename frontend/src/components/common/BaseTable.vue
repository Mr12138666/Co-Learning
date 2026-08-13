<script setup lang="ts">
import { computed, ref } from 'vue'
import { NDataTable, NEmpty, NSpin } from 'naive-ui'
import type { DataTableColumns, DataTableRowKey } from 'naive-ui'

const props = withDefaults(defineProps<{
  /** Table data */
  data: any[]
  /** Table columns */
  columns: DataTableColumns<unknown>
  /** Whether the table is loading */
  loading?: boolean
  /** Whether the table is empty */
  empty?: boolean
  /** Empty text */
  emptyText?: string
  /** Whether the table is striped */
  striped?: boolean
  /** Whether the table is bordered */
  bordered?: boolean
  /** Whether the table is single line */
  singleLine?: boolean
  /** Whether the table has hover effect */
  hoverable?: boolean
  /** Table size */
  size?: 'small' | 'medium' | 'large'
  /** Whether the table has pagination */
  pagination?: boolean
  /** Page size */
  pageSize?: number
  /** Custom class */
  class?: string
}>(), {
  loading: false,
  empty: false,
  emptyText: '暂无数据',
  striped: false,
  bordered: false,
  singleLine: true,
  hoverable: true,
  size: 'medium',
  pagination: false,
  pageSize: 10,
})

const emit = defineEmits<{
  'update:page': [page: number]
  'update:pageSize': [pageSize: number]
  'row-click': [row: unknown]
  'selection-change': [keys: DataTableRowKey[]]
}>()

const currentPage = ref(1)
const currentPageSize = ref(props.pageSize)

const classes = computed(() => [
  'base-table',
  `base-table--${props.size}`,
  {
    'base-table--striped': props.striped,
    'base-table--bordered': props.bordered,
    'base-table--hoverable': props.hoverable,
  },
  props.class,
])

const paginationConfig = computed(() => {
  if (!props.pagination) return false
  return {
    page: currentPage.value,
    pageSize: currentPageSize.value,
    pageCount: Math.ceil(props.data.length / currentPageSize.value),
    showSizePicker: true,
    pageSizes: [10, 20, 30, 50],
    onChange: (page: number) => {
      currentPage.value = page
      emit('update:page', page)
    },
    onUpdatePageSize: (pageSize: number) => {
      currentPageSize.value = pageSize
      currentPage.value = 1
      emit('update:pageSize', pageSize)
    },
  }
})

function handleRowClick(row: unknown) {
  emit('row-click', row)
}

function handleSelectionChange(keys: DataTableRowKey[]) {
  emit('selection-change', keys)
}
</script>

<template>
  <div :class="classes">
    <n-spin :show="loading">
      <n-data-table
        v-if="!empty"
        :columns="columns"
        :data="data"
        :pagination="paginationConfig"
        :striped="striped"
        :bordered="bordered"
        :single-line="singleLine"
        :size="size"
        @row-click="handleRowClick"
        @update:checked-row-keys="handleSelectionChange"
      />
      <n-empty v-else :description="emptyText" />
    </n-spin>
  </div>
</template>

<style scoped>
.base-table {
  width: 100%;
}

.base-table--small :deep(.n-data-table) {
  font-size: var(--text-sm);
}

.base-table--medium :deep(.n-data-table) {
  font-size: var(--text-base);
}

.base-table--large :deep(.n-data-table) {
  font-size: var(--text-lg);
}

.base-table--hoverable :deep(.n-data-table-tr:hover) {
  background: var(--surface-2);
}

.base-table--bordered :deep(.n-data-table) {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.base-table--bordered :deep(.n-data-table-th),
.base-table--bordered :deep(.n-data-table-td) {
  border-right: 1px solid var(--border-color);
}

.base-table--bordered :deep(.n-data-table-tr:last-child .n-data-table-td) {
  border-bottom: none;
}
</style>