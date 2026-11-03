<template>
  <div>
    <!-- Hero -->
    <section class="bg-gradient-to-br from-blue-50 via-white to-blue-50">
      <div class="container-app py-20 md:py-28 text-center">
        <span class="inline-block text-xs font-semibold uppercase tracking-wider text-primary bg-blue-100 px-3 py-1 rounded-full mb-6">
          Rejoignez-nous
        </span>
        <h1 class="text-4xl md:text-5xl font-bold tracking-tight text-gray-900 mb-4">
          Construisez votre carrière<br class="hidden md:block" />
          avec nous
        </h1>
        <p class="text-lg text-gray-600 max-w-2xl mx-auto mb-8">
          Découvrez nos offres d'emploi et postulez en quelques minutes.
          Nous recherchons des talents passionnés pour construire ensemble.
        </p>
        <div class="flex flex-wrap justify-center gap-3">
          <RouterLink to="/offres" class="btn btn-primary text-base px-6 py-3">
            Voir les offres
          </RouterLink>
          <RouterLink to="/auth/register" class="btn btn-secondary text-base px-6 py-3">
            Créer un compte
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- Avantages -->
    <section class="py-16 md:py-20">
      <div class="container-app">
        <header class="text-center mb-12">
          <h2 class="text-2xl md:text-3xl font-bold mb-2">Pourquoi nous rejoindre ?</h2>
          <p class="text-gray-500">
            Un environnement stimulant, des projets ambitieux, une équipe bienveillante.
          </p>
        </header>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <article
            v-for="atout in atouts"
            :key="atout.titre"
            class="p-6 bg-white border border-gray-200 rounded-xl transition-shadow hover:shadow-md"
          >
            <div class="text-3xl mb-3" aria-hidden="true">{{ atout.icone }}</div>
            <h3 class="text-lg font-semibold mb-1">{{ atout.titre }}</h3>
            <p class="text-sm text-gray-600">{{ atout.description }}</p>
          </article>
        </div>
      </div>
    </section>

    <!-- CTA final -->
    <section class="bg-primary">
      <div class="container-app py-14 md:py-16 text-center">
        <h2 class="text-2xl md:text-3xl font-bold text-white mb-3">
          Prêt à franchir le pas ?
        </h2>
        <p class="text-blue-100 mb-8 max-w-xl mx-auto">
          Parcourez nos offres et trouvez le poste qui vous correspond.
        </p>
        <RouterLink
          to="/offres"
          class="inline-flex items-center justify-center px-6 py-3 rounded-lg
                 bg-white text-primary font-medium hover:bg-blue-50 transition-colors"
        >
          Explorer les offres →
        </RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup>
const atouts = [
  {
    icone: '🚀',
    titre: 'Des projets ambitieux',
    description:
      'Rejoignez une équipe qui construit des produits à impact, avec des défis techniques et métier stimulants.',
  },
  {
    icone: '🌱',
    titre: 'Évolution continue',
    description:
      'Formations, mentorat, mobilité interne : nous investissons dans votre développement professionnel.',
  },
  {
    icone: '⚖️',
    titre: 'Équilibre de vie',
    description:
      'Télétravail, horaires flexibles, congés généreux : nous respectons votre temps et vos priorités.',
  },
]
</script>