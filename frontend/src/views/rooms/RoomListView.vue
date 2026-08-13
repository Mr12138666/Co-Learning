<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NSpin } from 'naive-ui'
import { useRoomStore } from '@/stores/roomStore'
import { usePageLoad } from '@/composables/usePageLoad'
import StateError from '@/components/common/StateError.vue'
import RoomCard from '@/components/room/RoomCard.vue'

const router = useRouter()
const message = useMessage()
const roomStore = useRoomStore()
const { loading, error, load, retry } = usePageLoad()

const showCreateModal = ref(false)
const createForm = ref({
  name: '',
  description: '',
  maxMembers: 20,
  visibility: 'PUBLIC',
  password: '',
  topic: '',
})

onMounted(() => load(async () => {
  await roomStore.loadRooms()
}))

function handleCreate() {
  showCreateModal.value = true
}

async function submitCreate() {
  if (!createForm.value.name.trim()) {
    message.warning('请输入房间名称')
    return
  }
  if (createForm.value.visibility === 'PRIVATE' && !createForm.value.password) {
    message.warning('私密房间需要设置密码')
    return
  }

  try {
    const room = await roomStore.createRoom({
      name: createForm.value.name,
      description: createForm.value.description || undefined,
      maxMembers: createForm.value.maxMembers,
      visibility: createForm.value.visibility,
      password: createForm.value.visibility === 'PRIVATE' ? createForm.value.password : undefined,
      topic: createForm.value.topic || undefined,
    })
    message.success('房间创建成功')
    showCreateModal.value = false
    createForm.value = {
      name: '', description: '', maxMembers: 20,
      visibility: 'PUBLIC', password: '', topic: '',
    }
    router.push(`/rooms/${room.id}`)
  } catch {
    message.error('创建失败')
  }
}
</script>

<template>
  <div class="room-list-view gradient-mesh">
    <!-- Loading -->
    <div v-if="loading" class="loading-center">
      <NSpin size="large" />
    </div>

    <!-- Error -->
    <StateError
      v-else-if="error"
      :title="error"
      @retry="retry(async () => {
        await roomStore.loadRooms()
      })"
    />

    <template v-else>
      <!-- Page Header -->
      <div class="page-header glass">
        <h3 class="page-title">陪伴房</h3>
        <n-button type="primary" size="small" @click="handleCreate">
          创建房间
        </n-button>
      </div>

      <n-spin :show="roomStore.loading">
        <div v-if="roomStore.rooms.length > 0" class="room-grid">
          <RoomCard
            v-for="room in roomStore.rooms"
            :key="room.id"
            :room="room"
          />
        </div>
        <div v-else class="empty-container">
          <n-empty description="暂无公开房间，快来创建第一个吧！" />
        </div>
      </n-spin>
    </template>

    <!-- Create Room Modal -->
    <n-modal v-model:show="showCreateModal" preset="card" title="创建陪伴房" class="create-modal glass--strong">
      <n-form label-placement="top">
        <n-form-item label="房间名称" required>
          <n-input v-model:value="createForm.name" placeholder="给房间起个名字" maxlength="100" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input
            v-model:value="createForm.description"
            type="textarea"
            placeholder="房间简介（选填）"
            maxlength="500"
          />
        </n-form-item>
        <n-form-item label="主题">
          <n-input v-model:value="createForm.topic" placeholder="如：考研数学、英语六级" maxlength="200" />
        </n-form-item>
        <n-form-item label="最大人数">
          <n-input-number v-model:value="createForm.maxMembers" :min="2" :max="100" />
        </n-form-item>
        <n-form-item label="可见性">
          <n-radio-group v-model:value="createForm.visibility">
            <n-radio value="PUBLIC">公开</n-radio>
            <n-radio value="PRIVATE">私密（需密码）</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item v-if="createForm.visibility === 'PRIVATE'" label="房间密码" required>
          <n-input v-model:value="createForm.password" type="password" placeholder="设置房间密码" />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreateModal = false">取消</n-button>
          <n-button type="primary" @click="submitCreate">创建</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.room-list-view {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: var(--sp-4);
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: var(--sp-12) 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-4);
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-md);
}

.page-title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.empty-container {
  padding: var(--sp-12) 0;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(320px, 100%), 1fr));
  gap: var(--sp-3);
}

/* Glass surface re-declared here so it wins over Naive UI's injected
   .n-card background (injected after main.css at equal specificity). */
.create-modal {
  max-width: 500px;
  width: 90vw;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(28px) saturate(1.5);
  -webkit-backdrop-filter: blur(28px) saturate(1.5);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    0 8px 40px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.dark .create-modal {
  background: rgba(18, 18, 22, 0.82);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 8px 48px rgba(0, 0, 0, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
}
</style>
