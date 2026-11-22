import { ref } from 'vue'
import { defineStore } from 'pinia'
import { applicationsApi } from '@/services/applications'
import { useCandidateStore } from '@/stores/candidate'

export const useApplicationsStore = defineStore('applications', () => {
  const mine = ref([])
  const current = ref(null)
  const loading = ref(false)
  const error = ref(null)

  /** ID du candidat authentifié, ou null (le backend refusera alors la requête). */
  function idCandidat() {
    const candidateStore = useCandidateStore()
    return candidateStore.candidate?.id ?? null
  }

  async function apply(jobId, { cv }) {
    const id = idCandidat()
    if (!id) throw new Error('Connexion requise.')
    loading.value = true
    error.value = null
    try {
      return await applicationsApi.create(jobId, { idCandidat: id, cv })
    } catch (e) {
      error.value = e.message ?? "Erreur lors de l'envoi de la candidature."
      throw e
    } finally {
      loading.value = false
    }
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
      mine.value = await applicationsApi.listMine(id)
    } catch (e) {
      error.value = e.message ?? 'Erreur lors du chargement des candidatures.'
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
      current.value = await applicationsApi.detail(id, idCand)
    } catch (e) {
      error.value = e.message ?? 'Erreur lors du chargement de la candidature.'
      current.value = null
    } finally {
      loading.value = false
    }
  }

  function resetCurrent() {
    current.value = null
  }

  return {
    mine,
    current,
    loading,
    error,
    apply,
    fetchMine,
    fetchOne,
    resetCurrent,
  }
})