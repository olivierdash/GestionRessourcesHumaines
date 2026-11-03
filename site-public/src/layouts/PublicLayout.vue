<template>
  <div class="flex flex-col min-h-screen">
    <AppHeader />
    <main class="flex-1 pt-16">
      <RouterView />
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import AppHeader from '@/components/common/AppHeader.vue'
import AppFooter from '@/components/common/AppFooter.vue'
</script>