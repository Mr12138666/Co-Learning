<script setup lang="ts">
import { computed } from 'vue'
import { NCard, NGrid, NGridItem, NStatistic, NSpace, NTag, NEmpty } from 'naive-ui'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const displayName = computed(() => authStore.user?.email?.split('@')[0] || '同学')
</script>

<template>
  <div>
    <NCard :bordered="false" size="small" style="margin-bottom: 16px;">
      <h2 style="margin: 0; font-size: 24px;">
        {{ greeting }}，{{ displayName }} 👋
      </h2>
      <p style="margin: 8px 0 0; color: #666;">
        欢迎来到伴学平台，和更多备考伙伴一起高效学习。
      </p>
    </NCard>

    <NGrid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <NGridItem span="3 m:1">
        <NCard title="今日学习" :bordered="false">
          <NStatistic label="专注时长" value="0" suffix="分钟" />
        </NCard>
      </NGridItem>
      <NGridItem span="3 m:1">
        <NCard title="连续打卡" :bordered="false">
          <NStatistic label="连续天数" value="0" suffix="天" />
        </NCard>
      </NGridItem>
      <NGridItem span="3 m:1">
        <NCard title="学习室" :bordered="false">
          <NStatistic label="在房间" value="0" suffix="间" />
        </NCard>
      </NGridItem>
    </NGrid>

    <NCard title="最近动态" :bordered="false" style="margin-top: 16px;">
      <NEmpty description="暂无动态，开始你的第一次专注学习吧！" />
    </NCard>

    <NCard title="快速开始" :bordered="false" style="margin-top: 16px;">
      <NSpace>
        <NTag type="info" size="large" round>📝 学习日志</NTag>
        <NTag type="success" size="large" round>🏠 加入学习室</NTag>
        <NTag type="warning" size="large" round>⏱️ 开始专注</NTag>
        <NTag type="error" size="large" round>🏆 查看排行榜</NTag>
      </NSpace>
    </NCard>
  </div>
</template>
