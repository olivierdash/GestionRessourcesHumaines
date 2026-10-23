--
-- PostgreSQL database dump
--

\restrict cb78em5iX1aZyivB0IYi5lSAPwwVHFvAPdcDGaD32AErGESwjEFmxytj3gTSdNS

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
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO roles (id_role, libelle) VALUES (1, 'MANAGER');
INSERT INTO roles (id_role, libelle) VALUES (2, 'RH');
INSERT INTO roles (id_role, libelle) VALUES (3, 'FINANCE');
INSERT INTO roles (id_role, libelle) VALUES (4, 'DG');


--
-- Data for Name: utilisateur; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO utilisateur (id_utilisateur, nom, email, mot_de_passe, id_role) VALUES (1, 'Alice RH', 'alice@sirh.com', '123', 2);
INSERT INTO utilisateur (id_utilisateur, nom, email, mot_de_passe, id_role) VALUES (2, 'Bob Manager', 'bob@sirh.com', '123', 1);
INSERT INTO utilisateur (id_utilisateur, nom, email, mot_de_passe, id_role) VALUES (3, 'Claire DG', 'claire@sirh.com', '123', 4);


--
-- Name: roles_id_role_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('roles_id_role_seq', 1, false);


--
-- Name: utilisateur_id_utilisateur_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('utilisateur_id_utilisateur_seq', 1, false);


--
-- PostgreSQL database dump complete
--

\unrestrict cb78em5iX1aZyivB0IYi5lSAPwwVHFvAPdcDGaD32AErGESwjEFmxytj3gTSdNS

