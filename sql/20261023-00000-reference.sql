--
-- PostgreSQL database dump
--

\restrict zEhiarWPOh10PhfO0HuPu8Tcpyd8vZysNejifHrWORI7QcS5Hnw78NkBrEWVe0J

-- Dumped from database version 16.15 (Ubuntu 16.15-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.15 (Ubuntu 16.15-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: etat_demande; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: statut_candidature; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.statut_candidature (id_statut_candidature, libelle) VALUES (1, 'RECU');
INSERT INTO public.statut_candidature (id_statut_candidature, libelle) VALUES (2, 'EN_EVALUATION');
INSERT INTO public.statut_candidature (id_statut_candidature, libelle) VALUES (3, 'RETENU');
INSERT INTO public.statut_candidature (id_statut_candidature, libelle) VALUES (4, 'REFUSE');


--
-- Data for Name: statut_demande_recrutement; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.statut_demande_recrutement (id_statut_demande_recrutement, libelle) VALUES (1, 'BROUILLON');
INSERT INTO public.statut_demande_recrutement (id_statut_demande_recrutement, libelle) VALUES (2, 'RECRUTEMENT_OUVERT');
INSERT INTO public.statut_demande_recrutement (id_statut_demande_recrutement, libelle) VALUES (3, 'CLOTUREE');


--
-- Data for Name: statut_offre_emploie; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.statut_offre_emploie (id_statut_offre_emploie, libelle) VALUES (1, 'BROUILLON');
INSERT INTO public.statut_offre_emploie (id_statut_offre_emploie, libelle) VALUES (2, 'PUBLIEE');
INSERT INTO public.statut_offre_emploie (id_statut_offre_emploie, libelle) VALUES (3, 'CLOTUREE');


--
-- Data for Name: statut_proposition; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.statut_proposition (id_statut_proposition, libelle) VALUES (1, 'EN_ATTENTE');
INSERT INTO public.statut_proposition (id_statut_proposition, libelle) VALUES (2, 'ACCEPTE');
INSERT INTO public.statut_proposition (id_statut_proposition, libelle) VALUES (3, 'REFUSE');


--
-- Data for Name: type_contrat; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.type_contrat (id_type_contrat, libelle) VALUES (1, 'CDI');
INSERT INTO public.type_contrat (id_type_contrat, libelle) VALUES (2, 'CDD');
INSERT INTO public.type_contrat (id_type_contrat, libelle) VALUES (3, 'CE');
INSERT INTO public.type_contrat (id_type_contrat, libelle) VALUES (4, 'STAGE');


--
-- Data for Name: type_evaluation; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.type_evaluation (id_type_evaluation, libelle) VALUES (1, 'TEST_TECHNIQUE');
INSERT INTO public.type_evaluation (id_type_evaluation, libelle) VALUES (2, 'ENTRETIEN_RH');
INSERT INTO public.type_evaluation (id_type_evaluation, libelle) VALUES (3, 'ENTRETIEN_MANAGER');


--
-- Data for Name: type_notification; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.type_notification (id_type_notification, libelle) VALUES (1, 'INFO');
INSERT INTO public.type_notification (id_type_notification, libelle) VALUES (2, 'ALERTE');


--
-- Name: etat_demande_id_etat_demande_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.etat_demande_id_etat_demande_seq', 1, false);


--
-- Name: statut_candidature_id_statut_candidature_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.statut_candidature_id_statut_candidature_seq', 1, false);


--
-- Name: statut_demande_recrutement_id_statut_demande_recrutement_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.statut_demande_recrutement_id_statut_demande_recrutement_seq', 1, false);


--
-- Name: statut_offre_emploie_id_statut_offre_emploie_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.statut_offre_emploie_id_statut_offre_emploie_seq', 1, false);


--
-- Name: statut_proposition_id_statut_proposition_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.statut_proposition_id_statut_proposition_seq', 1, false);


--
-- Name: type_contrat_id_type_contrat_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.type_contrat_id_type_contrat_seq', 1, false);


--
-- Name: type_evaluation_id_type_evaluation_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.type_evaluation_id_type_evaluation_seq', 1, false);


--
-- Name: type_notification_id_type_notification_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.type_notification_id_type_notification_seq', 1, false);


--
-- PostgreSQL database dump complete
--

\unrestrict zEhiarWPOh10PhfO0HuPu8Tcpyd8vZysNejifHrWORI7QcS5Hnw78NkBrEWVe0J

