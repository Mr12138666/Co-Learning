<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useRoomStore } from '@/stores/roomStore'
import RoomCard from '@/components/room/RoomCard.vue'

const router = useRouter()
const message = useMessage()
const roomStore = useRoomStore()

const showCreateModal = ref(false)
const createForm = ref({
  name: '',
  description: '',
  maxMembers: 20,
  visibility: 'PUBLIC',
  password: '',
  topic: '',
})

onMounted(() => {
  roomStore.loadRooms()
})

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
  <div class="room-list-view">
    <div class="page-header">
      <n-space justify="space-between" align="center">
        <h2>陪伴房</h2>
        <n-button type="primary" @click="handleCreate">
          创建房间
        </n-button>
      </n-space>
    </div>

    <n-spin :show="roomStore.loading">
      <div v-if="roomStore.rooms.length > 0" class="room-grid">
        <RoomCard
          v-for="room in roomStore.rooms"
          :key="room.id"
          :room="room"
        />
      </div>
      <n-empty v-else description="暂无公开房间，快来创建第一个吧！" style="padding: 60px 0" />
    </n-spin>

    <!-- Create Room Modal -->
    <n-modal v-model:show="showCreateModal" preset="card" title="创建陪伴房" style="width: 500px">
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
  padding: 0 4px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}
</style>
