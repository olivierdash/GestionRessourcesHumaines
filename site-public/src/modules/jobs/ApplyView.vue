<template>
  <RequireAuth>
    <div class="container-app py-8 max-w-2xl">
      <header class="mb-6">
        <RouterLink
          :to="{ name: 'job-detail', params: { id: id } }"
          class="text-sm text-gray-500 hover:text-gray-900"
        >
          ← Retour à l'offre
        </RouterLink>
        <h1 class="text-2xl font-bold mt-2">
          Postuler{{ jobsStore.current ? ' · ' + jobsStore.current.titre : '' }}
        </h1>
      </header>

      <div class="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-6">
        <p class="text-sm text-gray-700">
          Vous postulez en tant que
          <strong>{{ candidateStore.fullName }}</strong>
          <span class="text-gray-500">({{ candidateStore.candidate?.email }})</span>.
        </p>
      </div>

      <form class="flex flex-col gap-4" @submit.prevent="submit" novalidate>
        <div class="flex flex-col gap-2">
          <label for="cv" class="text-sm font-medium">CV (PDF, DOC, DOCX) *</label>
          <input
            id="cv"
            type="file"
            accept=".pdf,.doc,.docx"
            required
            class="text-sm file:mr-3 file:px-4 file:py-2 file:rounded-lg file:border-0
                   file:bg-primary file:text-white file:cursor-pointer
                   file:hover:bg-primary-hover"
            @change="onFile"
          />
          <p v-if="fileName" class="text-xs text-gray-500">{{ fileName }}</p>
        </div>

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
        <p v-if="success" class="text-sm text-green-600">
          Candidature envoyée. Merci !
        </p>

        <div>
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? 'Envoi…' : 'Envoyer ma candidature' }}
          </button>
        </div>
      </form>
    </div>
  </RequireAuth>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import RequireAuth from '@/components/common/RequireAuth.vue'
import { useJobsStore } from '@/stores/jobs'
import { useApplicationsStore } from '@/stores/applications'
import { useCandidateStore } from '@/stores/candidate'

const props = defineProps({ id: { type: [String, Number], required: true } })

const jobsStore = useJobsStore()
const applicationsStore = useApplicationsStore()
const candidateStore = useCandidateStore()
const router = useRouter()

const cvFile = ref(null)
const fileName = computed(() => cvFile.value?.name ?? '')
const submitting = ref(false)
const error = ref(null)
const success = ref(false)

function onFile(e) {
  cvFile.value = e.target.files?.[0] ?? null
}

async function submit() {
  error.value = null
  success.value = false

  if (!cvFile.value) {
    error.value = 'Merci de joindre votre CV.'
    return
  }

  submitting.value = true
  try {
    await applicationsStore.apply(props.id, { cv: cvFile.value })
    success.value = true
    setTimeout(() => router.push({ name: 'my-applications' }), 1500)
  } catch (e) {
    error.value = e.message ?? "Erreur lors de l'envoi de la candidature."
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (!jobsStore.current || jobsStore.current.id != props.id) {
    jobsStore.fetchJob(props.id)
  }
})
</script>