<template>
  <RequireAuth>
    <div class="container-app py-8 max-w-4xl">
      <h1 class="text-2xl font-bold mb-2">Mes demandes d'explication</h1>
      <p class="text-sm text-gray-500 mb-6">Consultez les démarches disciplinaires vous concernant.</p>

      <div v-if="loading" class="state">Chargement…</div>
      <div v-else-if="error" class="state text-red-600">{{ error }}</div>
      <div v-else-if="demandes.length === 0" class="state">Aucune demande d'explication.</div>

      <article v-for="d in demandes" :key="d.id" class="bg-white border border-gray-200 rounded-xl p-5 mb-4">
        <div class="flex items-start justify-between gap-4">
          <div>
            <h2 class="font-bold">Demande du {{ formatDate(d.dateEnvoi) }}</h2>
            <p class="text-sm text-gray-600 mt-2 whitespace-pre-line">{{ d.motif }}</p>
          </div>
          <span class="px-2 py-1 rounded bg-slate-100 text-xs font-bold">{{ d.statut }}</span>
        </div>
        <p class="text-xs text-gray-500 mt-3">
          Réponse attendue avant le {{ formatDate(d.dateLimiteReponse) }}
        </p>

        <div v-if="d.reponse" class="mt-4 p-3 bg-gray-50 border border-gray-200 rounded">
          <div class="text-xs text-gray-500">Votre réponse</div>
          <p class="text-sm mt-1 whitespace-pre-line">{{ d.reponse }}</p>
          <p v-if="d.typeSanction && d.typeSanction !== 'AUCUNE'" class="text-sm font-bold mt-2">
            Décision : {{ d.typeSanction }}
          </p>
          <p v-if="d.commentaireDecision" class="text-sm mt-1">{{ d.commentaireDecision }}</p>
        </div>

        <form v-else-if="d.statut === 'EXPLICATION_EN_COURS'" @submit.prevent="repondre(d)" class="mt-4 flex flex-col gap-3">
          <textarea v-model="reponses[d.id]" required rows="4" maxlength="500"
                    class="w-full border border-gray-200 rounded p-3 text-sm"
                    placeholder="Votre réponse à cette demande d'explication"></textarea>
          <button type="submit" class="btn btn-primary self-end" :disabled="envoi[d.id]">
            {{ envoi[d.id] ? 'Envoi…' : 'Répondre' }}
          </button>
        </form>
      </article>
    </div>
  </RequireAuth>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import RequireAuth from '@/components/common/RequireAuth.vue'
import { disciplineApi } from '@/services/discipline'
import { useCandidateStore } from '@/stores/candidate'

const candidateStore = useCandidateStore()
const demandes = ref([])
const loading = ref(true)
const error = ref(null)
const reponses = reactive({})
const envoi = reactive({})

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('fr-FR').format(new Date(value))
}

async function charger() {
  try {
    demandes.value = await disciplineApi.mesDemandes(candidateStore.candidate.id)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function repondre(d) {
  envoi[d.id] = true
  try {
    await disciplineApi.repondre(d.id, candidateStore.candidate.id, reponses[d.id])
    await charger()
  } catch (e) {
    error.value = e.message
  } finally {
    envoi[d.id] = false
  }
}

onMounted(charger)
</script>
