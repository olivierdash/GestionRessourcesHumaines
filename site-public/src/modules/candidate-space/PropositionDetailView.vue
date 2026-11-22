<template>
  <RequireAuth>
    <div class="container-app py-8 max-w-3xl">
      <header class="mb-6">
        <RouterLink
          :to="{ name: 'my-propositions' }"
          class="text-sm text-gray-500 hover:text-gray-900"
        >
          ← Retour à mes propositions
        </RouterLink>
      </header>

      <div v-if="propositionsStore.loading" class="state">Chargement…</div>
      <div v-else-if="propositionsStore.error" class="state text-red-600">
        {{ propositionsStore.error }}
      </div>
      <div v-else-if="!current" class="state">Proposition introuvable.</div>

      <template v-else>
        <article class="bg-white border border-gray-200 rounded-xl p-6 mb-6">
          <div class="flex items-start justify-between gap-4 mb-4">
            <div>
              <h1 class="text-2xl font-bold">{{ current.offreTitre }}</h1>
              <p class="text-sm text-gray-500 mt-1">
                {{ current.poste }} · {{ current.departement }}
              </p>
            </div>
            <PropositionStatusBadge :statut="current.statut" />
          </div>

          <dl class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
            <div>
              <dt class="text-gray-500">Salaire proposé</dt>
              <dd class="font-semibold mt-0.5">{{ formatEuro(current.salairePropose) }}</dd>
            </div>
            <div v-if="current.dateExpiration">
              <dt class="text-gray-500">Expire le</dt>
              <dd class="font-medium mt-0.5">{{ formatDate(current.dateExpiration) }}</dd>
            </div>
          </dl>

          <p v-if="current.remarque" class="mt-4 text-sm text-gray-700 bg-gray-50 p-3 rounded-lg">
            {{ current.remarque }}
          </p>
        </article>

        <section class="bg-white border border-gray-200 rounded-xl overflow-hidden mb-6">
          <header class="px-5 py-4 border-b border-gray-200 bg-gray-50 flex items-center justify-between">
            <h2 class="text-sm font-bold uppercase tracking-wider text-gray-900">
              Négociation
            </h2>
            <span class="text-xs text-gray-500">{{ messages.length }} message(s)</span>
          </header>

          <div class="p-5 flex flex-col gap-3 max-h-96 overflow-y-auto">
            <p v-if="messages.length === 0" class="text-sm text-gray-400 italic text-center py-4">
              Aucun échange pour le moment.
            </p>
            <div
              v-for="m in messages"
              :key="m.id"
              :class="m.deCandidat
                ? 'self-end bg-blue-50 border-blue-200'
                : 'self-start bg-gray-50 border-gray-200'"
              class="max-w-[80%] rounded-lg border p-3 flex flex-col gap-1"
            >
              <div class="flex items-baseline gap-2 text-xs">
                <strong class="text-gray-900">{{ m.auteur }}</strong>
                <span class="text-gray-400 font-mono">{{ formatDate(m.dateCreation) }}</span>
              </div>
              <div v-if="m.contenu" class="text-sm text-gray-700 whitespace-pre-line">
                {{ m.contenu }}
              </div>
              <div
                v-if="m.salairePropose"
                class="mt-1 inline-flex items-center gap-1 px-2 py-1
                       bg-amber-50 border border-amber-200 text-amber-800
                       text-xs font-bold rounded self-start"
              >
                💰 Contre-proposition : {{ formatEuro(m.salairePropose) }}
              </div>
            </div>
          </div>

          <div v-if="current.actionRequise" class="border-t border-gray-200 bg-gray-50 p-5">
            <form @submit.prevent="envoyerMessage" class="flex flex-col gap-3">
              <div>
                <label for="msg" class="block text-xs uppercase tracking-wider text-gray-500 font-bold mb-2">
                  Votre message
                </label>
                <textarea
                  id="msg"
                  v-model="msgForm.contenu"
                  rows="3"
                  placeholder="Ex : Je souhaiterais discuter du salaire…"
                  class="w-full p-3 border border-gray-200 rounded-lg text-sm bg-white
                         focus:outline-none focus:ring-2 focus:ring-primary"
                ></textarea>
              </div>
              <div class="flex items-center gap-3 flex-wrap">
                <div class="flex items-center gap-2">
                  <label for="msg-salaire" class="text-xs text-gray-600 font-semibold">
                    Contre-proposition (€) :
                  </label>
                  <input
                    id="msg-salaire"
                    type="number"
                    step="0.01"
                    min="0"
                    v-model.number="msgForm.salairePropose"
                    placeholder="Optionnel"
                    class="w-32 px-2 py-1.5 border border-gray-200 rounded text-sm bg-white
                           focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
                <button
                  type="submit"
                  class="ml-auto btn btn-primary"
                  :disabled="msgLoading"
                >
                  {{ msgLoading ? 'Envoi…' : 'Envoyer' }}
                </button>
              </div>
              <p v-if="msgError" class="text-sm text-red-600">{{ msgError }}</p>
            </form>
          </div>
        </section>

        <section
          v-if="current.actionRequise"
          class="bg-white border border-gray-200 rounded-xl p-6"
        >
          <h2 class="text-sm font-bold uppercase tracking-wider text-gray-900 mb-4">
            Répondre à la proposition
          </h2>
          <form @submit.prevent="repondre" class="flex flex-col gap-4">
            <div>
              <label for="reponse" class="block text-xs uppercase tracking-wider text-gray-500 font-bold mb-2">
                Votre réponse
              </label>
              <select
                id="reponse"
                v-model="reponseForm.reponse"
                class="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm bg-white
                       focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="ACCEPTER">Accepter la proposition</option>
                <option value="PATIENTER">Demander plus de temps (7 jours)</option>
                <option value="REFUSER">Refuser la proposition</option>
              </select>
            </div>
            <div>
              <label for="remarque" class="block text-xs uppercase tracking-wider text-gray-500 font-bold mb-2">
                Remarque (optionnel)
              </label>
              <textarea
                id="remarque"
                v-model="reponseForm.remarque"
                rows="2"
                class="w-full p-3 border border-gray-200 rounded-lg text-sm bg-white
                       focus:outline-none focus:ring-2 focus:ring-primary"
              ></textarea>
            </div>
            <div>
              <button
                type="submit"
                class="btn btn-primary"
                :disabled="reponseLoading"
              >
                {{ reponseLoading ? 'Envoi…' : 'Envoyer ma réponse' }}
              </button>
            </div>
            <p v-if="reponseError" class="text-sm text-red-600">{{ reponseError }}</p>
          </form>
        </section>

        <section
          v-if="!current.actionRequise"
          class="bg-gray-50 border border-gray-200 rounded-xl p-6 text-center"
        >
          <p class="text-sm text-gray-600">
            Cette proposition est clôturée.
          </p>
        </section>
      </template>
    </div>
  </RequireAuth>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import RequireAuth from '@/components/common/RequireAuth.vue'
import PropositionStatusBadge from '@/components/propositions/PropositionStatusBadge.vue'
import { usePropositionsStore } from '@/stores/propositions'

const props = defineProps({ id: { type: [String, Number], required: true } })
const propositionsStore = usePropositionsStore()

const current = computed(() => propositionsStore.current)
const messages = computed(() => current.value?.messages ?? [])

const msgForm = reactive({ contenu: '', salairePropose: null })
const msgLoading = ref(false)
const msgError = ref(null)

const reponseForm = reactive({ reponse: 'ACCEPTER', remarque: '' })
const reponseLoading = ref(false)
const reponseError = ref(null)

function formatEuro(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR',
    maximumFractionDigits: 0,
  }).format(v)
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function envoyerMessage() {
  msgError.value = null
  if (!msgForm.contenu?.trim() && !msgForm.salairePropose) {
    msgError.value = 'Saisissez un message ou une contre-proposition.'
    return
  }
  msgLoading.value = true
  try {
    await propositionsStore.envoyerMessage(props.id, {
      contenu: msgForm.contenu?.trim() || null,
      salairePropose: msgForm.salairePropose || null,
    })
    msgForm.contenu = ''
    msgForm.salairePropose = null
  } catch (e) {
    msgError.value = e.message ?? "Erreur lors de l'envoi."
  } finally {
    msgLoading.value = false
  }
}

async function repondre() {
  reponseError.value = null
  reponseLoading.value = true
  try {
    await propositionsStore.repondre(props.id, {
      reponse: reponseForm.reponse,
      remarque: reponseForm.remarque?.trim() || null,
    })
  } catch (e) {
    reponseError.value = e.message ?? "Erreur lors de l'envoi de la réponse."
  } finally {
    reponseLoading.value = false
  }
}

onMounted(() => {
  propositionsStore.fetchOne(props.id)
})

onUnmounted(() => {
  propositionsStore.resetCurrent()
})
</script>