<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, NAlert, useMessage } from 'naive-ui'
import { authApi } from '@/api/auth'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const sent = ref(false)
const formData = reactive({ email: '' })

async function handleSend() {
  if (!formData.email) {
    message.error('请输入邮箱')
    return
  }
  loading.value = true
  try {
    await authApi.forgotPassword(formData.email)
    sent.value = true
  } catch (error: any) {
    message.error(error.response?.data?.message || '发送失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-brand">CL</div>
    <NCard :bordered="false" class="auth-card">
      <h1 class="auth-title">忘记密码</h1>
      <template v-if="sent">
        <NAlert type="info" title="验证码已发送" show-icon>
          如果该邮箱已注册，您将收到一封包含6位验证码的邮件。请检查收件箱。
        </NAlert>
        <div class="auth-links-sent">
          <NButton text @click="router.push({ name: 'reset-password' })">去重置密码</NButton>
          <NButton text block @click="router.push({ name: 'login' })">返回登录</NButton>
        </div>
      </template>
      <template v-else>
        <p class="form-description">输入注册邮箱，我们将发送密码重置验证码。</p>
        <NForm>
          <NFormItem label="邮箱">
            <NInput v-model:value="formData.email" placeholder="请输入注册邮箱" />
          </NFormItem>
          <NFormItem>
            <NSpace vertical class="auth-actions">
              <NButton type="primary" block :loading="loading" @click="handleSend">发送验证码</NButton>
              <NButton text block @click="router.push({ name: 'login' })">返回登录</NButton>
            </NSpace>
          </NFormItem>
        </NForm>
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

.auth-links-sent {
  margin-top: var(--sp-4);
  text-align: center;
}
</style>
