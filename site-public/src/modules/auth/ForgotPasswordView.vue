<template>
  <div>
    <header class="text-center mb-8">
      <h1 class="text-2xl font-bold mb-2">Mot de passe oublié</h1>
      <p class="text-sm text-gray-500">
        Saisissez votre email et choisissez un nouveau mot de passe.
      </p>
    </header>

    <div
      v-if="success"
      class="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg"
    >
      <p class="text-sm text-green-700">Mot de passe modifié avec succès.</p>
      <RouterLink
        :to="{ name: 'login' }"
        class="text-sm text-green-800 font-medium hover:underline mt-2 inline-block"
      >
        Se connecter →
      </RouterLink>
    </div>

    <form
      v-if="!success"
      class="flex flex-col gap-4"
      @submit.prevent="submit"
      novalidate
    >
      <div class="flex flex-col gap-2">
        <label for="email" class="text-sm font-medium">Email</label>
        <input
          id="email"
          type="email"
          v-model.trim="form.email"
          autocomplete="email"
          required
          class="px-3 py-2.5 border rounded-lg bg-white text-sm
                 focus:outline-none focus:ring-2 focus:ring-primary"
          :class="errors.email ? 'border-red-500' : 'border-gray-200'"
          @input="clearError('email')"
        />
        <p v-if="errors.email" class="text-xs text-red-600">{{ errors.email }}</p>
      </div>

      <div class="flex flex-col gap-2">
        <label for="new" class="text-sm font-medium">Nouveau mot de passe</label>
        <input
          id="new"
          type="password"
          v-model="form.nouveauMotDePasse"
          autocomplete="new-password"
          required
          minlength="8"
          class="px-3 py-2.5 border rounded-lg bg-white text-sm
                 focus:outline-none focus:ring-2 focus:ring-primary"
          :class="errors.nouveauMotDePasse ? 'border-red-500' : 'border-gray-200'"
          @input="clearError('nouveauMotDePasse')"
        />
        <p class="text-xs text-gray-500">8 caractères minimum.</p>
        <p v-if="errors.nouveauMotDePasse" class="text-xs text-red-600">
          {{ errors.nouveauMotDePasse }}
        </p>
      </div>

      <div class="flex flex-col gap-2">
        <label for="confirm" class="text-sm font-medium">
          Confirmer le nouveau mot de passe
        </label>
        <input
          id="confirm"
          type="password"
          v-model="form.confirmationMotDePasse"
          autocomplete="new-password"
          required
          class="px-3 py-2.5 border rounded-lg bg-white text-sm
                 focus:outline-none focus:ring-2 focus:ring-primary"
          :class="errors.confirmationMotDePasse ? 'border-red-500' : 'border-gray-200'"
          @input="clearError('confirmationMotDePasse')"
        />
        <p v-if="errors.confirmationMotDePasse" class="text-xs text-red-600">
          {{ errors.confirmationMotDePasse }}
        </p>
      </div>

      <p v-if="globalError" class="text-sm text-red-600">{{ globalError }}</p>

      <button type="submit" class="btn btn-primary w-full" :disabled="loading">
        {{ loading ? 'Modification…' : 'Changer le mot de passe' }}
      </button>
    </form>

    <p class="text-sm text-center text-gray-500 mt-6">
      <RouterLink :to="{ name: 'login' }" class="text-primary font-medium hover:underline">
        ← Retour à la connexion
      </RouterLink>
    </p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { authApi } from '@/services/auth'

const MIN_PASSWORD_LENGTH = 8

const form = reactive({
  email: '',
  nouveauMotDePasse: '',
  confirmationMotDePasse: '',
})

const errors = ref({
  email: '',
  nouveauMotDePasse: '',
  confirmationMotDePasse: '',
})

const globalError = ref(null)
const success = ref(false)
const loading = ref(false)

function clearError(field) {
  if (errors.value[field]) errors.value[field] = ''
  if (globalError.value) globalError.value = null
}

function validate() {
  errors.value = { email: '', nouveauMotDePasse: '', confirmationMotDePasse: '' }
  let ok = true

  if (!form.email) {
    errors.value.email = "L'email est requis."
    ok = false
  }

  if (!form.nouveauMotDePasse) {
    errors.value.nouveauMotDePasse = 'Le nouveau mot de passe est requis.'
    ok = false
  } else if (form.nouveauMotDePasse.length < MIN_PASSWORD_LENGTH) {
    errors.value.nouveauMotDePasse =
      `Le mot de passe doit contenir au moins ${MIN_PASSWORD_LENGTH} caractères.`
    ok = false
  }

  if (!form.confirmationMotDePasse) {
    errors.value.confirmationMotDePasse = 'La confirmation est requise.'
    ok = false
  } else if (form.confirmationMotDePasse !== form.nouveauMotDePasse) {
    errors.value.confirmationMotDePasse = 'Les mots de passe ne correspondent pas.'
    ok = false
  }

  return ok
}

function applyBackendFieldErrors(details) {
  if (!details || typeof details !== 'object') return false
  let applied = false
  for (const field of ['email', 'nouveauMotDePasse', 'confirmationMotDePasse']) {
    if (details[field]) {
      errors.value[field] = details[field]
      applied = true
    }
  }
  return applied
}

async function submit() {
  globalError.value = null
  if (!validate()) return

  loading.value = true
  try {
    await authApi.resetPassword({
      email: form.email,
      nouveauMotDePasse: form.nouveauMotDePasse,
      confirmationMotDePasse: form.confirmationMotDePasse,
    })
    success.value = true
  } catch (e) {
    if (!applyBackendFieldErrors(e.details)) {
      globalError.value = e.message ?? 'Échec de la réinitialisation.'
    }
  } finally {
    loading.value = false
  }
}
</script>