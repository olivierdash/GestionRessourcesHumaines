<template>
  <div>
    <header class="text-center mb-8">
      <h1 class="text-2xl font-bold mb-2">Créer un compte</h1>
      <p class="text-sm text-gray-500">
        Quelques informations et vous pourrez postuler.
      </p>
    </header>

    <form class="flex flex-col gap-4" @submit.prevent="submit" novalidate>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div class="flex flex-col gap-2">
          <label for="prenom" class="text-sm font-medium">Prénom</label>
          <input
            id="prenom"
            v-model.trim="form.prenom"
            autocomplete="given-name"
            required
            class="px-3 py-2.5 border border-gray-200 rounded-lg bg-white text-sm
                   focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div class="flex flex-col gap-2">
          <label for="nom" class="text-sm font-medium">Nom</label>
          <input
            id="nom"
            v-model.trim="form.nom"
            autocomplete="family-name"
            required
            class="px-3 py-2.5 border border-gray-200 rounded-lg bg-white text-sm
                   focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="email" class="text-sm font-medium">Email</label>
        <input
          id="email"
          type="email"
          v-model.trim="form.email"
          autocomplete="email"
          required
          class="px-3 py-2.5 border border-gray-200 rounded-lg bg-white text-sm
                 focus:outline-none focus:ring-2 focus:ring-primary"
        />
      </div>

      <div class="flex flex-col gap-2">
        <label for="password" class="text-sm font-medium">Mot de passe</label>
        <input
          id="password"
          type="password"
          v-model="form.password"
          autocomplete="new-password"
          required
          minlength="8"
          class="px-3 py-2.5 border border-gray-200 rounded-lg bg-white text-sm
                 focus:outline-none focus:ring-2 focus:ring-primary"
        />
        <p class="text-xs text-gray-500">
          8 caractères minimum.
        </p>
      </div>

      <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

      <button type="submit" class="btn btn-primary w-full" :disabled="loading">
        {{ loading ? 'Création…' : 'Créer mon compte' }}
      </button>
    </form>

   <p class="text-sm text-center text-gray-500 mt-6">
  Déjà un compte ?
  <RouterLink
    :to="{ name: 'login' }"
    replace
    class="text-primary font-medium hover:underline"
  >
    Se connecter
  </RouterLink>
</p>
    <p class="text-sm text-center text-gray-500 mt-2">
      <RouterLink to="/" class="hover:underline">← Retour à l'accueil</RouterLink>
    </p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCandidateStore } from '@/stores/candidate'

const candidateStore = useCandidateStore()
const route = useRoute()
const router = useRouter()

const form = reactive({
  prenom: '',
  nom: '',
  email: '',
  password: '',
})
const loading = ref(false)
const error = ref(null)

function goAfterAuth() {
  const back = router.options.history.state?.back
  if (back && back.startsWith('/') && !back.startsWith('/auth/')) {
    router.back()
  } else {
    router.push('/mes-candidatures')
  }
}

async function submit() {
  error.value = null

  if (!form.prenom || !form.nom || !form.email || !form.password) {
    error.value = 'Merci de renseigner tous les champs obligatoires.'
    return
  }
  if (form.password.length < 8) {
    error.value = 'Le mot de passe doit contenir au moins 8 caractères.'
    return
  }

  loading.value = true
  try {
    await candidateStore.register({
      prenom: form.prenom,
      nom: form.nom,
      email: form.email,
      password: form.password,
    })
goAfterAuth()
  } catch (e) {
    error.value = e.message ?? "Échec de l'inscription."
  } finally {
    loading.value = false
  }
}
</script>