<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
  store.loadPet()
  store.loadShop()
  store.loadProfile()
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
              <div class="stat-row">
                <span class="stat-label">心情</span>
                <n-progress
                  type="line"
                  :percentage="store.pet.mood"
                  :color="moodColor(store.pet.mood)"
                  :show-indicator="false"
                  :height="8"
                />
                <span class="stat-value">{{ store.pet.mood }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">饱食</span>
                <n-progress
                  type="line"
                  :percentage="store.pet.hunger"
                  :color="moodColor(store.pet.hunger)"
                  :show-indicator="false"
                  :height="8"
                />
                <span class="stat-value">{{ store.pet.hunger }}</span>
              </div>
            </div>
          </div>
        </div>
      </n-card>

      <!-- Token Balance -->
      <n-card v-if="store.profile" class="token-card" :bordered="false">
        <n-space justify="space-around">
          <n-statistic label="等级" :value="store.profile.level" />
          <n-statistic label="总经验" :value="store.profile.totalExp" />
          <n-statistic label="代币" :value="store.profile.tokens">
            <template #suffix><span class="token-icon">🪙</span></template>
          </n-statistic>
        </n-space>
      </n-card>
    </n-spin>

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
          <div class="shop-item-actions">
            <n-button v-if="item.itemType === 'FOOD'" size="tiny" quaternary @click="handleFeed(item.id)">
              喂食
            </n-button>
            <n-button v-if="item.itemType === 'TOY'" size="tiny" quaternary @click="handleInteract(item.id)">
              互动
            </n-button>
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
  gap: 8px;
  max-width: 400px;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-label {
  width: 30px;
  font-size: 13px;
  color: var(--text-color-3);
  flex-shrink: 0;
}

.stat-value {
  width: 30px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.token-card {
  margin-bottom: 24px;
}

.token-icon {
  font-size: 16px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 18px;
}

.shop-item {
  display: flex;
  flex-direction: column;
}

.shop-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.shop-item-icon {
  font-size: 24px;
}

.shop-item-name {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 600;
}

.shop-item-desc {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: var(--text-color-3);
  line-height: 1.4;
}

.shop-item-effect {
  margin-bottom: 8px;
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

.shop-item-actions {
  margin-top: 8px;
  display: flex;
  gap: 4px;
}
</style>
