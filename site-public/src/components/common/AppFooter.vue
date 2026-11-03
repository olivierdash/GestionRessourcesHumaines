<template>
  <footer class="border-t border-gray-200 bg-gray-50 py-6 text-center text-sm text-gray-500">
    <div class="container-app">
      <p>© {{ year }} — Carrières. Tous droits réservés.</p>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>