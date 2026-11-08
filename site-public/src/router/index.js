import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('@/modules/home/HomeView.vue'), meta: { layout: 'public' } },
  { path: '/offres', name: 'jobs', component: () => import('@/modules/jobs/JobListView.vue'), meta: { layout: 'public' } },
  { path: '/offres/:id', name: 'job-detail', component: () => import('@/modules/jobs/JobDetailView.vue'), props: true, meta: { layout: 'public' } },
  { path: '/offres/:id/postuler', name: 'job-apply', component: () => import('@/modules/jobs/ApplyView.vue'), props: true, meta: { layout: 'public' } },

  { path: '/auth/login', name: 'login', component: () => import('@/modules/auth/LoginView.vue'), meta: { layout: 'auth' } },
  { path: '/auth/register', name: 'register', component: () => import('@/modules/auth/RegisterView.vue'), meta: { layout: 'auth' } },
  { path: '/auth/forgot-password', name: 'forgot-password', component: () => import('@/modules/auth/ForgotPasswordView.vue'), meta: { layout: 'auth' } },
  {
    path: '/mes-candidatures',
    name: 'my-applications',
    component: () => import('@/modules/candidate-space/MyApplicationsView.vue'),
    meta: { layout: 'public', requiresCandidateAuth: true },
  },
  {
    path: '/mes-candidatures/:id',
    name: 'application-detail',
    component: () => import('@/modules/candidate-space/ApplicationDetailView.vue'),
    props: true,
    meta: { layout: 'public', requiresCandidateAuth: true },
  },

  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/modules/home/HomeView.vue'), meta: { layout: 'public' } },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

export default router