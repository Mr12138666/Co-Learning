<script setup lang="ts">
import { computed } from 'vue'
import { Check, Pencil, Trash2, Calendar, GripVertical } from 'lucide-vue-next'
import type { StudyTask } from '@/api/study'
import { formatDuration, formatDurationFromMinutes } from '@/utils/format'
import dayjs from 'dayjs'

const props = withDefaults(
  defineProps<{
    task: StudyTask
    /** Show a drag handle on the left (for sortable lists). */
    draggable?: boolean
    /** Compact card variant used inside Boards columns. */
    card?: boolean
    /** Show the planned-date chip (hidden in per-day Planner columns). */
    showPlannedDate?: boolean
  }>(),
  { draggable: false, card: false, showPlannedDate: true },
)

const emit = defineEmits<{
  toggle: [task: StudyTask]
  open: [task: StudyTask]
  edit: [task: StudyTask]
  delete: [task: StudyTask]
}>()

const done = computed(() => props.task.status === 'DONE')

const plannedLabel = computed(() => {
  if (!props.task.plannedDate) return ''
  const d = dayjs(props.task.plannedDate)
  return d.isSame(dayjs(), 'year') ? d.format('M/D') : d.format('YY/M/D')
})

const plannedOverdue = computed(
  () => !!props.task.plannedDate && !done.value && dayjs(props.task.plannedDate).isBefore(dayjs(), 'day'),
)

const estimatedLabel = computed(() =>
  props.task.estimatedMinutes ? formatDurationFromMinutes(props.task.estimatedMinutes) : '',
)
const actualLabel = computed(() =>
  props.task.totalFocusSeconds > 0 ? formatDuration(props.task.totalFocusSeconds) : '',
)

const hasMeta = computed(
  () =>
    !!props.task.subjectName ||
    props.task.tags.length > 0 ||
    (props.showPlannedDate && !!plannedLabel.value),
)
</script>

<template>
  <div class="task-row" :class="{ 'task-row--done': done, 'task-row--card': card }">
    <span v-if="draggable" class="task-row__handle" aria-hidden="true">
      <GripVertical :size="15" />
    </span>

    <button
      class="task-row__check"
      :class="{ 'is-done': done }"
      type="button"
      role="checkbox"
      :aria-checked="done"
      :aria-label="done ? '标记为未完成' : '标记为完成'"
      @click.stop="emit('toggle', task)"
    >
      <Check v-if="done" :size="13" :stroke-width="3" />
    </button>

    <div class="task-row__body" @click="emit('open', task)">
      <div class="task-row__title-line">
        <span class="task-row__title">{{ task.title }}</span>
        <span v-if="task.important" class="task-row__flag task-row__flag--important" title="重要">!</span>
        <span v-if="task.urgent" class="task-row__flag task-row__flag--urgent" title="紧急">∗</span>
      </div>

      <div v-if="hasMeta" class="task-row__meta">
        <span v-if="task.subjectName" class="task-row__subject">
          <span class="task-row__dot" :style="{ background: task.subjectColor || 'var(--text-color-muted)' }" />
          {{ task.subjectName }}
        </span>
        <span
          v-for="tag in task.tags"
          :key="tag.id"
          class="task-row__tag"
          :style="{ color: tag.color, borderColor: tag.color }"
        >{{ tag.name }}</span>
        <span v-if="showPlannedDate && plannedLabel" class="task-row__chip" :class="{ 'is-overdue': plannedOverdue }">
          <Calendar :size="11" /> {{ plannedLabel }}
        </span>
      </div>
    </div>

    <div class="task-row__right">
      <span v-if="estimatedLabel" class="task-row__time" title="预计时长">{{ estimatedLabel }}</span>
      <span v-if="actualLabel" class="task-row__time task-row__time--actual" title="已专注">{{ actualLabel }}</span>
      <div class="task-row__actions">
        <button class="task-row__act" type="button" aria-label="编辑任务" @click.stop="emit('edit', task)">
          <Pencil :size="14" />
        </button>
        <button class="task-row__act task-row__act--danger" type="button" aria-label="删除任务" @click.stop="emit('delete', task)">
          <Trash2 :size="14" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.task-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  min-height: 40px;
  padding: var(--sp-1) var(--sp-2);
  border-radius: var(--radius-sm);
  background: var(--bg-card);
  border: 1px solid var(--divider);
  transition: background-color var(--transition-fast), border-color var(--transition-fast),
    opacity var(--transition-standard);
}
.task-row:hover {
  background: var(--state-hover);
  border-color: var(--border-color);
}
.task-row--card {
  background: var(--surface-3);
}
.task-row--done {
  opacity: 0.6;
}

.task-row__handle {
  display: flex;
  align-items: center;
  color: var(--text-color-muted);
  cursor: grab;
  opacity: 0;
  transition: opacity var(--transition-fast);
  flex-shrink: 0;
}
.task-row:hover .task-row__handle,
.task-row:focus-within .task-row__handle {
  opacity: 0.7;
}

.task-row__check {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--separator);
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-on-accent);
  cursor: pointer;
  transition: background-color var(--transition-fast), border-color var(--transition-fast),
    transform var(--transition-fast), box-shadow var(--transition-fast);
}
.task-row__check:hover {
  border-color: var(--success);
  box-shadow: 0 0 0 3px var(--success-muted);
}
.task-row__check:active {
  transform: scale(0.88);
}
.task-row__check.is-done {
  background: var(--success);
  border-color: var(--success);
  animation: scaleIn var(--duration-sm) var(--ease-bounce);
}

.task-row__body {
  flex: 1;
  min-width: 0;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 2px 0;
}
.task-row__title-line {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  min-width: 0;
}
.task-row__title {
  font-size: var(--text-base);
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.task-row--done .task-row__title {
  text-decoration: line-through;
  color: var(--text-color-muted);
}

.task-row__flag {
  flex-shrink: 0;
  font-weight: var(--weight-bold);
  font-size: var(--text-sm);
  line-height: 1;
}
.task-row__flag--important {
  color: var(--danger);
}
.task-row__flag--urgent {
  color: var(--urgent);
}

.task-row__meta {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  flex-wrap: wrap;
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}
.task-row__subject {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.task-row__dot {
  width: 7px;
  height: 7px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}
.task-row__tag {
  padding: 0 6px;
  border: 1px solid;
  border-radius: var(--radius-pill);
  font-size: var(--text-xs);
  opacity: 0.9;
}
.task-row__chip {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.task-row__chip.is-overdue {
  color: var(--danger);
}

.task-row__right {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  flex-shrink: 0;
}
.task-row__time {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  font-variant-numeric: tabular-nums;
}
.task-row__time--actual {
  color: var(--success);
}

.task-row__actions {
  display: flex;
  align-items: center;
  gap: 2px;
  opacity: 0;
  transition: opacity var(--transition-fast);
}
.task-row:hover .task-row__actions,
.task-row:focus-within .task-row__actions {
  opacity: 1;
}
.task-row__act {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  color: var(--text-color-muted);
  cursor: pointer;
  transition: background-color var(--transition-fast), color var(--transition-fast);
}
.task-row__act:hover {
  background: var(--state-hover);
  color: var(--text-color);
}
.task-row__act--danger:hover {
  color: var(--danger);
}
</style>
