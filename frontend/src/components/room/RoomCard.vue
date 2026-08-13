<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { RoomResponse } from '@/api/room'

const props = defineProps<{
  room: RoomResponse
}>()

const router = useRouter()

const statusTag = computed(() => {
  if (props.room.status === 'CLOSED') return { type: 'error' as const, label: '已关闭' }
  return { type: 'success' as const, label: '活跃' }
})

const visibilityTag = computed(() => {
  if (props.room.visibility === 'PRIVATE') return { type: 'warning' as const, label: '私密' }
  return null
})

const memberFill = computed(() => {
  return Math.round((props.room.memberCount / props.room.maxMembers) * 100)
})

function enterRoom() {
  router.push(`/rooms/${props.room.id}`)
}
</script>

<template>
  <div class="room-card glass interactive stagger-in" @click="enterRoom">
    <div class="card-header">
      <span class="card-name">{{ room.name }}</span>
      <div class="card-tags">
        <span class="tag-badge" :class="statusTag.type">{{ statusTag.label }}</span>
        <span v-if="visibilityTag" class="tag-badge warning">{{ visibilityTag.label }}</span>
      </div>
    </div>

    <p v-if="room.description" class="card-desc">{{ room.description }}</p>

    <div v-if="room.topic" class="card-topic">{{ room.topic }}</div>

    <div class="card-footer">
      <div class="card-owner">
        <n-avatar round size="small" :src="room.ownerAvatar || undefined">
          {{ room.ownerName?.charAt(0) }}
        </n-avatar>
        <span class="owner-name">{{ room.ownerName }}</span>
      </div>

      <div class="card-meta">
        <span class="member-bar">
          <span class="member-fill" :style="{ width: memberFill + '%' }" />
        </span>
        <span class="member-text">{{ room.memberCount }}/{{ room.maxMembers }}</span>
        <span v-if="room.isMember" class="joined-dot" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.room-card {
  border-radius: var(--radius-lg);
  padding: var(--sp-3) var(--sp-4);
  cursor: pointer;
}

/* Hover lift + brand glow (scoped wins over .glass:hover / .interactive:hover) */
.room-card:hover {
  transform: translateY(-3px);
  border-color: rgba(59, 130, 246, 0.22);
  box-shadow:
    0 8px 32px rgba(59, 130, 246, 0.06),
    0 0 0 1px rgba(59, 130, 246, 0.1),
    0 0 24px rgba(59, 130, 246, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-2);
}

.card-name {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-tags {
  display: flex;
  gap: var(--sp-1);
  flex-shrink: 0;
}

.tag-badge {
  font-size: var(--text-xs);
  font-weight: var(--weight-medium);
  padding: 1px var(--sp-1);
  border-radius: var(--radius-xs);
  line-height: 1.5;
}

.tag-badge.success {
  background: var(--success-muted);
  color: var(--success);
}

.tag-badge.error {
  background: var(--danger-muted);
  color: var(--danger);
}

.tag-badge.warning {
  background: var(--warning-muted);
  color: var(--warning);
}

.card-desc {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
  margin: var(--sp-1) 0 0 0;
  line-height: var(--leading-normal);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-topic {
  display: inline-block;
  font-size: var(--text-xs);
  color: var(--brand);
  background: var(--brand-subtle);
  padding: 1px var(--sp-2);
  border-radius: var(--radius-xs);
  margin-top: var(--sp-2);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--sp-2);
  padding-top: var(--sp-2);
  border-top: 1px solid var(--divider);
}

.card-owner {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
}

.owner-name {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

.card-meta {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.member-bar {
  width: 40px;
  height: 4px;
  background: var(--bg-sunken);
  border-radius: var(--radius-pill);
  overflow: hidden;
}

.member-fill {
  display: block;
  height: 100%;
  background: var(--brand);
  border-radius: var(--radius-pill);
  transition: width var(--transition-standard);
}

.member-text {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  font-variant-numeric: tabular-nums;
}

.joined-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--success);
  flex-shrink: 0;
}
</style>
