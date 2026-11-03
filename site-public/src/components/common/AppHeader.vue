<template>
  <header class="fixed top-0 inset-x-0 h-16 bg-white border-b border-gray-200 z-50">
    <div class="container-app h-full flex items-center gap-6">
      <RouterLink to="/" class="flex items-center gap-2 font-bold text-lg">
        <span class="text-primary">◆</span>
        <span>Carrières</span>
      </RouterLink>

      <nav class="flex items-center gap-6 ml-6 flex-1">
        <RouterLink
          to="/offres"
          class="py-2 text-gray-500 font-medium border-b-2 border-transparent
                 hover:text-gray-900 transition-colors"
          active-class="!text-gray-900 !border-primary"
        >
          Offres d'emploi
        </RouterLink>
        <RouterLink
          v-if="candidateStore.isAuthenticated"
          to="/mes-candidatures"
          class="py-2 text-gray-500 font-medium border-b-2 border-transparent
                 hover:text-gray-900 transition-colors"
          active-class="!text-gray-900 !border-primary"
        >
          Mes candidatures
        </RouterLink>
      </nav>

      <div class="flex items-center gap-3">
        <template v-if="candidateStore.isAuthenticated">
          <span class="text-sm text-gray-500">
            Bonjour, {{ candidateStore.fullName }}
          </span>
          <button class="btn btn-secondary" @click="logout">Se déconnecter</button>
        </template>
        <template v-else>
          <RouterLink to="/auth/login" class="btn btn-secondary">Se connecter</RouterLink>
          <RouterLink to="/auth/register" class="btn btn-primary">Créer un compte</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useCandidateStore } from '@/stores/candidate'

const candidateStore = useCandidateStore()
const router = useRouter()

function logout() {
  candidateStore.logout()
  router.push({ name: 'home' })
}
</script>