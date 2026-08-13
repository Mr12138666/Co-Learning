<script setup lang="ts">
import { ref, computed } from 'vue'
import { NCard, NForm, NFormItem, NInput, NButton, NSwitch, NSelect, NTabs, NTabPane, NDivider, NList, NListItem, NThing, NTag, NAlert } from 'naive-ui'
import { useAuthStore } from '@/stores/authStore'
import { useThemeStore } from '@/stores/themeStore'
import ThemeSwitcher from '@/components/common/ThemeSwitcher.vue'

const authStore = useAuthStore()
const themeStore = useThemeStore()

// Profile settings
const profileForm = ref({
  displayName: authStore.user?.displayName || '',
  email: authStore.user?.email || '',
  bio: '',
})

// Notification settings
const notifications = ref({
  email: true,
  push: false,
  weeklyReport: true,
  dailyReminder: true,
  reminderTime: '09:00',
})

// Privacy settings
const privacy = ref({
  profileVisibility: 'public',
  showOnlineStatus: true,
  showStudyStats: true,
})

// Theme options
const themeOptions = [
  { label: '浅色模式', value: 'light' },
  { label: '深色模式', value: 'dark' },
  { label: '蓝色主题', value: 'blue' },
  { label: '绿色主题', value: 'green' },
  { label: '紫色主题', value: 'purple' },
]

// Language options
const languageOptions = [
  { label: '简体中文', value: 'zh-CN' },
  { label: 'English', value: 'en' },
]

const selectedLanguage = ref('zh-CN')

function handleSaveProfile() {
  // TODO: Save profile
  console.log('Save profile:', profileForm.value)
}

function handleSaveNotifications() {
  // TODO: Save notifications
  console.log('Save notifications:', notifications.value)
}

function handleSavePrivacy() {
  // TODO: Save privacy
  console.log('Save privacy:', privacy.value)
}
</script>

<template>
  <div class="settings-view">
    <h1 class="settings-view__title">设置</h1>
    
    <n-tabs type="line" animated>
      <n-tab-pane name="profile" tab="个人资料">
        <n-card>
          <n-form :model="profileForm" label-placement="left" label-width="100">
            <n-form-item label="显示名称">
              <n-input v-model:value="profileForm.displayName" placeholder="输入显示名称" />
            </n-form-item>
            <n-form-item label="邮箱">
              <n-input :value="profileForm.email" disabled />
            </n-form-item>
            <n-form-item label="个人简介">
              <n-input
                v-model:value="profileForm.bio"
                type="textarea"
                placeholder="介绍一下自己"
                :rows="3"
              />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-button type="primary" @click="handleSaveProfile">保存</n-button>
          </template>
        </n-card>
      </n-tab-pane>
      
      <n-tab-pane name="appearance" tab="外观">
        <n-card>
          <n-list>
            <n-list-item>
              <n-thing title="主题模式">
                <template #description>
                  选择应用的外观主题
                </template>
                <template #header-extra>
                  <ThemeSwitcher />
                </template>
              </n-thing>
            </n-list-item>
            
            <n-list-item>
              <n-thing title="语言">
                <template #description>
                  选择应用界面语言
                </template>
                <template #header-extra>
                  <n-select
                    v-model:value="selectedLanguage"
                    :options="languageOptions"
                    style="width: 150px"
                  />
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-card>
      </n-tab-pane>
      
      <n-tab-pane name="notifications" tab="通知">
        <n-card>
          <n-form :model="notifications" label-placement="left" label-width="120">
            <n-form-item label="邮件通知">
              <n-switch v-model:value="notifications.email" />
            </n-form-item>
            <n-form-item label="推送通知">
              <n-switch v-model:value="notifications.push" />
            </n-form-item>
            <n-divider />
            <n-form-item label="每周报告">
              <n-switch v-model:value="notifications.weeklyReport" />
            </n-form-item>
            <n-form-item label="每日提醒">
              <n-switch v-model:value="notifications.dailyReminder" />
            </n-form-item>
            <n-form-item v-if="notifications.dailyReminder" label="提醒时间">
              <n-input v-model:value="notifications.reminderTime" placeholder="09:00" style="width: 120px" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-button type="primary" @click="handleSaveNotifications">保存</n-button>
          </template>
        </n-card>
      </n-tab-pane>
      
      <n-tab-pane name="privacy" tab="隐私">
        <n-card>
          <n-form :model="privacy" label-placement="left" label-width="120">
            <n-form-item label="个人资料可见性">
              <n-select
                v-model:value="privacy.profileVisibility"
                :options="[
                  { label: '公开', value: 'public' },
                  { label: '仅好友', value: 'friends' },
                  { label: '私密', value: 'private' },
                ]"
              />
            </n-form-item>
            <n-form-item label="显示在线状态">
              <n-switch v-model:value="privacy.showOnlineStatus" />
            </n-form-item>
            <n-form-item label="显示学习统计">
              <n-switch v-model:value="privacy.showStudyStats" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-button type="primary" @click="handleSavePrivacy">保存</n-button>
          </template>
        </n-card>
      </n-tab-pane>
      
      <n-tab-pane name="account" tab="账户">
        <n-card>
          <n-alert type="warning" title="账户安全" style="margin-bottom: 16px">
            请定期修改密码，确保账户安全。
          </n-alert>
          
          <n-list>
            <n-list-item>
              <n-thing title="修改密码">
                <template #description>
                  定期修改密码可以提高账户安全性
                </template>
                <template #header-extra>
                  <n-button>修改密码</n-button>
                </template>
              </n-thing>
            </n-list-item>
            
            <n-list-item>
              <n-thing title="两步验证">
                <template #description>
                  启用两步验证可以进一步提高账户安全性
                </template>
                <template #header-extra>
                  <n-tag type="warning">未启用</n-tag>
                </template>
              </n-thing>
            </n-list-item>
            
            <n-list-item>
              <n-thing title="登录历史">
                <template #description>
                  查看最近的登录活动
                </template>
                <template #header-extra>
                  <n-button quaternary>查看</n-button>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-card>
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<style scoped>
.settings-view {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--sp-6);
}

.settings-view__title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-color);
  margin: 0 0 var(--sp-6);
}
</style>