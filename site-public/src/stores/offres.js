import { ref } from 'vue'
import { defineStore } from 'pinia'
import { jobsApi } from '@/services/jobs'

export const useJobsStore = defineStore('jobs', () => {
  const list = ref([])
  const current = ref(null)
  const loading = ref(false)
  const pagination = ref({ page: 1, total: 0, perPage: 10 })

  async function fetchJobs(params = {}) {
    loading.value = true
    try {
      const data = await jobsApi.list(params)
      list.value = data.items
      pagination.value = {
        page: data.page,
        total: data.total,
        perPage: data.perPage,
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchJob(id) {
    loading.value = true
    try {
      const data = await jobsApi.detail(id)
      current.value = data
    } finally {
      loading.value = false
    }
  }

  return { list, current, loading, pagination, fetchJobs, fetchJob }
})