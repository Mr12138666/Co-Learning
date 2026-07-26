<script setup lang="ts">
import { computed } from 'vue'
import type { RoomMemberResponse } from '@/api/room'

const props = defineProps<{
  members: RoomMemberResponse[]
  onlineUserIds: Set<number>
}>()

const _emit = defineEmits<{
  kick: [userId: number]
  mute: [userId: number]
}>()

const sortedMembers = computed(() => {
  return [...props.members].sort((a, b) => {
    const aOnline = props.onlineUserIds.has(a.userId) ? 0 : 1
    const bOnline = props.onlineUserIds.has(b.userId) ? 0 : 1
    if (aOnline !== bOnline) return aOnline - bOnline

    const roleOrder = { OWNER: 0, ADMIN: 1, MEMBER: 2 }
    const aRole = roleOrder[a.role as keyof typeof roleOrder] ?? 3
    const bRole = roleOrder[b.role as keyof typeof roleOrder] ?? 3
    if (aRole !== bRole) return aRole - bRole

    return a.displayName.localeCompare(b.displayName)
  })
})

function getFocusTag(focusStatus: string | null) {
  if (focusStatus === 'STUDYING') return { type: 'success' as const, label: '学习中' }
  if (focusStatus === 'PAUSED') return { type: 'warning' as const, label: '暂停中' }
  return null
}

function getRoleTag(role: string) {
  if (role === 'OWNER') return { type: 'error' as const, label: '房主' }
  if (role === 'ADMIN') return { type: 'warning' as const, label: '管理员' }
  return null
}
</script>

<template>
  <div class="member-list">
    <div class="member-list-header">
      <span class="header-title">成员</span>
      <span class="online-count">{{ onlineUserIds.size }} 在线</span>
    </div>

    <div class="member-scroll">
      <div
        v-for="member in sortedMembers"
        :key="member.userId"
        class="member-item"
        :class="{ offline: !onlineUserIds.has(member.userId) }"
      >
        <div class="member-avatar">
          <n-avatar round size="small" :src="member.avatarUrl || undefined">
            {{ member.displayName?.charAt(0) }}
          </n-avatar>
          <span
            class="online-dot"
            :class="{
              online: onlineUserIds.has(member.userId),
              studying: member.focusStatus === 'STUDYING',
              paused: member.focusStatus === 'PAUSED',
            }"
          />
        </div>

        <div class="member-info">
          <div class="member-name-row">
            <span class="member-name">{{ member.displayName }}</span>
            <span v-if="getRoleTag(member.role)" class="role-tag" :class="getRoleTag(member.role)!.type">
              {{ getRoleTag(member.role)!.label }}
            </span>
            <span v-if="member.isMuted" class="role-tag error">已禁言</span>
          </div>
          <div class="member-status">
            <span
              v-if="getFocusTag(member.focusStatus)"
              class="status-tag"
              :class="getFocusTag(member.focusStatus)!.type"
            >
              {{ getFocusTag(member.focusStatus)!.label }}
            </span>
            <span v-else-if="onlineUserIds.has(member.userId)" class="status-text online">在线</span>
            <span v-else class="status-text offline">离线</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.member-list {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.member-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--sp-2) var(--sp-3);
  border-bottom: 1px solid var(--separator);
}

.header-title {
  font-weight: var(--weight-semibold);
  font-size: var(--text-sm);
  color: var(--text-color-strong);
}

.online-count {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

.member-scroll {
  flex: 1;
  overflow-y: auto;
  padding: var(--sp-1) 0;
}

.member-item {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  padding: var(--sp-2) var(--sp-3);
  transition: background-color var(--transition-fast);
}

.member-item:hover {
  background: var(--state-hover);
}

.member-item.offline {
  opacity: 0.5;
}

.member-avatar {
  position: relative;
  flex-shrink: 0;
}

.online-dot {
  position: absolute;
  bottom: -1px;
  right: -1px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 2px solid var(--bg-card);
  background-color: var(--text-color-muted);
}

.online-dot.online {
  background-color: var(--success);
}

.online-dot.studying {
  background-color: var(--danger);
  animation: pulse 2s infinite;
}

.online-dot.paused {
  background-color: var(--warning);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  flex-wrap: wrap;
}

.member-name {
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-tag {
  font-size: var(--text-xs);
  padding: 0 var(--sp-1);
  border-radius: var(--radius-xs);
  line-height: 1.5;
  flex-shrink: 0;
}

.role-tag.error {
  background: var(--danger-muted);
  color: var(--danger);
}

.role-tag.warning {
  background: var(--warning-muted);
  color: var(--warning);
}

.member-status {
  margin-top: 2px;
}

.status-tag {
  font-size: var(--text-xs);
  padding: 0 var(--sp-1);
  border-radius: var(--radius-xs);
  line-height: 1.5;
}

.status-tag.success {
  background: var(--success-muted);
  color: var(--success);
}

.status-tag.warning {
  background: var(--warning-muted);
  color: var(--warning);
}

.status-text {
  font-size: var(--text-xs);
}

.status-text.online {
  color: var(--success);
}

.status-text.offline {
  color: var(--text-color-muted);
}
</style>
