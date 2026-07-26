<script setup lang="ts">
import { computed } from 'vue'
import type { RoomMemberResponse } from '@/api/room'

const props = defineProps<{
  members: RoomMemberResponse[]
  onlineUserIds: Set<number>
}>()

const emit = defineEmits<{
  kick: [userId: number]
  mute: [userId: number]
}>()

const sortedMembers = computed(() => {
  return [...props.members].sort((a, b) => {
    // Online first, then by role, then by name
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
      <n-space align="center">
        <span class="header-title">成员</span>
        <n-badge :value="onlineUserIds.size" :max="999" type="success" show-zero />
      </n-space>
    </div>

    <n-scrollbar style="max-height: 500px">
      <div class="member-items">
        <div
          v-for="member in sortedMembers"
          :key="member.userId"
          class="member-item"
          :class="{ offline: !onlineUserIds.has(member.userId) }"
        >
          <div class="member-avatar">
            <n-avatar round size="medium" :src="member.avatarUrl || undefined">
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
            <n-space align="center" size="small">
              <span class="member-name">{{ member.displayName }}</span>
              <n-tag v-if="getRoleTag(member.role)" :type="getRoleTag(member.role)!.type" size="tiny" round>
                {{ getRoleTag(member.role)!.label }}
              </n-tag>
              <n-tag v-if="member.isMuted" type="error" size="tiny" round>已禁言</n-tag>
            </n-space>
            <div class="member-status">
              <n-tag
                v-if="getFocusTag(member.focusStatus)"
                :type="getFocusTag(member.focusStatus)!.type"
                size="tiny"
                round
              >
                {{ getFocusTag(member.focusStatus)!.label }}
              </n-tag>
              <span v-else-if="onlineUserIds.has(member.userId)" class="status-text online">在线</span>
              <span v-else class="status-text offline">离线</span>
            </div>
          </div>
        </div>
      </div>
    </n-scrollbar>
  </div>
</template>

<style scoped>
.member-list {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.member-list-header {
  padding: 8px 12px;
  border-bottom: 1px solid var(--separator);
}

.header-title {
  font-weight: 600;
  font-size: 14px;
}

.member-items {
  padding: 4px 0;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  transition: background-color 0.2s;
}

.member-item:hover {
  background-color: var(--bg-hover);
}

.member-item.offline {
  opacity: 0.6;
}

.member-avatar {
  position: relative;
  flex-shrink: 0;
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid var(--card-color);
  background-color: #909399;
}

.online-dot.online {
  background-color: #67C23A;
}

.online-dot.studying {
  background-color: #F56C6C;
  animation: pulse 2s infinite;
}

.online-dot.paused {
  background-color: #E6A23C;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 13px;
  font-weight: 500;
}

.member-status {
  margin-top: 2px;
}

.status-text {
  font-size: 12px;
}

.status-text.online {
  color: #67C23A;
}

.status-text.offline {
  color: #909399;
}
</style>
