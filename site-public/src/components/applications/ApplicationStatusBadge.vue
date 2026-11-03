<template>
  <span :class="badgeClass" class="text-xs font-medium px-2.5 py-0.5 rounded-full whitespace-nowrap">
    {{ label }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  statut: { type: String, required: true },
})

const MAP = {
  RECU:          { label: 'Reçu',          class: 'bg-gray-100 text-gray-700' },
  EN_EVALUATION: { label: 'En évaluation', class: 'bg-blue-50 text-blue-700' },
  RETENU:        { label: 'Retenu',        class: 'bg-green-50 text-green-700' },
  REFUSE:        { label: 'Refusé',        class: 'bg-red-50 text-red-700' },
}

const label = computed(() => MAP[props.statut]?.label ?? props.statut)
const badgeClass = computed(() => MAP[props.statut]?.class ?? 'bg-gray-100 text-gray-700')
</script>