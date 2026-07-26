<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useGamificationStore } from '@/stores/gamificationStore'
import { NCard, NTag, NSpin, NProgress, NEmpty } from 'naive-ui'
import type { AchievementResponse } from '@/api/gamification'

const store = useGamificationStore()

onMounted(() => {
  store.loadAchievements()
  store.loadProfile()
})

const categoryLabel: Record<string, string> = {
  STUDY: '学习',
  STREAK: '连续打卡',
  SOCIAL: '社交',
  SPECIAL: '特殊',
}

const categoryColor: Record<string, 'info' | 'warning' | 'success' | 'error'> = {
  STUDY: 'info',
  STREAK: 'warning',
  SOCIAL: 'success',
  SPECIAL: 'error',
}

const groupedAchievements = computed(() => {
  const groups: Record<string, AchievementResponse[]> = {}
  for (const ach of store.achievements) {
    if (!groups[ach.category]) groups[ach.category] = []
    groups[ach.category].push(ach)
  }
  return groups
})

const progressPercent = computed(() => {
  if (store.totalCount === 0) return 0
  return Math.round((store.unlockedCount / store.totalCount) * 100)
})
</script>

<template>
  <div class="achievements-view">
    <h2 class="page-title">成就</h2>

    <!-- Progress Overview -->
    <n-card class="progress-card" :bordered="false">
      <div class="progress-content">
        <div class="progress-info">
          <span class="progress-label">已解锁成就</span>
          <div class="progress-numbers">
            <span class="num-unlocked">{{ store.unlockedCount }}</span>
            <span class="num-sep">/</span>
            <span class="num-total">{{ store.totalCount }}</span>
          </div>
        </div>
        <n-progress
          type="circle"
          :percentage="progressPercent"
          :stroke-width="8"
          :show-indicator="true"
          class="progress-circle"
        >
          {{ progressPercent }}%
        </n-progress>
      </div>
    </n-card>

    <n-spin :show="store.loading">
      <div v-for="(achList, category) in groupedAchievements" :key="category" class="ach-group">
        <h3 class="group-title">
          <n-tag :type="categoryColor[category] || 'default'" size="small" round>
            {{ categoryLabel[category] || category }}
          </n-tag>
        </h3>
        <div class="ach-grid">
          <div
            v-for="ach in achList"
            :key="ach.id"
            class="ach-card"
            :class="{ 'ach-card--unlocked': ach.unlocked, 'ach-card--locked': !ach.unlocked }"
          >
            <div class="ach-icon-wrapper">
              <span v-if="ach.unlocked" class="ach-icon ach-icon--unlocked">*</span>
              <span v-else class="ach-icon ach-icon--locked">-</span>
            </div>
            <div class="ach-body">
              <h4 class="ach-name">{{ ach.name }}</h4>
              <p class="ach-desc">{{ ach.description }}</p>
              <div class="ach-rewards">
                <n-tag v-if="ach.expReward > 0" size="tiny" :bordered="false" type="info">
                  +{{ ach.expReward }} EXP
                </n-tag>
                <n-tag v-if="ach.tokenReward > 0" size="tiny" :bordered="false" type="warning">
                  +{{ ach.tokenReward }} T
                </n-tag>
              </div>
            </div>
            <div class="ach-status">
              <n-tag v-if="ach.unlocked" type="success" size="tiny" round>已解锁</n-tag>
              <n-tag v-else type="default" size="tiny" round>未解锁</n-tag>
            </div>
          </div>
        </div>
      </div>

      <n-empty v-if="store.achievements.length === 0 && !store.loading" description="暂无成就数据" class="section-empty" />
    </n-spin>
  </div>
</template>

<style scoped>
.achievements-view {
  padding: var(--sp-4);
}

.page-title {
  margin: 0 0 var(--sp-4) 0;
  font-size: var(--text-xl);
}

/* --- Progress Card --- */
.progress-card {
  margin-bottom: var(--sp-4);
  background: var(--surface-2);
  border: 1px solid var(--divider);
  border-radius: var(--radius-md);
}

.progress-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.progress-label {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.progress-numbers {
  margin-top: var(--sp-1);
  display: flex;
  align-items: baseline;
  gap: var(--sp-1);
}

.num-unlocked {
  font-size: 24px;
  font-weight: var(--weight-bold);
  color: var(--brand);
}

.num-sep {
  font-size: var(--text-lg);
  color: var(--text-color-muted);
}

.num-total {
  font-size: var(--text-lg);
  color: var(--text-color-muted);
}

.progress-circle {
  flex-shrink: 0;
}

/* --- Achievement Groups --- */
.ach-group {
  margin-bottom: var(--sp-4);
}

.group-title {
  margin: 0 0 var(--sp-3) 0;
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
}

.ach-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--sp-3);
}

/* --- Achievement Cards --- */
.ach-card {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-3);
  padding: var(--sp-3);
  background: var(--surface-2);
  border: 1px solid var(--divider);
  border-radius: var(--radius-md);
  transition: opacity var(--transition-fast), border-color var(--transition-fast);
}

.ach-card--unlocked {
  border-color: var(--brand);
}

.ach-card--locked {
  opacity: 0.55;
}

.ach-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: var(--text-lg);
  font-weight: var(--weight-bold);
}

.ach-icon--unlocked {
  color: var(--brand);
  background: var(--bg-page);
  border: 1px solid var(--brand);
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ach-icon--locked {
  color: var(--text-color-muted);
  background: var(--bg-page);
  border: 1px solid var(--separator);
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ach-body {
  flex: 1;
  min-width: 0;
}

.ach-name {
  margin: 0 0 2px 0;
  font-size: var(--text-sm);
  font-weight: var(--weight-semibold);
  color: var(--text-color);
}

.ach-desc {
  margin: 0 0 var(--sp-2) 0;
  font-size: 12px;
  color: var(--text-color-muted);
  line-height: 1.4;
}

.ach-rewards {
  display: flex;
  gap: var(--sp-1);
  flex-wrap: wrap;
}

.ach-status {
  flex-shrink: 0;
  padding-top: 2px;
}

.section-empty {
  padding: var(--sp-10) 0;
}

@media (max-width: 768px) {
  .ach-grid {
    grid-template-columns: 1fr;
  }
}
</style>
