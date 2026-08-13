<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NResult, NButton } from 'naive-ui'

const router = useRouter()
</script>

<template>
  <div class="not-found gradient-mesh">
    <div class="not-found__card glass">
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
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.not-found__card {
  padding: var(--sp-8) var(--sp-6);
  border-radius: var(--radius-2xl);
  max-width: 480px;
  width: 90vw;
}

.not-found__card :deep(.n-result-icon) {
  filter: drop-shadow(0 0 24px rgba(59, 130, 246, 0.35));
}
</style>
