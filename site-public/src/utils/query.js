export function versQueryString(params = {}) {
  const query = new URLSearchParams()
  for (const [cle, valeur] of Object.entries(params)) {
    if (valeur !== undefined && valeur !== null && valeur !== '') {
      query.append(cle, valeur)
    }
  }
  const qs = query.toString()
  return qs ? `?${qs}` : ''
}