<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NCard, NInput, NButton, NSpace, NAlert, useMessage } from 'naive-ui'
import { authApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const token = ref((route.query.token as string) || '')
const loading = ref(false)
const verified = ref(false)

async function handleVerify() {
  if (!token.value) {
    message.error('请输入验证令牌')
    return
  }
  loading.value = true
  try {
    await authApi.verifyEmail(token.value)
    verified.value = true
    message.success('邮箱验证成功！')
    setTimeout(() => router.push({ name: 'login' }), 2000)
  } catch (error: any) {
    message.error(error.response?.data?.message || '验证失败，请检查令牌或重新注册')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <NCard title="邮箱验证" size="large" :bordered="false">
    <template v-if="verified">
      <NAlert type="success" title="验证成功" show-icon>
        您的邮箱已验证成功，即将跳转到登录页面...
      </NAlert>
    </template>
    <template v-else>
      <p style="margin-bottom: 16px; color: #666;">请输入您收到的验证令牌，或点击邮件中的验证链接。</p>
      <NSpace vertical>
        <NInput v-model:value="token" placeholder="输入验证令牌" />
        <NButton type="primary" block :loading="loading" @click="handleVerify">验证</NButton>
        <NButton text block @click="router.push({ name: 'login' })">返回登录</NButton>
      </NSpace>
    </template>
  </NCard>
</template>
