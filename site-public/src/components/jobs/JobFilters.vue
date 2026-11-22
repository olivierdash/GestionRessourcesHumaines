<template>
  <div class="flex flex-wrap gap-3 items-center p-4 bg-gray-50 border border-gray-200 rounded-xl mb-6">
    <div class="flex-1 min-w-[280px]">
      <label for="q" class="sr-only">Recherche</label>
      <input
        id="q"
        type="search"
        :value="search"
        placeholder="Rechercher un poste, un lieu…"
        class="w-full px-3 py-2.5 border border-gray-200 rounded-lg bg-white text-sm
               focus:outline-none focus:ring-2 focus:ring-primary"
        @input="onSearch"
      />
    </div>

    <select
      :value="departement"
      class="min-w-[180px] px-3 py-2.5 border border-gray-200 rounded-lg bg-white text-sm
             focus:outline-none focus:ring-2 focus:ring-primary"
      @change="onDepartement"
    >
      <option value="">Tous les départements</option>
      <option v-for="d in departements" :key="d" :value="d">{{ d }}</option>
    </select>

    <button
      v-if="hasActiveFilters"
      type="button"
      class="btn btn-secondary"
      @click="reset"
    >
      Réinitialiser
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  search: { type: String, default: '' },
  departement: { type: String, default: '' },
  departements: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:search', 'update:departement', 'reset'])

const hasActiveFilters = computed(
  () => !!props.search || !!props.departement
)

function onSearch(e) { emit('update:search', e.target.value) }
function onDepartement(e) { emit('update:departement', e.target.value) }
function reset() {
  emit('update:search', '')
  emit('update:departement', '')
  emit('reset')
}
</script>