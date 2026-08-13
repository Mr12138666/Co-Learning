<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import {
  NCard,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NButton,
  NSpace,
  NAvatar,
  NTag,
  NSwitch,
  NSelect,
} from 'naive-ui'
import { userApi, type UserProfileResponse, type UpdateProfileRequest, type UpdateSettingsRequest } from '@/api/user'
import { useAuthStore } from '@/stores/authStore'
import dayjs from 'dayjs'

const message = useMessage()
const authStore = useAuthStore()
const loading = ref(false)
const profile = ref<UserProfileResponse | null>(null)
const editingProfile = ref(false)
const editingSettings = ref(false)
const avatarInput = ref<HTMLInputElement | null>(null)

const profileForm = reactive<UpdateProfileRequest>({
  displayName: '',
  bio: '',
})

const settingsForm = reactive<UpdateSettingsRequest>({
  privacyLevel: 'PUBLIC',
  notifEmailEnabled: true,
  notifPushEnabled: true,
  timezone: 'Asia/Shanghai',
  dailyFocusGoalMinutes: 120,
})

const privacyOptions = [
  { label: '公开', value: 'PUBLIC' },
  { label: '好友可见', value: 'FRIENDS' },
  { label: '私密', value: 'PRIVATE' },
]

const timezoneOptions = [
  { label: '亚洲/上海 (UTC+8)', value: 'Asia/Shanghai' },
  { label: '亚洲/东京 (UTC+9)', value: 'Asia/Tokyo' },
  { label: '欧洲/伦敦 (UTC+0)', value: 'Europe/London' },
  { label: '美国/纽约 (UTC-5)', value: 'America/New_York' },
]

const formattedCreatedAt = computed(() => {
  if (!profile.value?.createdAt) return ''
  return dayjs(profile.value.createdAt).format('YYYY-MM-DD HH:mm')
})

async function loadProfile() {
  loading.value = true
  try {
    const res = await userApi.getMyProfile()
    const data = res.data.data
    profile.value = data
    profileForm.displayName = data.displayName || ''
    profileForm.bio = data.bio || ''
    settingsForm.privacyLevel = data.privacyLevel || 'PUBLIC'
    settingsForm.notifEmailEnabled = data.notifEmailEnabled ?? true
    settingsForm.notifPushEnabled = data.notifPushEnabled ?? true
    settingsForm.timezone = data.timezone || 'Asia/Shanghai'
    settingsForm.dailyFocusGoalMinutes = data.dailyFocusGoalMinutes ?? 120
  } catch (error: any) {
    message.error(error.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  loading.value = true
  try {
    await userApi.updateProfile(profileForm)
    message.success('更新成功')
    editingProfile.value = false
    await loadProfile()
  } catch (error: any) {
    message.error(error.response?.data?.message || '更新失败')
  } finally {
    loading.value = false
  }
}

async function saveSettings() {
  loading.value = true
  try {
    await userApi.updateSettings(settingsForm)
    message.success('设置更新成功')
    editingSettings.value = false
    await loadProfile()
  } catch (error: any) {
    message.error(error.response?.data?.message || '更新失败')
  } finally {
    loading.value = false
  }
}

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

function handleAvatarUpload(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  loading.value = true
  userApi.uploadAvatar(file)
    .then((res) => {
      const newAvatarUrl = res.data.data
      message.success('头像上传成功')
      authStore.updateAvatar(newAvatarUrl)
      loadProfile()
    })
    .catch((error: any) => {
      message.error(error.response?.data?.message || '上传失败')
    })
    .finally(() => {
      loading.value = false
      target.value = ''
    })
}

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <div class="profile-view gradient-mesh">
    <h2 class="page-title">个人资料</h2>

    <!-- Profile Card -->
    <NCard :bordered="false" class="profile-card glass stagger-in" :loading="loading">
      <div class="profile-header glass--subtle">
        <div class="avatar-section">
          <NAvatar round size="large" :src="profile?.avatarUrl || undefined" />
          <NButton text size="small" @click="triggerAvatarUpload">更换头像</NButton>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            @change="handleAvatarUpload"
            hidden
          />
        </div>
        <div class="profile-info">
          <h3 class="profile-display-name">{{ profile?.displayName || '未设置昵称' }}</h3>
          <p class="profile-email">{{ profile?.email }}</p>
          <NSpace>
            <NTag :type="profile?.emailVerified ? 'success' : 'warning'" size="small">
              {{ profile?.emailVerified ? '邮箱已验证' : '邮箱未验证' }}
            </NTag>
            <NTag size="small">{{ profile?.role === 'ADMIN' ? '管理员' : '普通用户' }}</NTag>
          </NSpace>
        </div>
      </div>

      <!-- Bio -->
      <div v-if="editingProfile" class="edit-section glass--subtle">
        <NForm :model="profileForm">
          <NFormItem label="昵称">
            <NInput v-model:value="profileForm.displayName" placeholder="请输入昵称" />
          </NFormItem>
          <NFormItem label="个人简介">
            <NInput v-model:value="profileForm.bio" type="textarea" placeholder="介绍一下自己..." :rows="3" />
          </NFormItem>
          <div class="edit-actions">
            <NButton type="primary" size="small" @click="saveProfile">保存</NButton>
            <NButton size="small" @click="editingProfile = false">取消</NButton>
          </div>
        </NForm>
      </div>

      <div v-else class="bio-section glass--subtle">
        <p class="bio-text">{{ profile?.bio || '暂无简介' }}</p>
        <NButton text size="small" @click="editingProfile = true">编辑资料</NButton>
      </div>

      <!-- Stats -->
      <div class="profile-stats glass--subtle">
        <div class="stat-item">
          <span class="stat-label">注册时间</span>
          <span class="stat-value">{{ formattedCreatedAt }}</span>
        </div>
      </div>
    </NCard>

    <!-- Settings Card -->
    <NCard :bordered="false" class="settings-card glass stagger-in" :loading="loading">
      <template #header>
        <div class="settings-header">
          <span class="settings-header-title">账户设置</span>
          <NButton v-if="!editingSettings" text size="small" @click="editingSettings = true">编辑</NButton>
        </div>
      </template>

      <div v-if="editingSettings" class="edit-section">
        <NForm :model="settingsForm">
          <NFormItem label="隐私设置">
            <NSelect v-model:value="settingsForm.privacyLevel" :options="privacyOptions" />
          </NFormItem>
          <NFormItem label="时区">
            <NSelect v-model:value="settingsForm.timezone" :options="timezoneOptions" />
          </NFormItem>
          <NFormItem label="邮件通知">
            <NSpace>
              <NSwitch v-model:value="settingsForm.notifEmailEnabled" />
              <span class="switch-label">{{ settingsForm.notifEmailEnabled ? '开启' : '关闭' }}</span>
            </NSpace>
          </NFormItem>
          <NFormItem label="推送通知">
            <NSpace>
              <NSwitch v-model:value="settingsForm.notifPushEnabled" />
              <span class="switch-label">{{ settingsForm.notifPushEnabled ? '开启' : '关闭' }}</span>
            </NSpace>
          </NFormItem>
          <NFormItem label="日目标专注时长（分钟）">
            <NInputNumber v-model:value="settingsForm.dailyFocusGoalMinutes" :min="1" :max="1440" placeholder="请输入日目标分钟数" class="full-width" />
          </NFormItem>
          <div class="edit-actions">
            <NButton type="primary" size="small" @click="saveSettings">保存</NButton>
            <NButton size="small" @click="editingSettings = false">取消</NButton>
          </div>
        </NForm>
      </div>

      <div v-else class="settings-list">
        <div class="setting-item">
          <span class="setting-label">隐私设置</span>
          <span class="setting-value">{{ privacyOptions.find(o => o.value === profile?.privacyLevel)?.label || '公开' }}</span>
        </div>
        <div class="setting-item">
          <span class="setting-label">时区</span>
          <span class="setting-value">{{ timezoneOptions.find(o => o.value === profile?.timezone)?.label || '亚洲/上海' }}</span>
        </div>
        <div class="setting-item">
          <span class="setting-label">邮件通知</span>
          <span class="setting-value">{{ profile?.notifEmailEnabled ? '开启' : '关闭' }}</span>
        </div>
        <div class="setting-item">
          <span class="setting-label">推送通知</span>
          <span class="setting-value">{{ profile?.notifPushEnabled ? '开启' : '关闭' }}</span>
        </div>
        <div class="setting-item">
          <span class="setting-label">日目标专注时长</span>
          <span class="setting-value">{{ profile?.dailyFocusGoalMinutes || 120 }} 分钟</span>
        </div>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.profile-view {
  max-width: var(--component-max-width);
}

.page-title {
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  margin: 0 0 var(--sp-4) 0;
  color: var(--text-color-strong);
}

/* --- Cards --- */
/* Re-declare the glass surface here so it wins over Naive UI's injected
   .n-card background (injected after main.css at equal specificity). */
.profile-card,
.settings-card {
  margin-bottom: var(--sp-3);
  border-radius: var(--radius-lg);
  padding: var(--sp-2);
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(20px) saturate(1.3);
  -webkit-backdrop-filter: blur(20px) saturate(1.3);
  border: 1px solid rgba(255, 255, 255, 0.22);
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.dark .profile-card,
.dark .settings-card {
  background: rgba(18, 18, 22, 0.62);
  border-color: rgba(255, 255, 255, 0.07);
  box-shadow:
    0 4px 32px rgba(0, 0, 0, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.profile-card:hover,
.settings-card:hover {
  border-color: rgba(59, 130, 246, 0.22);
  box-shadow:
    0 8px 32px rgba(59, 130, 246, 0.06),
    0 0 0 1px rgba(59, 130, 246, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

/* --- Profile Header --- */
.profile-header {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  margin-bottom: var(--sp-4);
  padding: var(--sp-4);
  border-radius: var(--radius-md);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--sp-2);
}

.profile-display-name {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  margin: 0 0 2px 0;
  color: var(--text-color-strong);
}

.profile-email {
  color: var(--text-color-muted);
  font-size: var(--text-sm);
  margin: 0 0 var(--sp-2) 0;
}

/* --- Bio / Edit --- */
.bio-section,
.edit-section {
  padding: var(--sp-3) var(--sp-4);
  margin-bottom: var(--sp-3);
  border-radius: var(--radius-md);
}

.bio-text {
  margin-bottom: var(--sp-2);
  color: var(--text-color-muted);
  font-size: var(--text-sm);
}

.edit-actions {
  display: flex;
  gap: var(--sp-2);
}

/* --- Stats --- */
.profile-stats {
  display: flex;
  gap: var(--sp-4);
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-md);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

.stat-value {
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-color);
}

/* --- Settings --- */
.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-header-title {
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-1);
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--sp-2) 0;
  border-bottom: 1px solid var(--separator);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-label {
  color: var(--text-color-muted);
  font-size: var(--text-sm);
}

.setting-value {
  font-weight: var(--weight-medium);
  font-size: var(--text-sm);
  color: var(--text-color);
}

.switch-label {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.full-width {
  width: 100%;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--sp-3);
  }

  .profile-stats {
    flex-direction: column;
    gap: var(--sp-2);
  }
}
</style>
