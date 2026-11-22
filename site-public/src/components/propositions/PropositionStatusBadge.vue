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
  EN_ATTENTE: { label: 'En préparation', class: 'bg-gray-100 text-gray-700' },
  SOUMISE:    { label: 'En validation',  class: 'bg-amber-50 text-amber-700' },
  APPROUVEE:  { label: 'À accepter',     class: 'bg-blue-50 text-blue-700' },
  ACCEPTE:    { label: 'Acceptée',       class: 'bg-green-50 text-green-700' },
  REFUSE:     { label: 'Refusée',        class: 'bg-red-50 text-red-700' },
  EXPIREE:    { label: 'Expirée',        class: 'bg-gray-100 text-gray-500' },
}

const label = computed(() => MAP[props.statut]?.label ?? props.statut)
const badgeClass = computed(() => MAP[props.statut]?.class ?? 'bg-gray-100 text-gray-700')
</script>