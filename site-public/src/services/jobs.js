import { requete } from './api'
import { versQueryString } from '@/utils/query'

const BASE_URL = '/public/jobs'

export const jobsApi = {
  list: (params = {}) => requete(`${BASE_URL}${versQueryString(params)}`),
  detail: (id) => requete(`${BASE_URL}/${id}`),
}