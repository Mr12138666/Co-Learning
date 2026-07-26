<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NList,
  NListItem,
  NThing,
  NTag,
  NEmpty,
  NText,
  NButton,
  NSpace,
  NImage,
} from 'naive-ui'
import { journalApi, type Journal } from '@/api/journal'
import { userApi } from '@/api/user'
import dayjs from 'dayjs'

const router = useRouter()
const journals = ref<Journal[]>([])
const loading = ref(false)
const userCache = ref<Map<number, { nickname: string; avatarUrl: string }>>(new Map())

function formatDate(date: string): string {
  return dayjs(date).format('MM-DD HH:mm')
}

async function fetchUserInfo(userId: number) {
  if (userCache.value.has(userId)) {
    return userCache.value.get(userId)!
  }
  try {
    const res = await userApi.getProfile(userId)
    const info = {
      nickname: res.data.data.displayName || res.data.data.email,
      avatarUrl: res.data.data.avatarUrl || '',
    }
    userCache.value.set(userId, info)
    return info
  } catch {
    return { nickname: '未知用户', avatarUrl: '' }
  }
}

async function loadPublicJournals() {
  loading.value = true
  try {
    const res = await journalApi.listPublic({ page: 0, size: 20 })
    journals.value = res.data.data.items
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPublicJournals()
})
</script>

<template>
  <div>
    <NCard :bordered="false">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <span style="font-size: 20px;">📝</span>
            <span style="font-size: 18px; font-weight: 600; margin-left: 8px;">日志广场</span>
          </div>
          <NButton type="primary" @click="router.push('/journals/new')">
            + 写日志
          </NButton>
        </div>
      </template>

      <NEmpty v-if="!loading && journals.length === 0" description="还没有公开的日志">
        <template #extra>
          <NButton type="primary" @click="router.push('/journals/new')">发布第一篇日志</NButton>
        </template>
      </NEmpty>

      <NList v-else hoverable>
        <NListItem
          v-for="journal in journals"
          :key="journal.id"
          @click="router.push(`/journals/${journal.id}`)"
        >
          <NThing>
            <template #header>
              <NSpace align="center" :size="8">
                <NText strong>{{ journal.title }}</NText>
                <NTag size="small" round type="success">
                  公开
                </NTag>
              </NSpace>
            </template>
            <template #description>
              <NText depth="3" style="font-size: 13px;">
                {{ formatDate(journal.publishedAt || journal.createdAt) }}
              </NText>
            </template>
            <template #action>
              <NButton size="tiny" quaternary>阅读</NButton>
            </template>
          </NThing>
        </NListItem>
      </NList>
    </NCard>
  </div>
</template>
