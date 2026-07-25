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
  <NCard title="注册" size="large" :bordered="false">
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
        <NSpace vertical style="width: 100%;">
          <NButton type="primary" block :loading="loading" @click="handleRegister">
            注册
          </NButton>
          <NButton text block @click="router.push({ name: 'login' })">已有账号？去登录</NButton>
        </NSpace>
      </NFormItem>
    </NForm>
  </NCard>
</template>
