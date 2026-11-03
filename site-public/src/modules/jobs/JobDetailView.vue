<template>
  <div class="container-app py-8 max-w-3xl">
    <div v-if="jobsStore.loading" class="state">Chargement…</div>
    <div v-else-if="!jobsStore.current" class="state">
      Offre introuvable.
      <RouterLink to="/offres" class="text-primary hover:underline">Retour aux offres</RouterLink>
    </div>
    <article v-else class="flex flex-col gap-6">
      <header class="flex justify-between items-start gap-6 flex-wrap">
        <div>
          <h1 class="text-3xl font-bold mb-3">{{ jobsStore.current.titre }}</h1>
          <ul class="flex flex-wrap gap-4 text-sm text-gray-500 list-none p-0 m-0">
            <li v-if="jobsStore.current.departement">🏢 {{ jobsStore.current.departement }}</li>
            <li v-if="jobsStore.current.lieu">📍 {{ jobsStore.current.lieu }}</li>
            <li v-if="jobsStore.current.type_contrat">📄 {{ jobsStore.current.type_contrat }}</li>
          </ul>
        </div>
      </header>

      <section v-if="jobsStore.current.description">
        <h2 class="text-lg font-semibold mb-2">Description du poste</h2>
        <p class="whitespace-pre-line text-gray-700">{{ jobsStore.current.description }}</p>
      </section>

      <section v-if="jobsStore.current.profil_recherche">
        <h2 class="text-lg font-semibold mb-2">Profil recherché</h2>
        <p class="whitespace-pre-line text-gray-700">{{ jobsStore.current.profil_recherche }}</p>
      </section>

      <footer class="flex flex-wrap gap-3 pt-4 border-t border-gray-200">
        <RouterLink
          :to="{ name: 'job-apply', params: { id: jobsStore.current.id } }"
          class="btn btn-primary"
        >
          Postuler à cette offre
        </RouterLink>
        <RouterLink to="/offres" class="btn btn-secondary">Retour aux offres</RouterLink>
      </footer>
    </article>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useJobsStore } from '@/stores/jobs'

const props = defineProps({ id: { type: [String, Number], required: true } })
const jobsStore = useJobsStore()

onMounted(() => jobsStore.fetchJob(props.id))
</script>