package com.example.sirh.constants;

public final class ConstantSirh {

    private ConstantSirh() {
    }

    public static final class Statut {
        public static final String A_FAIRE = "à faire";
        public static final String ACCEPTE = "ACCEPTE";
        public static final String ACTIF = "actif";
        public static final String ACTIF_EMPLOYE = "ACTIF";
        public static final String APPROUVEE = "APPROUVEE";
        public static final String BROUILLON = "BROUILLON";
        public static final String BROUILLON_DEMANDE = "Brouillon";
        public static final String CLOTUREE = "CLOTUREE";
        public static final String CREE = "Cree";
        public static final String EMBAUCHE = "EMBAUCHE";
        public static final String EN_ATTENTE = "EN_ATTENTE";
        public static final String EN_COURS = "en cours";
        public static final String EN_EVALUATION = "EN_EVALUATION";
        public static final String EN_ONBOARDING = "EN_ONBOARDING";
        public static final String EXPIREE = "EXPIREE";
        public static final String INACTIF = "INACTIF";
        public static final String PUBLIEE = "PUBLIEE";
        public static final String RECU = "RECU";
        public static final String REFUSE = "REFUSE";
        public static final String REFUSE_DEMANDE = "Refuse";
        public static final String RETENU = "RETENU";
        public static final String SOUMISE = "SOUMISE";
        public static final String TERMINE = "terminé";
        public static final String TERMINEE = "terminée";
        public static final String VALIDE = "Valide";
        public static final String VALIDE_FINANCE = "ValideFinance";
        public static final String VALIDE_RH = "ValideRH";
    }

    public static final class Role {
        public static final String ADMIN = "ADMIN";
        public static final String DG = "DG";
        public static final String FINANCE = "FINANCE";
        public static final String MANAGER = "MANAGER";
        public static final String RESPONSABLE_IT = "RESPONSABLE_IT";
        public static final String RH = "RH";
    }

    public static final class Action {
        public static final String ACCEPTER = "ACCEPTER";
        public static final String APPROUVER = "APPROUVER";
        public static final String PATIENTER = "PATIENTER";
        public static final String REFUSER = "REFUSER";
        public static final String SAVE = "save";
        public static final String SUBMIT = "submit";
    }

    public static final class Decision {
        public static final String APPROUVEE = "APPROUVEE";
        public static final String REFUSEE = "REFUSEE";
    }

    public static final class TypeDecompteCode {
        public static final String HEURES = "HEURES";
        public static final String JOURS = "JOURS";
    }
}