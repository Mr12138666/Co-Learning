<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NResult, NButton } from 'naive-ui'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <NResult
      status="404"
      title="页面不存在"
      description="您访问的页面不存在或已被移动。"
    >
      <template #footer>
        <NButton type="primary" @click="router.push('/')">返回首页</NButton>
      </template>
    </NResult>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--bg-page);
}
</style>
