<template>
  <div>
    <header class="text-center mb-8">
      <h1 class="text-2xl font-bold mb-2">Connexion</h1>
      <p class="text-sm text-gray-500">
        Accédez à votre espace candidat.
      </p>
    </header>

    <form class="flex flex-col gap-4" @submit.prevent="submit" novalidate>
      <div class="flex flex-col gap-2">
        <label for="email" class="text-sm font-medium">Email</label>
        <input
          id="email"
          type="email"
          v-model.trim="form.email"
          autocomplete="email"
          required
          :aria-invalid="!!errors.email"
          :aria-describedby="errors.email ? 'email-error' : undefined"
          class="px-3 py-2.5 border rounded-lg bg-white text-sm
                 focus:outline-none focus:ring-2 focus:ring-primary"
          :class="errors.email ? 'border-red-500' : 'border-gray-200'"
          @input="clearError('email')"
        />
        <p v-if="errors.email" id="email-error" class="text-xs text-red-600">
          {{ errors.email }}
        </p>
      </div>

     <div class="flex flex-col gap-2">
  <label for="password" class="text-sm font-medium">Mot de passe</label>
  <input
    id="password"
    type="password"
    v-model="form.password"
    autocomplete="current-password"
    required
    :aria-invalid="!!errors.password"
    :aria-describedby="errors.password ? 'password-error' : undefined"
    class="px-3 py-2.5 border rounded-lg bg-white text-sm
           focus:outline-none focus:ring-2 focus:ring-primary"
    :class="errors.password ? 'border-red-500' : 'border-gray-200'"
    @input="clearError('password')"
  />
  <p v-if="errors.password" id="password-error" class="text-xs text-red-600">
    {{ errors.password }}
  </p>
  <RouterLink
    :to="{ name: 'forgot-password' }"
    class="text-xs text-primary font-medium hover:underline self-end"
  >
    Mot de passe oublié ?
  </RouterLink>
</div>

      <p v-if="globalError" class="text-sm text-red-600">{{ globalError }}</p>

      <button type="submit" class="btn btn-primary w-full" :disabled="loading">
        {{ loading ? 'Connexion…' : 'Se connecter' }}
      </button>
    </form>

    <p class="text-sm text-center text-gray-500 mt-6">
      Pas encore de compte ?
      <RouterLink
        :to="{ name: 'register' }"
        replace
        class="text-primary font-medium hover:underline"
      >
        Créer un compte
      </RouterLink>
    </p>

    <p class="text-sm text-center text-gray-500 mt-2">
      <RouterLink to="/" class="hover:underline">← Retour à l'accueil</RouterLink>
    </p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCandidateStore } from '@/stores/candidate'

const candidateStore = useCandidateStore()
const router = useRouter()

const form = reactive({ email: '', password: '' })
const errors = ref({ email: '', password: '' })
const globalError = ref(null)
const loading = ref(false)

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function clearError(field) {
  if (errors.value[field]) errors.value[field] = ''
  if (globalError.value) globalError.value = null
}

function validate() {
  errors.value = { email: '', password: '' }
  let ok = true
  if (!form.email) {
    errors.value.email = "L'email est requis."
    ok = false
  } else if (!EMAIL_REGEX.test(form.email)) {
    errors.value.email = "Format d'email invalide."
    ok = false
  }
  if (!form.password) {
    errors.value.password = 'Le mot de passe est requis.'
    ok = false
  }
  return ok
}

/**
 * Revient à la page précédente si elle est interne et hors /auth/*.
 * Sinon, fallback vers /mes-candidatures.
 */
function goAfterAuth() {
  const back = router.options.history.state?.back
  if (back && back.startsWith('/') && !back.startsWith('/auth/')) {
    router.back()
  } else {
    router.push('/mes-candidatures')
  }
}

function applyBackendFieldErrors(details) {
  if (!details || typeof details !== 'object') return false
  let applied = false
  if (details.email) { errors.value.email = details.email; applied = true }
  if (details.password) { errors.value.password = details.password; applied = true }
  return applied
}

async function submit() {
  globalError.value = null
  if (!validate()) return

  loading.value = true
  try {
    await candidateStore.login(form)
    goAfterAuth()
  } catch (e) {
    if (!applyBackendFieldErrors(e.details)) {
      globalError.value = e.message ?? 'Échec de la connexion.'
    }
  } finally {
    loading.value = false
  }
}
</script>