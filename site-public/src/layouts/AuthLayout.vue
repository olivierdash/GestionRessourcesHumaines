<template>
  <div class="auth-shell">
    <div class="auth-card">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.auth-shell {
  display: flex; align-items: center; justify-content: center;
  min-height: 100vh; background: #f5f7fa;
}
.auth-card {
  width: 100%; max-width: 420px; padding: 2rem;
  background: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}
</style>