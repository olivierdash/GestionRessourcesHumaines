# CONCEPTION D'UN LOGICIEL DE GESTION DE RH
_Du recrutement à la rupture du contrat : concevoir un SIRH autour du cycle de vie du salarié._

**fond :**  
manifestation du besoin du personnel dans les différentes département du pouvoir 

``pattern ( flux / cycle ) du personnel`` : besoin , recrutement ,embauche(ouverture du contrat) , vie du salarié , carrière , rupture du contrat(retraite , décès , démission , licenciement)

**Objectif :**  
(analyse des besoins:)
- comprendre les processus RH avant de concevoir le logiciel
- transformer un process métier en rêgle de gestion 
- concevoir un SIRH couvrant le cycle de vie du salarié 
- évité une conception limité à de simple écrans CRUD
- faire évoluer un même cas pratique jusqu'à l'architecture et au MVP  
(fait un groupe de 5 personnes maximum : )

<br>

# BLOC 1 : expression du besoin en personnel 
Un manager exprime un besoin : création d'un remplacement d'un poste.  

``Informations`` : 
- poste  
- remplacement 
- effectif 
- motif
- budget : les donnés doivent être consultable dans les autres département  
- date souhaitée  

``Workflow`` : Manager -> RH -> finance(regard financier) -> direction général(validation) -> ouverture du recrutement  

``Objet métié`` : Département , poste, demande Recrutement , Validation  

``Point de conception`` : un POSTE existe indépendamment de l'EMPLOYER    

# BLOC 2 : processus de recrutement 
- Transformer une demande validée en offre d'emploie(en ligne ou autre )  
- publi l'offre -> reception de candidature
- présélection :
    1. sélection de dossier
    2. test technique 
    3. entretient   
        #### fun fact : _pour les grandes entreprise il y a plusieur niveau de présélection._
- Conservation des évaluation et de la décision finale  (traçabilité de tout les passages pour les applications de ce genre)
    1. où le stocker le cv
    2. qui a fait l'entretien ? 
    3. qui a fait quoi ? 

**Objet**: Offre d'emploie , Candidat , Candidature ,Entretien , évaluation Candidat


# BLOC 3 : séléction et embauche
le candidat retenu reçoit une proposition d'embauche (champs oui/nom, remarque , acceptation finale(le bouton validé finale) ) 

    => pour la séléction faîte une éspèce de formulaire en ligne 
    - salaire oui/non/autre remarque(attendre le fait de reçu , refusé )  
    - fait de même pour pouvoir accepté , faire patienté , refusé la proposition finale du formulaire en ligne  
    - un bouton validé pour accépté le candidat  

- après acceptation : création du dossier salarié et du matricule  
- Créer le contrat , le poste et l'affectation
- point de conception : candidat != employé
- une personne peut avoir plusieur candidature avant de devenir salariée .

``recherche sur internet ``:   
```
structure d'un contrat de travail ,  
il faut mettre les champs et l'enregistré  
permettre la génération de pdf
```

# BLOC 4: Contrat et onboarding 
Un salarié pet avoir plusieurs contrats successifs : CDD , renouvellement , CDI , ...  
**Dropdown de contrat CE par défaut :**  
`CE `: contrat d'essaie (pas plus de 6 mois renouvellable une fois : 1 ans donc )  
`CDD `: Contrat à Durée Indéterminé (2 ans renouvellable une fois) : rêgle métié . On peut se passé ce celui ci et passé au CDI  
`CDI `: Contrat à Durée Indéterminé  

- Ne pas écraser l'historique en stockant seulement employe_type_contrat.  
- Onboarding contra signé (état créer) , badge (état signé )n compte , matériel (état validé), formation , inscription paie(compte bancaire donc obligatoire ) :
    1. état de paie périodique de la société  
- Utilisé une checklist et un workflow d'intégration  

**Objets** : Contrat , Affectation ,Onboarding, Onboarding Tache, Document. 
  
<br>

( à suivre )
# BLOC 5: 