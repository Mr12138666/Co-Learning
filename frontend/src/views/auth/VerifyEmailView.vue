<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NCard, NInput, NButton, NSpace, NAlert, useMessage } from 'naive-ui'
import { authApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const email = ref((route.query.email as string) || '')
const code = ref((route.query.token as string) || '')
const loading = ref(false)
const verified = ref(false)
const resending = ref(false)
const resendCountdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

function startCountdown() {
  resendCountdown.value = 60
  countdownTimer = setInterval(() => {
    resendCountdown.value--
    if (resendCountdown.value <= 0) {
      if (countdownTimer) clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

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
    message.error(error.response?.data?.message || '验证失败，请检查验证码')
  } finally {
    loading.value = false
  }
}

async function handleResend() {
  if (!email.value) {
    message.error('请先填写注册邮箱')
    return
  }
  if (resendCountdown.value > 0) return
  resending.value = true
  try {
    await authApi.resendVerification(email.value)
    message.success('验证码已重新发送，请查收邮箱')
    startCountdown()
  } catch (error: any) {
    message.error(error.response?.data?.message || '发送失败，请稍后重试')
  } finally {
    resending.value = false
  }
}
</script>

<template>
  <div class="auth-page gradient-mesh">
    <div class="auth-brand">CL</div>
    <NCard :bordered="false" class="auth-card glass--strong">
      <h1 class="auth-title">邮箱验证</h1>
      <template v-if="verified">
        <NAlert type="success" title="验证成功" show-icon>
          您的邮箱已验证成功，即将跳转到登录页面...
        </NAlert>
      </template>
      <template v-else>
        <p class="form-description">
          验证码已发送至 <strong>{{ email || '您的邮箱' }}</strong>，请输入6位验证码完成验证。
        </p>
        <NSpace vertical class="auth-actions">
          <NInput v-model:value="code" placeholder="输入6位验证码" maxlength="6" />
          <NButton type="primary" block :loading="loading" @click="handleVerify">验证</NButton>
          <div class="resend-row">
            <NButton
              text
              :disabled="resendCountdown > 0 || resending"
              :loading="resending"
              @click="handleResend"
            >
              {{ resendCountdown > 0 ? `${resendCountdown}s 后可重新发送` : '重新发送验证码' }}
            </NButton>
          </div>
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

.resend-row {
  text-align: center;
}
</style>
