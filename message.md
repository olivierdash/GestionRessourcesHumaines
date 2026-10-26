

**Objet : Points de réflexion et suggestions sur le modèle de données du SIRH**

Bonjour à tous,

Je vous partage quelques remarques et propositions concernant le modèle de données. Il s’agit de points à titre informatif et suggestif, à discuter ensemble, et non de décisions arrêtées.

**Base de données**
- L’utilisateur sert d’identifiant de connexion à l’application.

**Table OffreEmploi**
- Proposition : ajouter le champ `Description`.

**Table Poste**
- Proposition : retirer le champ `lieu`.

**Table Departement**
- Question : pourquoi `id_manager` pointe-t-il vers `utilisateur` et non vers `employe` ?

**Table etat_demande**
- Prévoir l’insertion des données de référence.

**Table type_contrat**
- Proposition : ajouter le champ `dureeMoisRenouvellement`.

**Table affectation**
- Proposition : supprimer cette table ou ne pas l’utiliser dans le périmètre actuel, sauf besoin métier identifié.

**Table Poste_mission**
- Question : quel est son rôle exact ? Pourquoi ne pas simplement mettre `missionDescription` dans la table `poste` ?

**Table demande_recrutement**
- Proposition : cette table devrait pointer vers `employe` et non vers `utilisateur`.

**Table candidature**
- Proposition : retirer `id_demande_recrutement`, car `offre.id_demande_recrutement` peut être différent de celui de la candidature.

**Table notification**
- Question : est-elle réellement nécessaire ? L’utilisation de services de messagerie en ligne pourrait peut-être suffire.

**Table grille_salarial**
- Question : pourquoi prévoir le champ `salaire_moyen` ? Quel est son usage métier ?

**Tables onboarding / onboarding_tache**
- Remarque : ce modèle ressemble à une TodoList avec un responsable et un manager. Il serait utile de confirmer le besoin fonctionnel et le rôle de chacun.

Merci de me faire vos retours sur ces points, avec votre avis ou une justification si certains choix actuels sont maintenus.

Bien cordialement,  