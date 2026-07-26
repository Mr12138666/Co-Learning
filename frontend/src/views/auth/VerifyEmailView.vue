<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NCard, NInput, NButton, NSpace, NAlert, useMessage } from 'naive-ui'
import { authApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const code = ref((route.query.token as string) || '')
const loading = ref(false)
const verified = ref(false)

async function handleVerify() {
  if (!code.value || code.value.length !== 6) {
    message.error('请输入6位验证码')
    return
  }
  loading.value = true
  try {
    await authApi.verifyEmail(code.value)
    verified.value = true
    message.success('邮箱验证成功！')
    setTimeout(() => router.push({ name: 'login' }), 2000)
  } catch (error: any) {
    message.error(error.response?.data?.message || '验证失败，请检查验证码或重新注册')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-brand">CL</div>
    <NCard :bordered="false" class="auth-card">
      <h1 class="auth-title">邮箱验证</h1>
      <template v-if="verified">
        <NAlert type="success" title="验证成功" show-icon>
          您的邮箱已验证成功，即将跳转到登录页面...
        </NAlert>
      </template>
      <template v-else>
        <p class="form-description">请输入您收到的6位邮箱验证码。</p>
        <NSpace vertical class="auth-actions">
          <NInput v-model:value="code" placeholder="输入6位验证码" maxlength="6" />
          <NButton type="primary" block :loading="loading" @click="handleVerify">验证</NButton>
          <NButton text block @click="router.push({ name: 'login' })">返回登录</NButton>
        </NSpace>
      </template>
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

.form-description {
  margin-bottom: var(--sp-4);
  color: var(--text-color-muted);
  font-size: var(--text-sm);
}
</style>
