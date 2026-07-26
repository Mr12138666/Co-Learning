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
  <NCard title="登录" size="large" :bordered="false">
    <NForm ref="formRef" :model="formData" :rules="rules" size="large">
      <NFormItem path="email" label="邮箱">
        <NInput v-model:value="formData.email" placeholder="请输入邮箱" @keyup.enter="handleLogin" />
      </NFormItem>
      <NFormItem path="password" label="密码">
        <NInput v-model:value="formData.password" type="password" show-password-on="click" placeholder="请输入密码" @keyup.enter="handleLogin" />
      </NFormItem>
      <NFormItem>
        <NSpace vertical style="width: 100%;">
          <NButton type="primary" block :loading="loading" @click="handleLogin">
            登录
          </NButton>
          <NSpace justify="space-between" style="width: 100%;">
            <NButton text @click="router.push({ name: 'register' })">注册新账号</NButton>
            <NButton text @click="router.push({ name: 'forgot-password' })">忘记密码？</NButton>
          </NSpace>
        </NSpace>
      </NFormItem>
    </NForm>

    <div class="test-accounts">
      <p class="test-title">测试账号：</p>
      <p>管理员: admin@colearning.local / admin123</p>
      <p>学生: student@test.com / student123</p>
    </div>
  </NCard>
</template>

<style scoped>
.test-accounts {
  margin-top: 16px;
  padding: 12px;
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--text-tertiary);
}

.test-title {
  font-weight: bold;
  margin-bottom: 4px;
}
</style>
