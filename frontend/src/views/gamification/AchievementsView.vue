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
  padding: 0 var(--sp-1);
}

.page-title {
  margin: 0 0 var(--sp-5) 0;
  font-size: var(--text-2xl);
}

.progress-card {
  margin-bottom: 24px;
  background-color: var(--bg-card);
}

.progress-label {
  font-size: var(--text-base);
  color: var(--text-secondary);
}

.progress-numbers {
  margin-top: var(--sp-1);
}

.unlocked {
  font-size: 28px;
  font-weight: var(--weight-bold);
  color: var(--accent);
}

.separator {
  font-size: var(--text-2xl);
  color: var(--text-secondary);
  margin: 0 var(--sp-1);
}

.total {
  font-size: var(--text-2xl);
  color: var(--text-secondary);
}

.achievement-group {
  margin-bottom: var(--sp-6);
}

.group-title {
  margin: 0 0 var(--sp-3) 0;
  font-size: var(--text-lg);
}

.achievement-card {
  transition: transform var(--duration-fast) var(--ease-default), opacity var(--duration-fast) var(--ease-default);
}

.achievement-card.unlocked {
  border-color: var(--accent);
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
  margin: 0 0 var(--sp-1) 0;
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  text-align: center;
}

.ach-desc {
  margin: 0 0 var(--sp-2) 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  text-align: center;
  line-height: var(--leading-snug);
  min-height: 32px;
}

.ach-rewards {
  text-align: center;
  margin-bottom: var(--sp-2);
}

.ach-status {
  text-align: center;
}
</style>
