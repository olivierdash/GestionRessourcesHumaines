<template>
  <article
    class="flex flex-col gap-3 p-6 bg-white border border-gray-200 rounded-xl
           transition-all hover:border-primary hover:shadow-md hover:-translate-y-px"
  >
    <header class="flex justify-between items-start gap-3">
      <h3 class="text-lg font-semibold">{{ job.titre }}</h3>
      <span
        v-if="job.departement"
        class="text-xs font-medium text-primary bg-blue-50 px-2.5 py-0.5 rounded-full whitespace-nowrap"
      >
        {{ job.departement }}
      </span>
    </header>

    <ul class="flex flex-wrap gap-4 text-sm text-gray-500 list-none p-0 m-0">
      <li v-if="job.intitulePoste">💼 {{ job.intitulePoste }}</li>
      <li v-if="job.lieu">📍 {{ job.lieu }}</li>
      <li v-if="job.datePublication">🗓 {{ formatDate(job.datePublication) }}</li>
    </ul>

    <p class="text-sm text-gray-500">{{ extrait }}</p>

    <footer class="mt-auto pt-2">
      <RouterLink
        :to="{ name: 'job-detail', params: { id: job.id } }"
        class="text-primary font-medium hover:underline"
      >
        Voir le détail →
      </RouterLink>
    </footer>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  job: { type: Object, required: true },
})

const extrait = computed(() => {
  const txt = props.job.description || ''
  return txt.length > 140 ? txt.slice(0, 140) + '…' : txt
})

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}
</script>