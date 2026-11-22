import { requete } from './api'
import { versQueryString } from '@/utils/query'

const BASE_URL = '/public'

export const applicationsApi = {
  /**
   * Postuler à une offre. Envoie un FormData avec idCandidat + CV.
   * @param {number} jobId
   * @param {{ idCandidat: number, cv: File }} payload
   */
  create: (jobId, { idCandidat, cv }) => {
    const form = new FormData()
    form.append('idCandidat', idCandidat)
    form.append('cv', cv)
    return requete(`${BASE_URL}/jobs/${jobId}/apply`, {
      method: 'POST',
      body: form,
    })
  },

  listMine: (idCandidat) =>
    requete(`${BASE_URL}/applications${versQueryString({ idCandidat })}`),

  detail: (id, idCandidat) =>
    requete(`${BASE_URL}/applications/${id}${versQueryString({ idCandidat })}`),
}