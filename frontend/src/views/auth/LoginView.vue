<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, NAlert, useMessage } from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const unverifiedEmail = ref('')

const formData = reactive({
  email: '',
  password: '',
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
}

async function handleLogin() {
  formRef.value?.validate(async (errors) => {
    if (errors) return
    loading.value = true
    try {
      await authStore.login({ email: formData.email, password: formData.password })
      message.success('登录成功')
      const redirect = route.query.redirect as string
      router.push(redirect || '/dashboard')
    } catch (error: any) {
      const errorCode = error.response?.data?.code
      if (errorCode === 'AUTH-002') {
        unverifiedEmail.value = formData.email
        message.warning('邮箱未验证，请先完成验证')
      } else {
        const msg = error.response?.data?.message || '登录失败，请检查邮箱和密码'
        message.error(msg)
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="auth-page gradient-mesh">
    <div class="auth-brand">
      <span class="auth-brand__mark">CL</span>
      <span class="auth-brand__name">Co-Learning 伴学</span>
    </div>
    <NCard :bordered="false" class="auth-card glass--strong">
      <h1 class="auth-title">欢迎回来 👋</h1>
      <p class="auth-subtitle">继续你的专注之旅</p>
      <NAlert
        v-if="unverifiedEmail"
        type="warning"
        class="unverified-alert"
        :show-icon="true"
      >
        邮箱未验证，请先完成验证。
        <NButton
          text
          type="primary"
          @click="router.push({ name: 'verify-email', query: { email: unverifiedEmail } })"
        >
          去验证
        </NButton>
      </NAlert>
      <NForm ref="formRef" :model="formData" :rules="rules" size="large">
        <NFormItem path="email" label="邮箱">
          <NInput v-model:value="formData.email" placeholder="请输入邮箱" @keyup.enter="handleLogin" />
        </NFormItem>
        <NFormItem path="password" label="密码">
          <NInput v-model:value="formData.password" type="password" show-password-on="click" placeholder="请输入密码" @keyup.enter="handleLogin" />
        </NFormItem>
        <NFormItem>
          <NSpace vertical class="auth-actions">
            <NButton type="primary" block :loading="loading" @click="handleLogin">
              登录
            </NButton>
            <div class="auth-links">
              <NButton text @click="router.push({ name: 'register' })">注册新账号</NButton>
              <NButton text @click="router.push({ name: 'forgot-password' })">忘记密码？</NButton>
            </div>
          </NSpace>
        </NFormItem>
      </NForm>

      <div v-if="isDev" class="test-accounts">
        <p class="test-title">测试账号：</p>
        <p>管理员: admin@colearning.local / admin123</p>
        <p>学生: student@test.com / student123</p>
      </div>
    </NCard>
  </div>
</template>

<script lang="ts">
const isDev = import.meta.env.DEV
</script>

<style scoped>
.auth-page {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: var(--sp-5);
}

.auth-brand__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  font-size: var(--text-base);
  font-weight: var(--weight-bold);
  color: #fff;
  background: linear-gradient(135deg, #4f8cff, #7c5cff);
  box-shadow: 0 4px 18px rgba(79, 140, 255, 0.4);
}

.auth-brand__name {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  letter-spacing: 0.02em;
}

/* Glass surface re-declared here so it wins over Naive UI's injected
   .n-card background (injected after main.css at equal specificity). */
.auth-card {
  width: 100%;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(28px) saturate(1.5);
  -webkit-backdrop-filter: blur(28px) saturate(1.5);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    0 8px 40px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.dark .auth-card {
  background: rgba(18, 18, 22, 0.82);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 8px 48px rgba(0, 0, 0, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

/* glow-brand focus state for inputs */
.auth-card :deep(.n-input:focus-within) {
  border-color: var(--brand);
  box-shadow:
    0 0 0 1px rgba(59, 130, 246, 0.15),
    0 0 20px rgba(59, 130, 246, 0.12),
    0 0 40px rgba(59, 130, 246, 0.06);
}

.auth-title {
  margin: 0 0 var(--sp-2) 0;
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.auth-subtitle {
  margin: 0 0 var(--sp-5) 0;
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.unverified-alert {
  margin-bottom: var(--sp-4);
}

.auth-actions {
  width: 100%;
}

.auth-links {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.test-accounts {
  margin-top: var(--sp-4);
  padding: var(--sp-3);
  background-color: var(--bg-page);
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  border: 1px solid var(--separator);
}

.test-title {
  font-weight: var(--weight-bold);
  margin-bottom: var(--sp-1);
}
</style>
