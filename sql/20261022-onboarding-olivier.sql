CREATE TABLE onboarding(
    id_onboarding SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL UNIQUE,
    date_debut DATE DEFAULT CURRENT_DATE,
    date_fin DATE,
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer)
);