<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import {
  NCard,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NSpace,
  NAvatar,
  NTag,
  NSwitch,
  NSelect,
} from 'naive-ui'
import { userApi, type UserProfileResponse, type UpdateProfileRequest, type UpdateSettingsRequest } from '@/api/user'
import dayjs from 'dayjs'

const message = useMessage()
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
    .then(() => {
      message.success('头像上传成功')
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
  <div class="profile-view">
    <h2 class="page-title">个人资料</h2>

    <!-- Profile Card -->
    <NCard :bordered="false" class="profile-card" :loading="loading">
      <div class="profile-header">
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
          <h3>{{ profile?.displayName || '未设置昵称' }}</h3>
          <p class="email">{{ profile?.email }}</p>
          <NSpace>
            <NTag :type="profile?.emailVerified ? 'success' : 'warning'" size="small">
              {{ profile?.emailVerified ? '邮箱已验证' : '邮箱未验证' }}
            </NTag>
            <NTag size="small">{{ profile?.role === 'ADMIN' ? '管理员' : '普通用户' }}</NTag>
          </NSpace>
        </div>
      </div>

      <!-- Bio -->
      <div v-if="editingProfile" class="edit-section">
        <NForm :model="profileForm">
          <NFormItem label="昵称">
            <NInput v-model:value="profileForm.displayName" placeholder="请输入昵称" />
          </NFormItem>
          <NFormItem label="个人简介">
            <NInput v-model:value="profileForm.bio" type="textarea" placeholder="介绍一下自己..." :rows="3" />
          </NFormItem>
          <NSpace>
            <NButton type="primary" @click="saveProfile">保存</NButton>
            <NButton @click="editingProfile = false">取消</NButton>
          </NSpace>
        </NForm>
      </div>

      <div v-else class="bio-section">
        <p>{{ profile?.bio || '暂无简介' }}</p>
        <NButton text size="small" @click="editingProfile = true">编辑资料</NButton>
      </div>

      <!-- Stats -->
      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-label">注册时间</span>
          <span class="stat-value">{{ formattedCreatedAt }}</span>
        </div>
      </div>
    </NCard>

    <!-- Settings Card -->
    <NCard :bordered="false" class="settings-card" :loading="loading">
      <template #header>
        <div class="settings-header">
          <span>账户设置</span>
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
              <span>{{ settingsForm.notifEmailEnabled ? '开启' : '关闭' }}</span>
            </NSpace>
          </NFormItem>
          <NFormItem label="推送通知">
            <NSpace>
              <NSwitch v-model:value="settingsForm.notifPushEnabled" />
              <span>{{ settingsForm.notifPushEnabled ? '开启' : '关闭' }}</span>
            </NSpace>
          </NFormItem>
          <NFormItem label="日目标专注时长（分钟）">
            <NInput v-model:value="settingsForm.dailyFocusGoalMinutes" type="number" :min="1" :max="1440" placeholder="请输入日目标分钟数" />
          </NFormItem>
          <NSpace>
            <NButton type="primary" @click="saveSettings">保存</NButton>
            <NButton @click="editingSettings = false">取消</NButton>
          </NSpace>
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
  max-width: 600px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
}

.profile-card,
.settings-card {
  margin-bottom: 16px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.avatar-section {
  position: relative;
}

.upload-avatar-btn {
  display: block;
  margin-top: 8px;
  cursor: pointer;
}

.profile-info h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.email {
  color: var(--text-tertiary);
  font-size: 14px;
  margin: 0 0 8px 0;
}

.bio-section,
.edit-section {
  padding: 16px 0;
  border-top: 1px solid var(--border-color);
}

.bio-section p {
  margin-bottom: 8px;
  color: var(--text-secondary);
}

.profile-stats {
  display: flex;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 12px;
  color: var(--text-tertiary);
}

.stat-value {
  font-size: 14px;
  font-weight: 500;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.setting-label {
  color: var(--text-secondary);
}

.setting-value {
  font-weight: 500;
}
</style>
