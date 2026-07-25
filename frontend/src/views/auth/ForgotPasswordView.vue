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
  <NCard title="忘记密码" size="large" :bordered="false">
    <template v-if="sent">
      <NAlert type="info" title="邮件已发送" show-icon>
        如果该邮箱已注册，您将收到一封密码重置邮件。请检查收件箱。
      </NAlert>
      <div style="margin-top: 16px; text-align: center;">
        <NButton text @click="router.push({ name: 'login' })">返回登录</NButton>
      </div>
    </template>
    <template v-else>
      <p style="margin-bottom: 16px; color: #666;">输入注册邮箱，我们将发送密码重置链接。</p>
      <NForm>
        <NFormItem label="邮箱">
          <NInput v-model:value="formData.email" placeholder="请输入注册邮箱" />
        </NFormItem>
        <NFormItem>
          <NSpace vertical style="width: 100%;">
            <NButton type="primary" block :loading="loading" @click="handleSend">发送重置链接</NButton>
            <NButton text block @click="router.push({ name: 'login' })">返回登录</NButton>
          </NSpace>
        </NFormItem>
      </NForm>
    </template>
  </NCard>
</template>
