<template>
  <RequireAuth>
    <div class="container-app py-8">
      <header class="mb-6">
        <h1 class="text-2xl font-bold">Mes propositions</h1>
        <p class="text-gray-500 mt-1 text-sm">
          {{ propositionsStore.mine.length === 0
            ? 'Aucune proposition'
            : `${propositionsStore.mine.length} proposition${propositionsStore.mine.length > 1 ? 's' : ''}` }}
        </p>
      </header>

      <div v-if="propositionsStore.loading" class="state">Chargement…</div>
      <div v-else-if="propositionsStore.error" class="state text-red-600">
        {{ propositionsStore.error }}
      </div>
      <div v-else-if="propositionsStore.mine.length === 0" class="state">
        Vous n'avez reçu aucune proposition d'embauche pour l'instant.
        <div class="mt-4">
          <RouterLink :to="{ name: 'jobs' }" class="btn btn-primary">
            Voir les offres
          </RouterLink>
        </div>
      </div>
      <div v-else class="flex flex-col gap-3">
        <article
          v-for="p in propositionsStore.mine"
          :key="p.idProposition"
          class="flex items-start justify-between gap-4 p-5 bg-white border border-gray-200 rounded-xl
                 transition-all hover:border-primary hover:shadow-sm"
        >
          <div class="flex-1 min-w-0">
            <div class="flex items-start gap-3 flex-wrap">
              <h2 class="text-lg font-semibold truncate">{{ p.offreTitre }}</h2>
              <PropositionStatusBadge :statut="p.statut" />
              <span
                v-if="p.attendReponse"
                class="text-xs font-medium text-primary bg-blue-50 px-2.5 py-0.5 rounded-full whitespace-nowrap"
              >
                Réponse attendue
              </span>
            </div>
            <p class="text-sm text-gray-500 mt-1">
              {{ p.poste }} · {{ p.departement }}
            </p>
            <p class="text-sm text-gray-700 mt-2">
              Salaire proposé :
              <strong>{{ formatEuro(p.salaireFinal ?? p.salairePropose) }}</strong>
            </p>
          </div>
          <RouterLink
            :to="{ name: 'proposition-detail', params: { id: p.idProposition } }"
            class="text-primary font-medium whitespace-nowrap hover:underline"
          >
            Ouvrir →
          </RouterLink>
        </article>
      </div>
    </div>
  </RequireAuth>
</template>

<script setup>
import { onMounted } from 'vue'
import RequireAuth from '@/components/common/RequireAuth.vue'
import PropositionStatusBadge from '@/components/propositions/PropositionStatusBadge.vue'
import { usePropositionsStore } from '@/stores/propositions'

const propositionsStore = usePropositionsStore()

function formatEuro(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR',
    maximumFractionDigits: 0,
  }).format(v)
}

onMounted(() => {
  propositionsStore.fetchMine()
})
</script>