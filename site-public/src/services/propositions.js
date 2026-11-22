import { requete } from './api'
import { versQueryString } from '@/utils/query'

const BASE_URL = '/public/propositions'

export const propositionsApi = {
  list: (idCandidat) =>
    requete(`${BASE_URL}${versQueryString({ idCandidat })}`),

  detail: (id, idCandidat) =>
    requete(`${BASE_URL}/${id}${versQueryString({ idCandidat })}`),

  envoyerMessage: (id, { idCandidat, contenu, salairePropose }) =>
    requete(`${BASE_URL}/${id}/messages`, {
      method: 'POST',
      body: JSON.stringify({ idCandidat, contenu, salairePropose }),
    }),

  repondre: (id, { idCandidat, reponse, remarque }) =>
    requete(`${BASE_URL}/${id}/repondre`, {
      method: 'POST',
      body: JSON.stringify({ idCandidat, reponse, remarque }),
    }),
}