import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/services/auth'

const STORAGE_KEY = 'sirh.candidate'

function loadFromStorage() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function saveToStorage(data) {
  try {
    if (data) {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    } else {
      sessionStorage.removeItem(STORAGE_KEY)
    }
  } catch {
    // sessionStorage indisponible (mode privé strict, quota) : on ignore.
  }
}

export const useCandidateStore = defineStore('candidate', () => {
  const candidate = ref(loadFromStorage())

  const isAuthenticated = computed(() => !!candidate.value)

  const fullName = computed(() => {
    if (!candidate.value) return ''
    const prenom = candidate.value.prenom ?? ''
    const nom = candidate.value.nom ?? ''
    return `${prenom} ${nom}`.trim()
  })

  async function login(credentials) {
    const data = await authApi.login(credentials)
    const candidatPayload = data?.candidate ?? data
    candidate.value = candidatPayload
    saveToStorage(candidatPayload)
    return candidatPayload
  }

  async function register(payload) {
    const data = await authApi.register(payload)
    const candidatPayload = data?.candidate ?? data
    candidate.value = candidatPayload
    saveToStorage(candidatPayload)
    return candidatPayload
  }

  /**
   * Déconnexion purement client — aucun appel réseau.
   * L'API est stateless pour les candidats : le serveur ne retient
   * aucune session, il n'y a donc rien à invalider côté serveur.
   */
  function logout() {
    candidate.value = null
    saveToStorage(null)
  }

  return {
    candidate,
    isAuthenticated,
    fullName,
    login,
    register,
    logout,
  }
})