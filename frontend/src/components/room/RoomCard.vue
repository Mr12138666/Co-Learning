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
  return { type: 'info' as const, label: '公开' }
})

const memberColor = computed(() => {
  const ratio = props.room.memberCount / props.room.maxMembers
  if (ratio >= 0.9) return '#F56C6C'
  if (ratio >= 0.7) return '#E6A23C'
  return '#67C23A'
})

function enterRoom() {
  router.push(`/rooms/${props.room.id}`)
}
</script>

<template>
  <n-card hoverable class="room-card" @click="enterRoom">
    <template #header>
      <div class="room-header">
        <span class="room-name">{{ room.name }}</span>
        <n-space size="small">
          <n-tag :type="statusTag.type" size="small" round>{{ statusTag.label }}</n-tag>
          <n-tag :type="visibilityTag.type" size="small" round>{{ visibilityTag.label }}</n-tag>
        </n-space>
      </div>
    </template>

    <p class="room-description" v-if="room.description">{{ room.description }}</p>
    <p class="room-description room-no-desc" v-else>暂无描述</p>

    <div class="room-meta">
      <n-space align="center" size="small">
        <n-avatar round size="small" :src="room.ownerAvatar || undefined">
          {{ room.ownerName?.charAt(0) }}
        </n-avatar>
        <span class="owner-name">{{ room.ownerName }}</span>
      </n-space>
      <n-space size="small">
        <n-tag v-if="room.topic" size="small">{{ room.topic }}</n-tag>
        <span class="member-count" :style="{ color: memberColor }">
          {{ room.memberCount }}/{{ room.maxMembers }} 人
        </span>
      </n-space>
    </div>

    <template #action>
      <n-space justify="space-between" align="center">
        <span class="joined-badge" v-if="room.isMember">
          <n-tag type="success" size="small">已加入</n-tag>
        </span>
        <span v-else></span>
        <n-button type="primary" size="small" @click.stop="enterRoom">
          {{ room.isMember ? '进入房间' : '查看' }}
        </n-button>
      </n-space>
    </template>
  </n-card>
</template>

<style scoped>
.room-card {
  cursor: pointer;
  transition: transform var(--duration-fast) var(--ease-default);
}

.room-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-name {
  font-weight: var(--weight-semibold);
  font-size: var(--text-lg);
}

.room-description {
  color: var(--text-secondary);
  margin: 0 0 var(--sp-3) 0;
  font-size: var(--text-base);
  line-height: var(--leading-normal);
}

.room-no-desc {
  color: var(--text-tertiary);
  font-style: italic;
}

.room-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.owner-name {
  font-size: var(--text-md);
  color: var(--text-secondary);
}

.member-count {
  font-size: var(--text-md);
  font-weight: var(--weight-medium);
}
</style>
