<template>
  <component :is="layout" />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PublicLayout from '@/layouts/PublicLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'

const layouts = {
  public: PublicLayout,
  auth: AuthLayout,
}

const route = useRoute()
const layout = computed(() => layouts[route.meta.layout] ?? PublicLayout)
</script>