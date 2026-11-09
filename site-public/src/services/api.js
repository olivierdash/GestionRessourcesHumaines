// src/services/api.js
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

export async function requete(chemin, options = {}) {
  const { headers = {}, ...rest } = options

  const finalHeaders = { ...headers }
  const isFormData = options.body instanceof FormData

  // Si ce n'est pas un FormData, on pose Content-Type: application/json par défaut.
  // Si c'est un FormData, on NE MET PAS de Content-Type :
  // le navigateur ajoute multipart/form-data avec son boundary automatiquement.
  if (!isFormData && !finalHeaders['Content-Type']) {
    finalHeaders['Content-Type'] = 'application/json'
  }

  const reponse = await fetch(BASE_URL + chemin, {
    credentials: 'include', // envoie le cookie de session (JSESSIONID) au backend
    headers: finalHeaders,
    ...rest,
  })

  const donnees = reponse.status === 204 ? null : await reponse.json().catch(() => null)

  if (!reponse.ok) {
    const erreur = new Error(donnees?.message ?? `Erreur HTTP ${reponse.status}`)
    erreur.statut = reponse.status
    erreur.details = donnees?.erreurs ?? {}
    throw erreur
  }
  return donnees
}