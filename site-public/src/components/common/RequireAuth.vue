<template>
  <div v-if="!candidateStore.isAuthenticated" class="container-app py-16">
    <div class="max-w-md mx-auto text-center">
      <h2 class="text-2xl font-bold mb-2">Connexion requise</h2>
      <p class="text-gray-500 mb-6">
        Vous devez être connecté pour accéder à cette page.
      </p>
      <div class="flex gap-3 justify-center">
        <RouterLink :to="{ name: 'login' }" class="btn btn-primary">
          Se connecter
        </RouterLink>
        <RouterLink :to="{ name: 'register' }" class="btn btn-secondary">
          Créer un compte
        </RouterLink>
      </div>
    </div>
  </div>
  <slot v-else />
</template>

<script setup>
import { useCandidateStore } from '@/stores/candidate'

const candidateStore = useCandidateStore()
</script>