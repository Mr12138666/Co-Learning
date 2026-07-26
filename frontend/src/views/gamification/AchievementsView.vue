<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useGamificationStore } from '@/stores/gamificationStore'
import { NCard, NGrid, NGridItem, NTag, NSpin, NProgress, NSpace, NEmpty } from 'naive-ui'
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

const categoryColor: Record<string, string> = {
  STUDY: 'info',
  STREAK: 'warning',
  SOCIAL: 'success',
  SPECIAL: 'error',
}

// Group achievements by category
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

const iconEmoji: Record<string, string> = {
  baby: '👶',
  clock: '⏰',
  book: '📖',
  graduation: '🎓',
  fire: '🔥',
  flame: '🔥',
  crown: '👑',
  trophy: '🏆',
  calendar: '📅',
  'calendar-check': '✅',
  star: '⭐',
  'star-fill': '🌟',
}
</script>

<template>
  <div class="achievements-view">
    <h2 class="page-title">成就</h2>

    <!-- Progress Overview -->
    <n-card class="progress-card" :bordered="false">
      <n-space align="center" justify="space-between">
        <div>
          <div class="progress-label">已解锁成就</div>
          <div class="progress-numbers">
            <span class="unlocked">{{ store.unlockedCount }}</span>
            <span class="separator">/</span>
            <span class="total">{{ store.totalCount }}</span>
          </div>
        </div>
        <div class="progress-bar-wrapper">
          <n-progress
            type="circle"
            :percentage="progressPercent"
            :stroke-width="8"
            :show-indicator="true"
          >
            {{ progressPercent }}%
          </n-progress>
        </div>
      </n-space>
    </n-card>

    <n-spin :show="store.loading">
      <!-- Achievement Groups -->
      <div v-for="(achList, category) in groupedAchievements" :key="category" class="achievement-group">
        <h3 class="group-title">
          <n-tag :type="(categoryColor[category] as any) || 'default'" size="small" round>
            {{ categoryLabel[category] || category }}
          </n-tag>
        </h3>
        <n-grid :cols="4" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
          <n-grid-item v-for="ach in achList" :key="ach.id" span="4 m:2 l:1">
            <n-card
              hoverable
              size="small"
              class="achievement-card"
              :class="{ unlocked: ach.unlocked, locked: !ach.unlocked }"
            >
              <div class="ach-icon">
                {{ iconEmoji[ach.icon || ''] || '🏅' }}
              </div>
              <h4 class="ach-name">{{ ach.name }}</h4>
              <p class="ach-desc">{{ ach.description }}</p>
              <div class="ach-rewards">
                <n-space size="small">
                  <n-tag v-if="ach.expReward > 0" size="tiny" :bordered="false" type="info">
                    +{{ ach.expReward }} EXP
                  </n-tag>
                  <n-tag v-if="ach.tokenReward > 0" size="tiny" :bordered="false" type="warning">
                    +{{ ach.tokenReward }} 🪙
                  </n-tag>
                </n-space>
              </div>
              <div class="ach-status">
                <n-tag v-if="ach.unlocked" type="success" size="tiny" round>已解锁</n-tag>
                <n-tag v-else type="default" size="tiny" round>未解锁</n-tag>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </div>

      <n-empty v-if="store.achievements.length === 0 && !store.loading" description="暂无成就数据" style="padding: 60px 0" />
    </n-spin>
  </div>
</template>

<style scoped>
.achievements-view {
  padding: 0 4px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 22px;
}

.progress-card {
  margin-bottom: 24px;
  background-color: var(--bg-card);
}

.progress-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.progress-numbers {
  margin-top: 4px;
}

.unlocked {
  font-size: 28px;
  font-weight: bold;
  color: var(--accent-primary);
}

.separator {
  font-size: 20px;
  color: var(--text-secondary);
  margin: 0 4px;
}

.total {
  font-size: 20px;
  color: var(--text-secondary);
}

.achievement-group {
  margin-bottom: 24px;
}

.group-title {
  margin: 0 0 12px 0;
  font-size: 16px;
}

.achievement-card {
  transition: transform 0.2s, opacity 0.2s;
}

.achievement-card.unlocked {
  border-color: var(--primary-color);
}

.achievement-card.locked {
  opacity: 0.6;
}

.achievement-card.locked .ach-icon {
  filter: grayscale(1);
}

.ach-icon {
  font-size: 32px;
  text-align: center;
  margin-bottom: 8px;
}

.ach-name {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
}

.ach-desc {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
  line-height: 1.4;
  min-height: 32px;
}

.ach-rewards {
  text-align: center;
  margin-bottom: 8px;
}

.ach-status {
  text-align: center;
}
</style>
