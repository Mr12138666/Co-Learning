<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useLeaderboardStore, type LeaderboardType } from '@/stores/leaderboardStore'
import { NTabs, NTabPane, NCard, NAvatar, NSpin, NEmpty, NTag, NSpace, NStatistic } from 'naive-ui'
import { usePageLoad } from '@/composables/usePageLoad'
import StateError from '@/components/common/StateError.vue'

const store = useLeaderboardStore()
const { loading, error, load, retry } = usePageLoad()

onMounted(() => load(async () => {
  await store.loadLeaderboard('daily')
}))

watch(() => store.currentType, (type) => {
  store.loadLeaderboard(type)
})

function switchTab(name: string) {
  store.loadLeaderboard(name as LeaderboardType)
}

function formatScore(score: number): string {
  const hours = Math.floor(score / 3600)
  const minutes = Math.floor((score % 3600) / 60)
  if (hours > 0) {
    return `${hours}h ${minutes}m`
  }
  return `${minutes}m`
}

function rankColor(rank: number): string {
  if (rank === 1) return '#FFD700'
  if (rank === 2) return '#C0C0C0'
  if (rank === 3) return '#CD7F32'
  return '#909399'
}

const tabLabels: Record<LeaderboardType, string> = {
  daily: '日榜',
  weekly: '周榜',
  alltime: '总榜',
}
</script>

<template>
  <div class="leaderboard-view">
    <!-- Loading State -->
    <div v-if="loading" style="display: flex; justify-content: center; padding: 80px 0;">
      <NSpin size="large" />
    </div>

    <!-- Error State -->
    <StateError
      v-else-if="error"
      :title="error"
      @retry="retry(async () => {
        await store.loadLeaderboard('daily')
      })"
    />

    <template v-else>
    <h2 class="page-title">排行榜</h2>

    <!-- My Rank Card -->
    <n-card v-if="store.myRank" class="my-rank-card" :bordered="false">
      <n-space align="center" justify="space-between">
        <n-space align="center" size="large">
          <n-avatar round size="medium" :src="store.myRank.avatarUrl || undefined">
            {{ store.myRank.displayName?.charAt(0) }}
          </n-avatar>
          <div>
            <div class="my-name">{{ store.myRank.displayName }}</div>
            <n-tag :type="store.myRank.rank > 0 ? 'info' : 'default'" size="small" round>
              {{ store.myRank.rank > 0 ? `第 ${store.myRank.rank} 名` : '未上榜' }}
            </n-tag>
          </div>
        </n-space>
        <n-space size="large">
          <n-statistic label="专注时长" :value="formatScore(store.myRank.score)" />
        </n-space>
      </n-space>
    </n-card>

    <!-- Leaderboard Tabs -->
    <n-tabs type="line" animated :value="store.currentType" @update:value="switchTab">
      <n-tab-pane v-for="(label, key) in tabLabels" :key="key" :name="key" :tab="label">
        <n-spin :show="store.loading">
          <div v-if="store.entries.length > 0" class="rank-list">
            <div
              v-for="entry in store.entries"
              :key="entry.userId"
              class="rank-item"
              :class="{ 'rank-top': entry.rank <= 3 }"
            >
              <div class="rank-number" :style="{ color: rankColor(entry.rank) }">
                {{ entry.rank <= 3 ? ['🥇', '🥈', '🥉'][entry.rank - 1] : entry.rank }}
              </div>
              <n-avatar round size="small" :src="entry.avatarUrl || undefined">
                {{ entry.displayName?.charAt(0) }}
              </n-avatar>
              <span class="rank-name">{{ entry.displayName }}</span>
              <span class="rank-score">{{ formatScore(entry.score) }}</span>
            </div>
          </div>
          <n-empty v-else description="暂无排行数据，开始学习冲刺榜首吧！" style="padding: 60px 0" />
        </n-spin>
      </n-tab-pane>
    </n-tabs>
    </template>
  </div>
</template>

<style scoped>
.leaderboard-view {
  padding: 0 var(--sp-1);
}

.page-title {
  margin: 0 0 var(--sp-5) 0;
  font-size: var(--text-2xl);
}

.my-rank-card {
  margin-bottom: var(--sp-5);
  background-color: var(--bg-card);
}

.my-name {
  font-weight: var(--weight-semibold);
  font-size: var(--text-lg);
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-md);
  transition: background-color var(--duration-fast) var(--ease-default);
}

.rank-item:hover {
  background-color: var(--bg-hover);
}

.rank-item.rank-top {
  background-color: var(--bg-sunken);
}

.rank-number {
  width: 32px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
}

.rank-name {
  flex: 1;
  font-size: var(--text-base);
  font-weight: var(--weight-medium);
}

.rank-score {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  color: var(--accent);
}
</style>
