<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import { useGamificationStore } from '@/stores/gamificationStore'
import {
  NCard, NSpace, NButton, NInput, NModal, NProgress, NTag, NGrid, NGridItem,
  NEmpty, NSpin, NAvatar, NStatistic, NPopconfirm,
} from 'naive-ui'

const message = useMessage()
const store = useGamificationStore()

const showRenameModal = ref(false)
const renameValue = ref('')

// Countdown for pet stats decay
const hungerDecaySeconds = ref(0)
const moodDecaySeconds = ref(0)

let countdownTimer: number | null = null

function formatCountdown(minutes: number): string {
  if (minutes <= 0) return '即将减少'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}小时${mins}分钟后减少`
  }
  return `${mins}分钟后减少`
}

const hungerCountdown = computed(() => formatCountdown(Math.floor(hungerDecaySeconds.value / 60)))
const moodCountdown = computed(() => formatCountdown(Math.floor(moodDecaySeconds.value / 60)))

function startCountdown() {
  if (countdownTimer) clearInterval(countdownTimer)
  // Convert minutes to seconds for countdown
  hungerDecaySeconds.value = (store.pet?.nextHungerDecayInMinutes || 0) * 60
  moodDecaySeconds.value = (store.pet?.nextMoodDecayInMinutes || 0) * 60
  countdownTimer = window.setInterval(async () => {
    if (hungerDecaySeconds.value > 0) hungerDecaySeconds.value--
    if (moodDecaySeconds.value > 0) moodDecaySeconds.value--
    
    // Refresh pet data when either countdown reaches 0
    if (hungerDecaySeconds.value <= 0 || moodDecaySeconds.value <= 0) {
      await store.loadPet()
      // Reset countdowns with new data
      hungerDecaySeconds.value = (store.pet?.nextHungerDecayInMinutes || 0) * 60
      moodDecaySeconds.value = (store.pet?.nextMoodDecayInMinutes || 0) * 60
    }
  }, 1000) // Update every second for smoother countdown
}

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const speciesEmoji: Record<string, string> = {
  CAT: '🐱',
  DOG: '🐶',
  RABBIT: '🐰',
  OWL: '🦉',
}

const itemTypeLabel: Record<string, string> = {
  FOOD: '食物',
  TOY: '玩具',
  ACCESSORY: '饰品',
}

const effectLabel: Record<string, string> = {
  HUNGER_RESTORE: '饱食度',
  MOOD_BOOST: '心情',
  EXP_BOOST: '经验',
}

onMounted(() => {
  store.loadPet().then(() => {
    startCountdown()
  })
  store.loadShop()
  store.loadProfile()
  store.loadInventory()
})

function openRename() {
  renameValue.value = store.pet?.name || ''
  showRenameModal.value = true
}

async function submitRename() {
  if (!renameValue.value.trim()) {
    message.warning('请输入宠物名字')
    return
  }
  try {
    await store.renamePet(renameValue.value.trim())
    message.success('改名成功')
    showRenameModal.value = false
  } catch {
    message.error('改名失败')
  }
}

async function handleBuy(itemId: number) {
  try {
    await store.purchaseItem(itemId)
    message.success('购买成功')
  } catch (e: any) {
    message.error(e?.response?.data?.message || '购买失败，代币不足')
  }
}

async function handleFeed(itemId: number) {
  try {
    await store.feedPet(itemId)
    message.success('喂食成功')
  } catch {
    message.error('使用失败，道具不足')
  }
}

async function handleInteract(itemId: number) {
  try {
    await store.interactPet(itemId)
    message.success('互动成功')
  } catch {
    message.error('使用失败，道具不足')
  }
}

function moodColor(mood: number): string {
  if (mood >= 70) return '#67C23A'
  if (mood >= 30) return '#E6A23C'
  return '#F56C6C'
}

// Pet EXP calculation (same formula as backend)
const petExpToNext = computed(() => {
  const level = store.pet?.level || 1
  return level * 100 // expForLevel(level + 1) - expForLevel(level)
})

const petExpPercent = computed(() => {
  if (!store.pet) return 0
  const current = store.pet.exp
  const next = petExpToNext.value
  if (next === 0) return 0
  return Math.min(100, Math.round((current / next) * 100))
})
</script>

<template>
  <div class="pet-view">
    <h2 class="page-title">我的宠物</h2>

    <n-spin :show="store.loading">
      <!-- Pet Status Card -->
      <n-card v-if="store.pet" class="pet-card" :bordered="false">
        <div class="pet-display">
          <div class="pet-avatar">
            <span class="pet-emoji">{{ speciesEmoji[store.pet.species] || '🐱' }}</span>
          </div>
          <div class="pet-info">
            <n-space align="center" size="small">
              <h3 class="pet-name">{{ store.pet.name }}</h3>
              <n-tag type="info" size="small" round>Lv.{{ store.pet.level }}</n-tag>
              <n-button quaternary size="tiny" @click="openRename">改名</n-button>
            </n-space>
            <div class="pet-stats">
              <!-- Pet EXP bar -->
              <div class="stat-block">
                <div class="stat-block-header">
                  <span class="stat-label">EXP</span>
                  <span class="stat-text">{{ store.pet.exp }}/{{ petExpToNext }}</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="petExpPercent"
                  color="#722ED1"
                  :show-indicator="false"
                  :height="6"
                />
              </div>
              <div class="stat-block">
                <div class="stat-block-header">
                  <span class="stat-label">心情</span>
                  <span class="stat-text">{{ store.pet.mood }}</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="store.pet.mood"
                  :color="moodColor(store.pet.mood)"
                  :show-indicator="false"
                  :height="6"
                />
                <div class="stat-hint">{{ moodCountdown }}</div>
              </div>
              <div class="stat-block">
                <div class="stat-block-header">
                  <span class="stat-label">饱食</span>
                  <span class="stat-text">{{ store.pet.hunger }}</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="store.pet.hunger"
                  :color="moodColor(store.pet.hunger)"
                  :show-indicator="false"
                  :height="6"
                />
                <div class="stat-hint">{{ hungerCountdown }}</div>
              </div>
            </div>
          </div>
        </div>
      </n-card>

      <!-- Token Balance -->
      <n-card v-if="store.profile" class="token-card" :bordered="false">
        <div class="token-display">
          <span class="token-emoji">🪙</span>
          <span class="token-amount">{{ store.profile.tokens }}</span>
          <span class="token-label">代币</span>
        </div>
      </n-card>

      <!-- How to Earn -->
      <n-card class="earn-guide-card" :bordered="false">
        <div class="guide-header">
          <span class="guide-icon">📈</span>
          <h3 class="guide-title">代币与经验获取</h3>
        </div>
        <n-grid :cols="2" :x-gap="12" :y-gap="8">
          <n-grid-item span="2">
            <div class="earn-item">
              <span class="earn-icon">📚</span>
              <div class="earn-info">
                <div class="earn-title">专注学习</div>
                <div class="earn-desc">每专注10分钟获得 1代币 + 宠物经验</div>
              </div>
              <span class="earn-value">+1🪙/10min</span>
            </div>
          </n-grid-item>
          <n-grid-item span="2">
            <div class="earn-item">
              <span class="earn-icon">✅</span>
              <div class="earn-info">
                <div class="earn-title">每日打卡</div>
                <div class="earn-desc">完成每日复盘获得5代币+连续打卡奖励</div>
              </div>
              <span class="earn-value">+5🪙/天</span>
            </div>
          </n-grid-item>
          <n-grid-item span="2">
            <div class="earn-item">
              <span class="earn-icon">🎁</span>
              <div class="earn-info">
                <div class="earn-title">新手礼包</div>
                <div class="earn-desc">首次注册赠送50代币</div>
              </div>
              <span class="earn-value">+50🪙</span>
            </div>
          </n-grid-item>
          <n-grid-item span="2">
            <div class="earn-item">
              <span class="earn-icon">🏆</span>
              <div class="earn-info">
                <div class="earn-title">成就奖励</div>
                <div class="earn-desc">解锁成就获得5-200代币</div>
              </div>
              <span class="earn-value">+5~200🪙</span>
            </div>
          </n-grid-item>
          <n-grid-item span="2">
            <div class="earn-item">
              <span class="earn-icon">🐱</span>
              <div class="earn-info">
                <div class="earn-title">喂食/互动</div>
                <div class="earn-desc">使用经验药水道具提升宠物经验</div>
              </div>
              <span class="earn-value">宠物EXP</span>
            </div>
          </n-grid-item>
        </n-grid>
      </n-card>
    </n-spin>

    <!-- My Inventory -->
    <h3 class="section-title">我的道具</h3>
    <n-grid v-if="store.inventory.length > 0" :cols="4" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
      <n-grid-item v-for="item in store.inventory" :key="item.id" span="4 m:2 l:1">
        <n-card hoverable size="small" class="inventory-item">
          <div class="item-header">
            <span class="item-icon">🎁</span>
            <n-tag size="tiny" round>{{ itemTypeLabel[item.itemType] || item.itemType }}</n-tag>
            <n-tag size="tiny" type="warning" round>x{{ item.quantity }}</n-tag>
          </div>
          <h4 class="item-name">{{ item.name }}</h4>
          <p class="item-desc">{{ item.description }}</p>
          <div class="item-effect">
            <n-tag size="tiny" :bordered="false">
              {{ effectLabel[item.effectType] }} +{{ item.effectValue }}
            </n-tag>
          </div>
          <div class="item-actions">
            <n-button v-if="item.itemType === 'FOOD'" size="small" type="primary" @click="handleFeed(item.itemId)">
              喂食
            </n-button>
            <n-button v-if="item.itemType === 'TOY'" size="small" type="info" @click="handleInteract(item.itemId)">
              互动
            </n-button>
            <n-button v-if="item.itemType === 'ACCESSORY'" size="small" type="warning" @click="handleFeed(item.itemId)">
              使用
            </n-button>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>
    <n-empty v-else description="还没有道具，去商店购买吧" style="padding: 40px 0" />

    <!-- Shop Section -->
    <h3 class="section-title">道具商店</h3>
    <n-grid v-if="store.shopItems.length > 0" :cols="4" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
      <n-grid-item v-for="item in store.shopItems" :key="item.id" span="4 m:2 l:1">
        <n-card hoverable size="small" class="shop-item">
          <div class="shop-item-header">
            <span class="shop-item-icon">🎁</span>
            <n-tag size="tiny" round>{{ itemTypeLabel[item.itemType] || item.itemType }}</n-tag>
          </div>
          <h4 class="shop-item-name">{{ item.name }}</h4>
          <p class="shop-item-desc">{{ item.description }}</p>
          <div class="shop-item-effect">
            <n-tag size="tiny" :bordered="false">
              {{ effectLabel[item.effectType] }} +{{ item.effectValue }}
            </n-tag>
          </div>
          <div class="shop-item-footer">
            <span class="shop-item-price">{{ item.price }} 🪙</span>
            <n-popconfirm @positive-click="handleBuy(item.id)">
              <template #trigger>
                <n-button size="small" type="primary" :disabled="(store.profile?.tokens || 0) < item.price">
                  购买
                </n-button>
              </template>
              确认花费 {{ item.price }} 代币购买？
            </n-popconfirm>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>
    <n-empty v-else description="商店加载中..." style="padding: 40px 0" />

    <!-- Rename Modal -->
    <n-modal v-model:show="showRenameModal" preset="card" title="给宠物改名" style="width: 400px">
      <n-input v-model:value="renameValue" placeholder="输入新名字" maxlength="50" />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showRenameModal = false">取消</n-button>
          <n-button type="primary" @click="submitRename">确认</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.pet-view {
  padding: 0 4px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 22px;
}

.pet-card {
  margin-bottom: 16px;
}

.pet-display {
  display: flex;
  gap: 20px;
  align-items: center;
}

.pet-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8f4f8 0%, #f0e8f8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.pet-emoji {
  font-size: 40px;
}

.pet-info {
  flex: 1;
}

.pet-name {
  margin: 0;
  font-size: 18px;
}

.pet-stats {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 400px;
}

.stat-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: var(--text-color-3);
  font-weight: 500;
}

.stat-text {
  font-size: 12px;
  font-weight: 600;
}

.stat-hint {
  font-size: 11px;
  color: var(--text-color-4);
  margin-top: 2px;
}

.token-card {
  margin-bottom: 16px;
}

.token-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.token-emoji {
  font-size: 28px;
}

.token-amount {
  font-size: 32px;
  font-weight: 700;
  color: var(--accent-primary);
}

.token-label {
  font-size: 14px;
  color: var(--text-color-3);
}

.earn-guide-card {
  margin-bottom: 24px;
  background-color: var(--bg-card);
}

.guide-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.guide-icon {
  font-size: 20px;
}

.guide-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.earn-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background-color: var(--bg-primary);
  border-radius: 8px;
}

.earn-icon {
  font-size: 18px;
}

.earn-info {
  flex: 1;
}

.earn-title {
  font-size: 14px;
  font-weight: 500;
}

.earn-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.earn-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--accent-primary);
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 18px;
}

.inventory-item, .shop-item {
  display: flex;
  flex-direction: column;
}

.item-header, .shop-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.item-icon, .shop-item-icon {
  font-size: 24px;
}

.item-name, .shop-item-name {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 600;
}

.item-desc, .shop-item-desc {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: var(--text-color-3);
  line-height: 1.4;
}

.item-effect, .shop-item-effect {
  margin-bottom: 8px;
}

.item-actions {
  display: flex;
  gap: 4px;
}

.shop-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shop-item-price {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
}
</style>
