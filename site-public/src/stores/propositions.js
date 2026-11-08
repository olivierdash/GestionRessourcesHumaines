import { ref } from 'vue'
import { defineStore } from 'pinia'
import { propositionsApi } from '@/services/propositions'
import { useCandidateStore } from '@/stores/candidate'

export const usePropositionsStore = defineStore('propositions', () => {
  const mine = ref([])
  const current = ref(null)
  const loading = ref(false)
  const error = ref(null)

  function idCandidat() {
    const candidateStore = useCandidateStore()
    return candidateStore.candidate?.id ?? null
  }

  async function fetchMine() {
    const id = idCandidat()
    if (!id) {
      mine.value = []
      return
    }
    loading.value = true
    error.value = null
    try {
      mine.value = await propositionsApi.list(id)
    } catch (e) {
      error.value = e.message ?? 'Erreur lors du chargement des propositions.'
      mine.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchOne(id) {
    const idCand = idCandidat()
    if (!idCand) {
      current.value = null
      return
    }
    loading.value = true
    error.value = null
    try {
      current.value = await propositionsApi.detail(id, idCand)
    } catch (e) {
      error.value = e.message ?? 'Erreur lors du chargement de la proposition.'
      current.value = null
    } finally {
      loading.value = false
    }
  }

  async function envoyerMessage(id, { contenu, salairePropose }) {
    const idCand = idCandidat()
    if (!idCand) throw new Error('Connexion requise.')
    await propositionsApi.envoyerMessage(id, {
      idCandidat: idCand,
      contenu,
      salairePropose,
    })
    await fetchOne(id)
  }

  async function repondre(id, { reponse, remarque }) {
    const idCand = idCandidat()
    if (!idCand) throw new Error('Connexion requise.')
    const res = await propositionsApi.repondre(id, {
      idCandidat: idCand,
      reponse,
      remarque,
    })
    await fetchOne(id)
    return res.message
  }

  function resetCurrent() {
    current.value = null
  }

  return {
    mine,
    current,
    loading,
    error,
    fetchMine,
    fetchOne,
    envoyerMessage,
    repondre,
    resetCurrent,
  }
})