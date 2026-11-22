<template>
  <RequireAuth>
    <div class="container-app py-8 max-w-3xl">
      <header class="mb-6">
        <RouterLink
          :to="{ name: 'my-applications' }"
          class="text-sm text-gray-500 hover:text-gray-900"
        >
          ← Retour à mes candidatures
        </RouterLink>
      </header>

      <div v-if="applicationsStore.loading" class="state">Chargement…</div>
      <div v-else-if="applicationsStore.error" class="state text-red-600">
        {{ applicationsStore.error }}
      </div>
      <div v-else-if="!current" class="state">
        Candidature introuvable.
      </div>
      <article v-else class="bg-white border border-gray-200 rounded-xl p-6">
        <div class="flex items-start justify-between gap-4 mb-6">
          <div>
            <h1 class="text-2xl font-bold">{{ current.offreTitre }}</h1>
            <p class="text-sm text-gray-500 mt-1">
              {{ current.poste }} · {{ current.departement }}
            </p>
          </div>
          <ApplicationStatusBadge :statut="current.statut" />
        </div>

        <dl class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
          <div>
            <dt class="text-gray-500">Date de dépôt</dt>
            <dd class="font-medium mt-0.5">{{ formatDate(current.dateDepot) }}</dd>
          </div>
          <div>
            <dt class="text-gray-500">Statut</dt>
            <dd class="font-medium mt-0.5">{{ current.statut }}</dd>
          </div>
        </dl>

        <div class="mt-8">
          <RouterLink
            :to="{ name: 'job-detail', params: { id: current.offreId } }"
            class="btn btn-secondary"
          >
            Voir l'offre
          </RouterLink>
        </div>
      </article>
    </div>
  </RequireAuth>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import RequireAuth from '@/components/common/RequireAuth.vue'
import ApplicationStatusBadge from '@/components/applications/ApplicationStatusBadge.vue'
import { useApplicationsStore } from '@/stores/applications'

const props = defineProps({ id: { type: [String, Number], required: true } })
const applicationsStore = useApplicationsStore()

const current = computed(() => applicationsStore.current)

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  applicationsStore.fetchOne(props.id)
})

onUnmounted(() => {
  applicationsStore.resetCurrent()
})
</script>