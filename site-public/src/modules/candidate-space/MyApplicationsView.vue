<template>
  <RequireAuth>
    <div class="container-app py-8">
      <header class="mb-6">
        <h1 class="text-2xl font-bold">Mes candidatures</h1>
        <p class="text-gray-500 mt-1 text-sm">
          {{ mine.length === 0 ? 'Aucune candidature' : `${mine.length} candidature${mine.length > 1 ? 's' : ''}` }}
        </p>
      </header>

      <div v-if="applicationsStore.loading" class="state">
        Chargement…
      </div>
      <div v-else-if="applicationsStore.error" class="state text-red-600">
        {{ applicationsStore.error }}
      </div>
      <div v-else-if="mine.length === 0" class="state">
        Vous n'avez encore postulé à aucune offre.
        <div class="mt-4">
          <RouterLink :to="{ name: 'jobs' }" class="btn btn-primary">
            Voir les offres
          </RouterLink>
        </div>
      </div>
      <div v-else class="flex flex-col gap-3">
        <article
          v-for="c in mine"
          :key="c.id"
          class="flex items-start justify-between gap-4 p-5 bg-white border border-gray-200 rounded-xl
                 transition-all hover:border-primary hover:shadow-sm"
        >
          <div class="flex-1 min-w-0">
            <div class="flex items-start gap-3">
              <h2 class="text-lg font-semibold truncate">{{ c.offreTitre }}</h2>
              <ApplicationStatusBadge :statut="c.statut" />
            </div>
            <p class="text-sm text-gray-500 mt-1">
              {{ c.poste }} · {{ c.departement }}
            </p>
            <p class="text-xs text-gray-400 mt-2">
              Déposée le {{ formatDate(c.dateDepot) }}
            </p>
          </div>
          <RouterLink
            :to="{ name: 'application-detail', params: { id: c.id } }"
            class="text-primary font-medium whitespace-nowrap hover:underline"
          >
            Détail →
          </RouterLink>
        </article>
      </div>
    </div>
  </RequireAuth>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import RequireAuth from '@/components/common/RequireAuth.vue'
import ApplicationStatusBadge from '@/components/applications/ApplicationStatusBadge.vue'
import { useApplicationsStore } from '@/stores/applications'

const applicationsStore = useApplicationsStore()

const mine = computed(() => applicationsStore.mine)

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

onMounted(() => {
  applicationsStore.fetchMine()
})
</script>