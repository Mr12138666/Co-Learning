<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, useMessage } from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'

const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formData = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  displayName: '',
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少 8 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string) => value === formData.password,
      message: '两次输入的密码不一致',
      trigger: 'blur',
    },
  ],
  displayName: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
  ],
}

async function handleRegister() {
  formRef.value?.validate(async (errors) => {
    if (errors) return
    loading.value = true
    try {
      await authStore.register({
        email: formData.email,
        password: formData.password,
        displayName: formData.displayName,
      })
      message.success('注册申请已提交，请查收邮箱完成验证')
      router.push({ name: 'verify-email', query: { email: formData.email } })
    } catch (error: any) {
      const msg = error.response?.data?.message || '注册失败'
      message.error(msg)
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
      <h1 class="auth-title">创建账号 ✨</h1>
      <p class="auth-subtitle">加入伴学社区，一起成长</p>
      <NForm ref="formRef" :model="formData" :rules="rules" size="large">
        <NFormItem path="email" label="邮箱">
          <NInput v-model:value="formData.email" placeholder="请输入邮箱" />
        </NFormItem>
        <NFormItem path="displayName" label="昵称">
          <NInput v-model:value="formData.displayName" placeholder="请输入昵称" />
        </NFormItem>
        <NFormItem path="password" label="密码">
          <NInput v-model:value="formData.password" type="password" show-password-on="click" placeholder="至少 8 位" />
        </NFormItem>
        <NFormItem path="confirmPassword" label="确认密码">
          <NInput v-model:value="formData.confirmPassword" type="password" show-password-on="click" placeholder="再次输入密码" />
        </NFormItem>
        <NFormItem>
          <NSpace vertical class="auth-actions">
            <NButton type="primary" block :loading="loading" @click="handleRegister">
              注册
            </NButton>
            <NButton text block @click="router.push({ name: 'login' })">已有账号？去登录</NButton>
          </NSpace>
        </NFormItem>
      </NForm>
    </NCard>
  </div>
</template>

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

.auth-actions {
  width: 100%;
}
</style>
