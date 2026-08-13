<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useLeaderboardStore, type LeaderboardType } from '@/stores/leaderboardStore'
import { NTabs, NTabPane, NAvatar, NSpin, NEmpty, NTag } from 'naive-ui'
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
  if (rank === 1) return 'var(--warning)'
  if (rank === 2) return 'var(--text-color-muted)'
  if (rank === 3) return '#CD7F32'
  return 'var(--text-color-muted)'
}

const tabLabels: Record<LeaderboardType, string> = {
  daily: '日榜',
  weekly: '周榜',
  alltime: '总榜',
}
</script>

<template>
  <div class="leaderboard-view gradient-mesh">
    <!-- Loading -->
    <div v-if="loading" class="loading-center">
      <NSpin size="large" />
    </div>

    <!-- Error -->
    <StateError
      v-else-if="error"
      :title="error"
      @retry="retry(async () => {
        await store.loadLeaderboard('daily')
      })"
    />

    <template v-else>
      <!-- Page Header -->
      <div class="page-header glass page-hero">
        <h3 class="page-title">排行榜</h3>
      </div>

      <!-- My Rank -->
      <div v-if="store.myRank" class="my-rank-bar glass glow-warm">
        <div class="my-rank-left">
          <NAvatar round size="small" :src="store.myRank.avatarUrl || undefined">
            {{ store.myRank.displayName?.charAt(0) }}
          </NAvatar>
          <span class="my-name">{{ store.myRank.displayName }}</span>
          <NTag :type="store.myRank.rank > 0 ? 'info' : 'default'" size="tiny" round :bordered="false">
            {{ store.myRank.rank > 0 ? `第 ${store.myRank.rank} 名` : '未上榜' }}
          </NTag>
        </div>
        <div class="my-rank-right">
          <span class="my-score-label">专注时长</span>
          <span class="my-score-value">{{ formatScore(store.myRank.score) }}</span>
        </div>
      </div>

      <!-- Leaderboard Tabs -->
      <n-tabs type="line" animated :value="store.currentType" @update:value="switchTab">
        <n-tab-pane v-for="(label, key) in tabLabels" :key="key" :name="key" :tab="label">
          <n-spin :show="store.loading">
            <div v-if="store.entries.length > 0" class="rank-table glass">
              <div class="rank-table-header glass--subtle">
                <span class="col-rank">排名</span>
                <span class="col-user">用户</span>
                <span class="col-score">专注时长</span>
              </div>
              <div
                v-for="entry in store.entries"
                :key="entry.userId"
                class="rank-row glass-list-item stagger-in"
                :class="{
                  'rank-top': entry.rank <= 3,
                  'rank-1': entry.rank === 1,
                  'rank-2': entry.rank === 2,
                  'rank-3': entry.rank === 3,
                  'rank-current': store.myRank?.userId === entry.userId,
                  'glow-brand': store.myRank?.userId === entry.userId,
                }"
              >
                <div class="col-rank">
                  <span class="rank-num badge glass--subtle" :style="{ color: rankColor(entry.rank) }">
                    {{ entry.rank <= 3 ? ['🥇', '🥈', '🥉'][entry.rank - 1] : entry.rank }}
                  </span>
                </div>
                <div class="col-user">
                  <NAvatar round size="small" :src="entry.avatarUrl || undefined">
                    {{ entry.displayName?.charAt(0) }}
                  </NAvatar>
                  <span class="rank-name">{{ entry.displayName }}</span>
                </div>
                <div class="col-score">
                  <span class="rank-score">{{ formatScore(entry.score) }}</span>
                </div>
              </div>
            </div>
            <div v-else class="empty-container">
              <n-empty description="暂无排行数据，开始学习冲刺榜首吧！" />
            </div>
          </n-spin>
        </n-tab-pane>
      </n-tabs>
    </template>
  </div>
</template>

<style scoped>
.leaderboard-view {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: var(--sp-4);
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: var(--sp-12) 0;
}

.page-header {
  margin-bottom: var(--sp-4);
}

.page-title {
  position: relative;
  z-index: 1;
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.empty-container {
  padding: var(--sp-12) 0;
}

/* My Rank Bar */
.my-rank-bar {
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-lg);
  margin-bottom: var(--sp-3);
}

.my-rank-bar::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, var(--brand), var(--urgent));
}

.my-rank-left {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.my-name {
  font-weight: var(--weight-semibold);
  font-size: var(--text-base);
  color: var(--text-color-strong);
}

.my-rank-right {
  display: flex;
  align-items: baseline;
  gap: var(--sp-2);
}

.my-score-label {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.my-score-value {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--brand);
  font-variant-numeric: tabular-nums;
}

/* Rank Table */
.rank-table {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  padding: var(--sp-2);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-top: var(--sp-3);
}

.rank-table-header {
  display: grid;
  grid-template-columns: 60px 1fr 100px;
  padding: var(--sp-2) var(--sp-4);
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  color: var(--text-color-muted);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
  border-radius: var(--radius-md);
}

.rank-row {
  display: grid;
  grid-template-columns: 60px 1fr 100px;
  align-items: center;
  gap: 0;
  padding: var(--sp-2) var(--sp-4);
  min-height: 44px;
}

/* Podium glow + gold/silver/bronze accents for top 3 */
.rank-row.rank-top {
  box-shadow:
    0 0 20px rgba(245, 158, 11, 0.12),
    0 0 40px rgba(245, 158, 11, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.rank-row.rank-1 {
  border-left: 3px solid var(--warning);
}

.rank-row.rank-2 {
  border-left: 3px solid #c0c4c8;
}

.rank-row.rank-3 {
  border-left: 3px solid #cd7f32;
}

.rank-row.rank-current {
  border-left: 3px solid var(--brand);
  padding-left: calc(var(--sp-4) - 2px);
  box-shadow:
    0 0 20px rgba(59, 130, 246, 0.12),
    0 0 40px rgba(59, 130, 246, 0.06),
    0 0 0 1px rgba(59, 130, 246, 0.15);
}

.rank-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 26px;
  height: 24px;
  font-size: var(--text-base);
  font-weight: var(--weight-bold);
  text-align: center;
}

.col-user {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.rank-name {
  font-size: var(--text-base);
  font-weight: var(--weight-medium);
  color: var(--text-color);
}

.col-score {
  text-align: right;
}

.rank-score {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  color: var(--brand);
  font-variant-numeric: tabular-nums;
}
</style>
