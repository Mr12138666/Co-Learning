<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NCard,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NSpace,
  NAlert,
  useMessage,
  type FormInst,
  type FormRules,
} from 'naive-ui'
import { authApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const loading = ref(false)
const success = ref(false)
const formRef = ref<FormInst | null>(null)

const formData = reactive({
  code: (route.query.token as string) || '',
  newPassword: '',
  confirmPassword: '',
})

const rules: FormRules = {
  code: {
    required: true,
    message: '请输入6位验证码',
    trigger: 'blur',
  },
  newPassword: [
    {
      required: true,
      message: '请输入新密码',
      trigger: 'blur',
    },
    {
      min: 8,
      message: '密码至少 8 个字符',
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    {
      required: true,
      message: '请确认密码',
      trigger: 'blur',
    },
    {
      validator: (_rule: unknown, value: string) => {
        if (value !== formData.newPassword) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur',
    },
  ],
}

async function handleReset() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  if (formData.code.length !== 6) {
    message.error('请输入6位验证码')
    return
  }
  loading.value = true
  try {
    await authApi.resetPassword(formData.code, formData.newPassword)
    success.value = true
    message.success('密码重置成功')
  } catch (error: any) {
    message.error(error.response?.data?.message || '重置失败，验证码可能已过期')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <NCard title="重置密码" size="large" :bordered="false">
    <template v-if="success">
      <NAlert type="success" title="密码已重置" show-icon>
        您的密码已成功重置，请使用新密码登录。
      </NAlert>
      <div style="margin-top: 16px; text-align: center;">
        <NButton type="primary" @click="router.push({ name: 'login' })">前往登录</NButton>
      </div>
    </template>
    <template v-else>
      <p class="form-description">
        输入邮件中的6位验证码以及您的新密码。
      </p>
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="top">
        <NFormItem label="验证码" path="code">
          <NInput
            v-model:value="formData.code"
            placeholder="请输入邮件中的6位验证码"
            maxlength="6"
          />
        </NFormItem>
        <NFormItem label="新密码" path="newPassword">
          <NInput
            v-model:value="formData.newPassword"
            type="password"
            show-password-on="click"
            placeholder="至少 8 个字符"
          />
        </NFormItem>
        <NFormItem label="确认密码" path="confirmPassword">
          <NInput
            v-model:value="formData.confirmPassword"
            type="password"
            show-password-on="click"
            placeholder="再次输入新密码"
          />
        </NFormItem>
        <NFormItem>
          <NSpace vertical style="width: 100%;">
            <NButton type="primary" block :loading="loading" @click="handleReset">
              重置密码
            </NButton>
            <NButton text block @click="router.push({ name: 'login' })">返回登录</NButton>
          </NSpace>
        </NFormItem>
      </NForm>
    </template>
  </NCard>
</template>

<style scoped>
.form-description {
  margin-bottom: 16px;
  color: var(--text-secondary);
}
</style>
