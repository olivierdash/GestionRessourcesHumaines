import { requete } from './api'
import { versQueryString } from '@/utils/query'

const BASE_URL = '/public/discipline'

export const disciplineApi = {
  mesDemandes: (idCandidat) =>
    requete(`${BASE_URL}/mes-demandes${versQueryString({ idCandidat })}`),
  repondre: (id, idCandidat, contenu) =>
    requete(`${BASE_URL}/demandes/${id}/reponse`, {
      method: 'POST',
      body: JSON.stringify({ idCandidat, contenu }),
    }),
}
