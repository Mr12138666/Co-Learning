<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, useMessage } from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

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
      const msg = error.response?.data?.message || '登录失败，请检查邮箱和密码'
      message.error(msg)
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-brand">CL</div>
    <NCard :bordered="false" class="auth-card">
      <h1 class="auth-title">登录</h1>
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
  font-size: var(--text-2xl);
  font-weight: var(--weight-bold);
  color: var(--brand);
  letter-spacing: 0.08em;
  margin-bottom: var(--sp-5);
}

.auth-card {
  width: 100%;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.auth-title {
  margin: 0 0 var(--sp-5) 0;
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
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
