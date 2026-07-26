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
      message.success('注册成功！请检查邮箱完成验证')
      router.push({ name: 'verify-email' })
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
  <div class="auth-page">
    <div class="auth-brand">CL</div>
    <NCard :bordered="false" class="auth-card">
      <h1 class="auth-title">注册</h1>
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
</style>
