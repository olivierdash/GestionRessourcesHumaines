import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { jobsApi } from '@/services/jobs'

export const useJobsStore = defineStore('jobs', () => {
  // Données brutes
  const all = ref([])
  const current = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Filtres et pagination côté client
  const filters = ref({ q: '', departement: '' })
  const page = ref(1)
  const perPage = ref(10)

  // Liste filtrée
  const filtered = computed(() => {
    let result = all.value

    if (filters.value.q) {
      const q = filters.value.q.toLowerCase()
      result = result.filter(
        (j) =>
          (j.titre || '').toLowerCase().includes(q) ||
          (j.intitulePoste || '').toLowerCase().includes(q) ||
          (j.lieu || '').toLowerCase().includes(q) ||
          (j.description || '').toLowerCase().includes(q)
      )
    }

    if (filters.value.departement) {
      result = result.filter((j) => j.departement === filters.value.departement)
    }

    return result
  })

  // Page courante
  const list = computed(() => {
    const start = (page.value - 1) * perPage.value
    return filtered.value.slice(start, start + perPage.value)
  })

  const total = computed(() => filtered.value.length)
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / perPage.value)))

  // Départements disponibles (dérivés de la liste complète)
  const departements = computed(() => {
    const set = new Set(all.value.map((j) => j.departement).filter(Boolean))
    return [...set].sort()
  })

  // Pagination exposée comme avant (mais en lecture seule)
  const pagination = computed(() => ({
    page: page.value,
    total: total.value,
    perPage: perPage.value,
    totalPages: totalPages.value,
  }))

  async function fetchJobs() {
    loading.value = true
    error.value = null
    try {
      const data = await jobsApi.list() // tableau brut
      all.value = Array.isArray(data) ? data : []
    } catch (e) {
      error.value = e.message ?? 'Erreur lors du chargement des offres.'
      all.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchJob(id) {
    loading.value = true
    error.value = null
    try {
      current.value = await jobsApi.detail(id)
    } catch (e) {
      error.value = e.message ?? "Erreur lors du chargement de l'offre."
      current.value = null
    } finally {
      loading.value = false
    }
  }

  function setFilters(newFilters) {
    filters.value = { ...filters.value, ...newFilters }
    page.value = 1 // reset pagination quand un filtre change
  }

  function goToPage(p) {
    if (p >= 1 && p <= totalPages.value) page.value = p
  }

  function resetFilters() {
    filters.value = { q: '', departement: '' }
    page.value = 1
  }

  function resetCurrent() {
    current.value = null
  }

  return {
    // état
    all,
    current,
    loading,
    error,
    filters,
    page,
    perPage,
    // computed
    list,
    total,
    totalPages,
    departements,
    pagination,
    // actions
    fetchJobs,
    fetchJob,
    setFilters,
    goToPage,
    resetFilters,
    resetCurrent,
  }
})