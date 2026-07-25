import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  journalApi,
  type Journal,
  type CreateJournalRequest,
  type UpdateJournalRequest,
  type JournalListParams,
} from '@/api/journal'

export const useJournalStore = defineStore('journal', () => {
  // State
  const journals = ref<Journal[]>([])
  const publicJournals = ref<Journal[]>([])
  const currentJournal = ref<Journal | null>(null)
  const total = ref(0)
  const loading = ref(false)

  // Actions
  async function fetchMyJournals(params?: JournalListParams) {
    loading.value = true
    try {
      const res = await journalApi.listMy(params)
      const pageData = res.data.data
      journals.value = pageData.items ?? pageData
      total.value = pageData.total ?? journals.value.length
    } finally {
      loading.value = false
    }
  }

  async function fetchPublicJournals(params?: JournalListParams) {
    const res = await journalApi.listPublic(params)
    const pageData = res.data.data
    publicJournals.value = pageData.items ?? pageData
  }

  async function fetchById(id: number) {
    const res = await journalApi.getById(id)
    currentJournal.value = res.data.data
    return res.data.data as Journal
  }

  async function create(data: CreateJournalRequest) {
    const res = await journalApi.create(data)
    const journal = res.data.data as Journal
    journals.value.unshift(journal)
    return journal
  }

  async function update(id: number, data: UpdateJournalRequest) {
    const res = await journalApi.update(id, data)
    const journal = res.data.data as Journal
    const index = journals.value.findIndex((j) => j.id === id)
    if (index !== -1) journals.value[index] = journal
    if (currentJournal.value?.id === id) currentJournal.value = journal
    return journal
  }

  async function remove(id: number) {
    await journalApi.delete(id)
    journals.value = journals.value.filter((j) => j.id !== id)
  }

  async function publish(id: number) {
    const res = await journalApi.publish(id)
    const journal = res.data.data as Journal
    const index = journals.value.findIndex((j) => j.id === id)
    if (index !== -1) journals.value[index] = journal
    if (currentJournal.value?.id === id) currentJournal.value = journal
    return journal
  }

  return {
    journals,
    publicJournals,
    currentJournal,
    total,
    loading,
    fetchMyJournals,
    fetchPublicJournals,
    fetchById,
    create,
    update,
    remove,
    publish,
  }
})
