<template>
  <div class="container-app py-8">
    <header class="mb-6">
      <h1 class="text-2xl font-bold">Offres d'emploi</h1>
      <p class="text-gray-500 mt-1 text-sm">{{ totalLabel }}</p>
    </header>

    <JobFilters
      v-model:search="filters.search"
      v-model:departement="filters.departement"
      :departements="jobsStore.departements"
      @reset="onResetFilters"
    />

    <div v-if="jobsStore.loading" class="state">Chargement des offres…</div>
    <div v-else-if="jobsStore.error" class="state text-red-600">{{ jobsStore.error }}</div>
    <div v-else-if="jobsStore.list.length === 0" class="state">
      Aucune offre trouvée.
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <JobCard v-for="job in jobsStore.list" :key="job.id" :job="job" />
    </div>

    <nav
      v-if="jobsStore.totalPages > 1"
      class="flex justify-center items-center gap-4 mt-8"
      aria-label="Pagination des offres"
    >
      <button
        class="btn btn-secondary"
        :disabled="jobsStore.page <= 1"
        @click="jobsStore.goToPage(jobsStore.page - 1)"
      >
        Précédent
      </button>
      <span class="text-sm text-gray-500">
        Page {{ jobsStore.page }} / {{ jobsStore.totalPages }}
      </span>
      <button
        class="btn btn-secondary"
        :disabled="jobsStore.page >= jobsStore.totalPages"
        @click="jobsStore.goToPage(jobsStore.page + 1)"
      >
        Suivant
      </button>
    </nav>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, watch } from 'vue'
import JobFilters from '@/components/jobs/JobFilters.vue'
import JobCard from '@/components/jobs/JobCard.vue'
import { useJobsStore } from '@/stores/jobs'

const jobsStore = useJobsStore()

const filters = reactive({
  search: '',
  departement: '',
})

const totalLabel = computed(() => {
  const total = jobsStore.total
  if (total === 0) return 'Aucune offre disponible'
  return `${total} offre${total > 1 ? 's' : ''} disponible${total > 1 ? 's' : ''}`
})

let debounceTimer = null

watch(
  () => [filters.search, filters.departement],
  () => {
    clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      jobsStore.setFilters({
        q: filters.search,
        departement: filters.departement,
      })
    }, 300)
  }
)

function onResetFilters() {
  jobsStore.resetFilters()
}

onMounted(() => {
  jobsStore.fetchJobs()
})
</script>