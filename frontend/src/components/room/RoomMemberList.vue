<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { NPopover } from 'naive-ui'
import type { RoomMemberResponse } from '@/api/room'
import { userApi } from '@/api/user'
import type { PublicUserProfileResponse } from '@/api/user'

const props = defineProps<{
  members: RoomMemberResponse[]
  onlineUserIds: Set<number>
}>()

const _emit = defineEmits<{
  kick: [userId: number]
  mute: [userId: number]
}>()

// ===== Focus time local increment =====
const focusOffsets = ref<Map<number, number>>(new Map())
let tickTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  tickTimer = setInterval(() => {
    for (const member of props.members) {
      if (member.focusStatus === 'STUDYING' && member.focusElapsedSeconds != null && member.focusElapsedSeconds > 0) {
        const current = focusOffsets.value.get(member.userId) ?? 0
        focusOffsets.value.set(member.userId, current + 60)
      }
    }
  }, 60_000)
})

onUnmounted(() => {
  if (tickTimer) clearInterval(tickTimer)
})

function getEffectiveSeconds(member: RoomMemberResponse): number {
  const base = member.focusElapsedSeconds ?? 0
  const offset = focusOffsets.value.get(member.userId) ?? 0
  return base + offset
}

function formatFocusTime(totalSeconds: number): string {
  const h = Math.floor(totalSeconds / 3600)
  const m = Math.floor((totalSeconds % 3600) / 60)
  if (h > 0) return `${h}h${m}m`
  return `${m}m`
}

function truncate(text: string, maxLen: number): string {
  return text.length > maxLen ? text.slice(0, maxLen) + '…' : text
}

// ===== Popover profile =====
const profileCache = ref<Map<number, PublicUserProfileResponse>>(new Map())
const popoverLoading = ref<Set<number>>(new Set())

async function loadProfile(userId: number) {
  if (profileCache.value.has(userId) || popoverLoading.value.has(userId)) return
  popoverLoading.value.add(userId)
  try {
    const { data } = await userApi.getProfile(userId)
    if (data.code === '0' && data.data) {
      profileCache.value.set(userId, data.data)
    }
  } finally {
    popoverLoading.value.delete(userId)
  }
}

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
      <NPopover
        v-for="member in sortedMembers"
        :key="member.userId"
        trigger="hover"
        :delay="300"
        :duration="200"
        placement="left"
        @update:show="(show: boolean) => { if (show) loadProfile(member.userId) }"
      >
        <template #trigger>
          <div
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
                <template v-if="getFocusTag(member.focusStatus)">
                  <span class="status-tag" :class="getFocusTag(member.focusStatus)!.type">
                    {{ getFocusTag(member.focusStatus)!.label }}
                  </span>
                  <template v-if="getEffectiveSeconds(member) > 0">
                    <span class="status-separator">·</span>
                    <span class="status-detail">{{ formatFocusTime(getEffectiveSeconds(member)) }}</span>
                  </template>
                  <template v-if="member.focusTaskTitle">
                    <span class="status-separator">·</span>
                    <span class="status-detail">{{ truncate(member.focusTaskTitle, 15) }}</span>
                  </template>
                </template>
                <span v-else-if="onlineUserIds.has(member.userId)" class="status-text online">在线</span>
                <span v-else class="status-text offline">离线</span>
              </div>
            </div>
          </div>
        </template>

        <div class="profile-card">
          <template v-if="profileCache.has(member.userId)">
            <div class="profile-card-header">
              <n-avatar round :size="40" :src="profileCache.get(member.userId)!.avatarUrl || undefined">
                {{ profileCache.get(member.userId)!.displayName?.charAt(0) }}
              </n-avatar>
              <div class="profile-card-info">
                <div class="profile-card-name">{{ profileCache.get(member.userId)!.displayName }}</div>
              </div>
            </div>
            <div v-if="profileCache.get(member.userId)!.bio" class="profile-card-bio">
              {{ profileCache.get(member.userId)!.bio }}
            </div>
            <div v-else class="profile-card-bio empty">暂无简介</div>
          </template>
          <template v-else>
            <div class="profile-card-loading">加载中...</div>
          </template>
        </div>
      </NPopover>
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

.status-separator {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  margin: 0 2px;
}

.status-detail {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

/* ===== Profile Card (Popover) ===== */
.profile-card {
  min-width: 180px;
  max-width: 240px;
}

.profile-card-header {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.profile-card-info {
  flex: 1;
  min-width: 0;
}

.profile-card-name {
  font-size: var(--text-sm);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-card-bio {
  margin-top: var(--sp-2);
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  line-height: 1.5;
  word-break: break-all;
}

.profile-card-bio.empty {
  font-style: italic;
}

.profile-card-loading {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  padding: var(--sp-1) 0;
}
</style>
