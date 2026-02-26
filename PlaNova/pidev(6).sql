-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Feb 26, 2026 at 10:50 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pidev`
--

-- --------------------------------------------------------

--
-- Table structure for table `activite`
--

CREATE TABLE `activite` (
  `id_activite` int(11) NOT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `date_activite` date DEFAULT NULL,
  `heure_activite` time DEFAULT NULL,
  `lieu` varchar(100) DEFAULT NULL,
  `prix` decimal(10,2) DEFAULT NULL,
  `id_excursion` int(11) DEFAULT NULL,
  `id_destination` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `activite`
--

INSERT INTO `activite` (`id_activite`, `nom`, `description`, `date_activite`, `heure_activite`, `lieu`, `prix`, `id_excursion`, `id_destination`) VALUES
(1, 'Visite du port venetien', 'Promenade guidee autour du vieux port de Chania.', '2026-04-11', '09:00:00', 'Port de Chania', 25.00, 1, 1),
(2, 'Degustation de mezze', 'Atelier cuisine cretoise avec un chef local.', '2026-04-12', '12:00:00', 'Agora de Chania', 35.00, 1, 1),
(3, 'Balade en gondole', 'Tour romantique en gondole dans les canaux de Venise.', '2026-05-02', '10:00:00', 'Grand Canal Venise', 80.00, 2, 2),
(4, 'Visite Palais des Doges', 'Visite guidee du Palazzo Ducale.', '2026-05-03', '14:00:00', 'Place Saint-Marc', 30.00, 2, 2),
(5, 'Tour des Pyramides', 'Exploration des pyramides de Gizeh a cheval.', '2026-06-16', '07:00:00', 'Site de Gizeh', 90.00, 3, 3),
(6, 'Croisiere sur le Nil', 'Croisiere en felouque au coucher du soleil.', '2026-06-17', '17:00:00', 'Le Caire', 60.00, 3, 3),
(7, 'Randonnee cotiere Feroe', 'Trek sur les falaises avec vue sur Atlantique.', '2026-07-02', '08:00:00', 'Sorvagsvatan', 40.00, 4, 4),
(8, 'Village de pecheurs', 'Decouverte du mode de vie traditionnel feroien.', '2026-07-03', '14:00:00', 'Gjogv', 20.00, 4, 4),
(9, 'Marche cretois', 'Visite du marche central avec guide.', '2026-04-21', '10:00:00', 'Agora Heraklion', 15.00, 5, 5),
(10, 'Plongee en mer Egee', 'Session snorkeling dans les eaux cristallines.', '2026-04-22', '11:00:00', 'Plage de Vai', 55.00, 5, 5),
(11, 'Visite de la forteresse', 'Tour guide de la Fortezza ottomane de Rethymno.', '2026-05-16', '09:30:00', 'Fortezza Rethymno', 20.00, 6, 6),
(12, 'Cours de poterie', 'Atelier artisanat traditionnel avec un potier local.', '2026-05-17', '15:00:00', 'Atelier local', 40.00, 6, 6),
(13, 'Diner romantique plage', 'Diner 5 etoiles sous les etoiles au bord de la mer.', '2026-08-03', '19:00:00', 'Plage de Phuket', 150.00, 7, 7),
(14, 'Excursion iles Phi Phi', 'Journee en bateau vers les iles Phi Phi.', '2026-08-04', '08:00:00', 'Port de Phuket', 95.00, 7, 7),
(15, 'Randonnee Mont Blanc', 'Trek guide vers les glaciers du Mont Blanc.', '2026-07-16', '06:30:00', 'Chamonix', 45.00, 8, 8),
(16, 'Nuit sous les etoiles', 'Camping sauvage avec observation astronomique.', '2026-07-17', '20:00:00', 'Alpes francaises', 30.00, 8, 8);

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `id_admin` int(11) NOT NULL,
  `matricule` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`id_admin`, `matricule`) VALUES
(901, 'ADM00901'),
(902, 'ADM00902'),
(903, 'ADM00903'),
(904, 'ADM00904'),
(905, 'ADM00905');

-- --------------------------------------------------------

--
-- Table structure for table `billet`
--

CREATE TABLE `billet` (
  `id_billet` int(11) NOT NULL,
  `db` varchar(100) DEFAULT NULL,
  `idv` varchar(100) DEFAULT NULL,
  `num_place` varchar(20) DEFAULT NULL,
  `id_destination` int(11) DEFAULT NULL,
  `id_transport_pub` int(11) DEFAULT NULL,
  `id_transport_priv` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `billet`
--

INSERT INTO `billet` (`id_billet`, `db`, `idv`, `num_place`, `id_destination`, `id_transport_pub`, `id_transport_priv`) VALUES
(1, 'Paris CDG', 'Chania', 'A12', 1, 1, 1),
(2, 'Lyon', 'Venise', 'B7', 2, 3, 2),
(3, 'Marseille', 'Gizeh', 'C3', 3, 4, 3),
(4, 'Paris CDG', 'Torshavn', 'D15', 4, 6, 4),
(5, 'Nice', 'Heraklion', 'A2', 5, 7, 5),
(6, 'Bordeaux', 'Rethymno', 'B9', 6, 8, 6),
(7, 'Paris CDG', 'Phuket', 'C22', 7, 9, 7),
(8, 'Geneve', 'Chamonix', 'A5', 8, 10, 8),
(9, 'Toulouse', 'Chania', 'B3', 1, 2, 1),
(10, 'Bruxelles', 'Venise', 'D8', 2, 3, 2);

-- --------------------------------------------------------

--
-- Table structure for table `chambre`
--

CREATE TABLE `chambre` (
  `id_chambre` int(11) NOT NULL,
  `type_chambre` varchar(50) DEFAULT NULL,
  `capacite` int(11) DEFAULT NULL,
  `prix_chambre` decimal(10,2) DEFAULT NULL,
  `statut_chambre` varchar(20) DEFAULT NULL,
  `id_hotel` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `chambre`
--

INSERT INTO `chambre` (`id_chambre`, `type_chambre`, `capacite`, `prix_chambre`, `statut_chambre`, `id_hotel`) VALUES
(1, 'Simple', 1, 80.00, 'disponible', 1),
(2, 'Double', 2, 130.00, 'disponible', 1),
(3, 'Suite', 2, 250.00, 'reservee', 1),
(4, 'Simple', 1, 150.00, 'disponible', 2),
(5, 'Double', 2, 220.00, 'disponible', 2),
(6, 'Suite', 3, 480.00, 'disponible', 2),
(7, 'Double', 2, 160.00, 'disponible', 3),
(8, 'Suite', 4, 320.00, 'reservee', 3),
(9, 'Simple', 1, 70.00, 'disponible', 4),
(10, 'Double', 2, 110.00, 'disponible', 4),
(11, 'Double', 2, 140.00, 'disponible', 5),
(12, 'Suite', 2, 290.00, 'disponible', 5),
(13, 'Simple', 1, 65.00, 'disponible', 6),
(14, 'Double', 2, 105.00, 'reservee', 6),
(15, 'Suite', 3, 350.00, 'disponible', 7),
(16, 'Double', 2, 180.00, 'disponible', 7),
(17, 'Simple', 1, 55.00, 'disponible', 8),
(18, 'Double', 2, 90.00, 'disponible', 8);

-- --------------------------------------------------------

--
-- Table structure for table `client`
--

CREATE TABLE `client` (
  `id_client` int(11) NOT NULL,
  `cin` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `client`
--

INSERT INTO `client` (`id_client`, `cin`) VALUES
(1, 'CIN12345678'),
(2, 'CIN23456789'),
(3, 'CIN34567890'),
(4, 'CIN45678901'),
(5, 'CIN56789012'),
(6, 'CIN67890123'),
(7, 'CIN78901234'),
(8, 'CIN89012345'),
(9, 'CIN90123456'),
(10, 'CIN01234567');

-- --------------------------------------------------------

--
-- Table structure for table `destination`
--

CREATE TABLE `destination` (
  `id_destination` int(11) NOT NULL,
  `nom_destination` varchar(150) DEFAULT NULL,
  `pays` varchar(100) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `destination`
--

INSERT INTO `destination` (`id_destination`, `nom_destination`, `pays`, `image`) VALUES
(1, 'Chania Old Town', 'Grece', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/pexels-catiamatos-984862.jpg'),
(2, 'Venise Canal Grande', 'Italie', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/pexels-leeloothefirst-5227440.jpg'),
(3, 'Pyramides de Gizeh', 'Egypte', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/pexels-the-world-hopper-929714-1851481.jpg'),
(4, 'Iles Feroe', 'Norvege', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/pexels-maksim-smirnov-27565989-32234331.jpg'),
(5, 'Crete Agora', 'Grece', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/pexels-catiamatos-984869.jpg'),
(6, 'Rethymno Vieille Ville', 'Grece', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/pexels-alexravvas-20727529.jpg'),
(7, 'Plage Tropicale Phuket', 'Thailande', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/pexels-suzukii-xingfu-67659-872831.jpg'),
(8, 'Camping Mont Blanc', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/pexels-cliford-mervil-988071-2398220.jpg'),
(9, 'ariana', 'tunisia', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/pexels-leeloothefirst-5227440.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `employee`
--

CREATE TABLE `employee` (
  `id_employee` int(11) NOT NULL,
  `matricule` varchar(50) DEFAULT NULL,
  `id_admin` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `employee`
--

INSERT INTO `employee` (`id_employee`, `matricule`, `id_admin`) VALUES
(701, 'EMP00701', 901),
(702, 'EMP00702', 901),
(703, 'EMP00703', 902),
(704, 'EMP00704', 903),
(705, 'EMP00705', 904);

-- --------------------------------------------------------

--
-- Table structure for table `excursion`
--

CREATE TABLE `excursion` (
  `id_excursion` int(11) NOT NULL,
  `titre` varchar(100) DEFAULT NULL,
  `destination` varchar(100) DEFAULT NULL,
  `date_depart` date DEFAULT NULL,
  `date_retour` date DEFAULT NULL,
  `prix` decimal(10,2) DEFAULT NULL,
  `nb_places` int(11) DEFAULT NULL,
  `statut` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `excursion`
--

INSERT INTO `excursion` (`id_excursion`, `titre`, `destination`, `date_depart`, `date_retour`, `prix`, `nb_places`, `statut`) VALUES
(1, 'Decouverte de Chania', 'Chania Old Town', '2026-04-10', '2026-04-15', 850.00, 20, 'Disponible'),
(2, 'Escapade a Venise', 'Venise Canal Grande', '2026-05-01', '2026-05-07', 1100.00, 15, 'Disponible'),
(3, 'Mysteres de Egypte', 'Pyramides de Gizeh', '2026-06-15', '2026-06-22', 1350.00, 12, 'Disponible'),
(4, 'Aventure aux Iles Feroe', 'Iles Feroe', '2026-07-01', '2026-07-10', 1600.00, 10, 'Disponible'),
(5, 'Crete Authentique', 'Crete Agora', '2026-04-20', '2026-04-26', 780.00, 25, 'Disponible'),
(6, 'Charme de Rethymno', 'Rethymno Vieille Ville', '2026-05-15', '2026-05-20', 720.00, 18, 'Complet'),
(7, 'Paradis Tropical Thailande', 'Plage Tropicale Phuket', '2026-08-01', '2026-08-10', 2100.00, 8, 'Disponible'),
(8, 'Trek Alpin et Nature', 'Camping Mont Blanc', '2026-07-15', '2026-07-20', 650.00, 16, 'Disponible');

-- --------------------------------------------------------

--
-- Table structure for table `forum`
--

CREATE TABLE `forum` (
  `id_forum` int(11) NOT NULL,
  `nom` varchar(150) DEFAULT NULL,
  `nbparticipant` int(11) DEFAULT NULL,
  `commentaire` text DEFAULT NULL,
  `idposte` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `forum`
--

INSERT INTO `forum` (`id_forum`, `nom`, `nbparticipant`, `commentaire`, `idposte`) VALUES
(1, 'Discussion Venise 2026', 12, 'Super article, reserve grace a tes conseils.', 1),
(2, 'Questions Egypte securite', 8, 'Est-ce que tu as eu des problemes sur place ?', 2),
(3, 'Retours Thailande Luxe', 5, 'Tres bon rapport qualite-prix pour ce standing.', 3),
(4, 'Conseils Randonnee Alpes', 15, 'Quelle est la difficulte reelle du trek ?', 4),
(5, 'Crete en famille avis', 20, 'On revient une semaine la-bas, exactement ca!', 5),
(6, 'Rethymno secrets', 7, 'J habite ici et j approuve ces recommandations.', 6),
(7, 'Reservations Grece printemps', 18, 'Il reste encore des places pour avril ?', 7),
(8, 'Voyageurs Feroe', 9, 'Les iles Feroe sont sur ma bucket list !', 8);

-- --------------------------------------------------------

--
-- Table structure for table `guide`
--

CREATE TABLE `guide` (
  `id_guide` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `guide`
--

INSERT INTO `guide` (`id_guide`) VALUES
(801),
(802),
(803),
(804),
(805);

-- --------------------------------------------------------

--
-- Table structure for table `hotel`
--

CREATE TABLE `hotel` (
  `id_hotel` int(11) NOT NULL,
  `nom_hotel` varchar(150) DEFAULT NULL,
  `adresse` text DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL,
  `nombre_etoile` int(11) DEFAULT NULL,
  `descescription` text DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `id_destination` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `hotel`
--

INSERT INTO `hotel` (`id_hotel`, `nom_hotel`, `adresse`, `ville`, `nombre_etoile`, `descescription`, `image`, `id_destination`) VALUES
(1, 'Hotel Poseidon', '12 Rue du Port', 'Chania', 4, 'Hotel de charme avec vue sur le port venetien.', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/hotels/h1.jpg', 1),
(2, 'Casa Venezia', 'Calle dei Fiori 34', 'Venise', 5, 'Palace historique au bord du Grand Canal.', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/hotels/h2.jpg', 2),
(3, 'Pharaoh Palace', 'Avenue des Pyramides 7', 'Gizeh', 4, 'Hotel luxueux face aux pyramides mythiques.', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/hotels/h3.jpg', 3),
(4, 'Nordic Fjord Lodge', 'Haraldsgata 22', 'Torshavn', 3, 'Lodge confortable au coeur des iles Feroe.', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/hotels/h4.jpg', 4),
(5, 'Creta Sun Resort', 'Platanias Beach Road', 'Heraklion', 4, 'Resort face a la mer Mediterranee.', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/hotels/h5.jpg', 5),
(6, 'Hotel Fortezza', 'Plateia Rimondi 5', 'Rethymno', 3, 'Boutique hotel dans la vieille ville ottomane.', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/hotels/h6.jpg', 6),
(7, 'Tropical Palms Hotel', 'Beach Road 101', 'Phuket', 5, 'Resort 5 etoiles en bord de mer tropicale.', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/hotels/h7.jpg', 7),
(8, 'Chalet Alpin', 'Route des Cimes 3', 'Chamonix', 3, 'Chalet authentique pour amateurs de randonnee.', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/hotels/h8.jpg', 8);

-- --------------------------------------------------------

--
-- Table structure for table `post`
--

CREATE TABLE `post` (
  `idPost` int(11) NOT NULL,
  `nomPost` varchar(200) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `prix` decimal(10,2) DEFAULT NULL,
  `typePost` varchar(50) DEFAULT NULL,
  `id_utilisateur` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `post`
--

INSERT INTO `post` (`idPost`, `nomPost`, `description`, `prix`, `typePost`, `id_utilisateur`) VALUES
(1, 'Mon voyage a Venise', 'Un sejour inoubliable a la decouverte des canaux.', 0.00, 'Question', 1),
(2, 'Guide pratique Egypte 2026', 'Tous mes conseils pour visiter les pyramides.', 0.00, 'Recommandation', 2),
(3, 'Offre Thailande Luxe', 'Package tout compris 10 jours en Thailande.', 2100.00, 'Question', 3),
(4, 'Trek Alpin debutant', 'Circuit adapte aux novices en altitude.', 650.00, 'Recommandation', 4),
(5, 'Crete en famille', 'Meilleures adresses pour voyager en famille.', 0.00, 'Question', 5),
(6, 'Rethymno cache', 'Les joyaux caches de la vieille ville.', 0.00, 'Recommandation', 6),
(7, 'Offre Grece Printemps', 'Combine 2 iles grecques au meilleur prix.', 780.00, 'Question', 7),
(8, 'Aventure Feroe', 'Mon retour 10 jours dans une destination sauvage.', 0.00, 'Recommandation', 8);

-- --------------------------------------------------------

--
-- Table structure for table `reservation`
--

CREATE TABLE `reservation` (
  `id_reservation` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `id_destination` int(11) NOT NULL,
  `id_hotel` int(11) DEFAULT NULL,
  `id_chambre` int(11) DEFAULT NULL,
  `transport_type` varchar(50) DEFAULT NULL,
  `id_transport` int(11) DEFAULT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `prix_total` decimal(10,2) NOT NULL,
  `status` varchar(20) DEFAULT 'en_attente'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reservation`
--

INSERT INTO `reservation` (`id_reservation`, `id_utilisateur`, `id_destination`, `id_hotel`, `id_chambre`, `transport_type`, `id_transport`, `date_debut`, `date_fin`, `prix_total`, `status`) VALUES
(1, 1, 1, 1, 2, 'public', 1, '2026-04-10', '2026-04-15', 1200.00, 'confirmee'),
(2, 2, 2, 2, 5, 'public', 3, '2026-05-01', '2026-05-07', 1650.00, 'confirmee'),
(3, 3, 3, 3, 7, 'prive', 3, '2026-06-15', '2026-06-22', 1890.00, 'en_attente'),
(4, 4, 4, 4, 9, 'public', 6, '2026-07-01', '2026-07-10', 1820.00, 'confirmee'),
(5, 5, 5, 5, 11, 'public', 7, '2026-04-20', '2026-04-26', 980.00, 'confirmee'),
(6, 6, 6, 6, 13, 'prive', 6, '2026-05-15', '2026-05-20', 870.00, 'annulee'),
(7, 7, 7, 7, 15, 'prive', 7, '2026-08-01', '2026-08-10', 2780.00, 'confirmee'),
(8, 8, 8, 8, 17, 'public', 10, '2026-07-15', '2026-07-20', 730.00, 'en_attente'),
(9, 9, 1, 1, 1, 'public', 2, '2026-04-10', '2026-04-15', 950.00, 'confirmee'),
(10, 10, 2, 2, 6, 'prive', 2, '2026-05-01', '2026-05-07', 2100.00, 'en_attente'),
(15, 1, 3, 3, 3, 'public', 5, '2026-02-19', '2026-02-21', 405.00, 'en_attente'),
(16, 1, 3, 3, 2, 'prive', 3, '2026-02-17', '2026-02-19', 550.00, 'en_attente'),
(17, 1, 5, 5, 3, 'prive', 5, '2026-02-19', '2026-03-14', 4680.00, 'en_attente'),
(18, 1, 4, 4, 3, 'prive', 4, '2026-02-19', '2026-02-21', 400.00, 'en_attente'),
(19, 1, 4, 4, 3, 'public', 6, '2026-02-26', '2026-03-01', 485.00, 'en_attente'),
(20, 1, 4, 4, 3, 'public', 6, '2026-02-18', '2026-02-23', 785.00, 'en_attente'),
(21, 1, 4, 4, 2, 'prive', 4, '2026-02-19', '2026-02-27', 1300.00, 'en_attente'),
(22, 1, 3, 3, 2, 'public', 4, '2026-02-18', '2026-02-24', 1380.00, 'en_attente'),
(23, 1, 2, 2, NULL, 'public', 3, '2026-02-10', '2026-02-17', 1762.00, 'en_attente'),
(24, 1, 4, 4, 2, NULL, NULL, '2026-02-12', '2026-02-27', 2250.00, 'en_attente'),
(25, 1, 5, 5, 3, NULL, NULL, '2026-02-20', '2026-02-27', 1400.00, 'en_attente');

-- --------------------------------------------------------

--
-- Table structure for table `transport_prive`
--

CREATE TABLE `transport_prive` (
  `id_transport_priv` int(11) NOT NULL,
  `marque` varchar(100) DEFAULT NULL,
  `etat` varchar(50) DEFAULT NULL,
  `complement` text DEFAULT NULL,
  `prix_Lac` decimal(10,2) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `id_destination` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transport_prive`
--

INSERT INTO `transport_prive` (`id_transport_priv`, `marque`, `etat`, `complement`, `prix_Lac`, `image`, `id_destination`) VALUES
(1, 'Mercedes Sprinter', 'Excellent', 'Van 8 places climatise, chauffeur prive', 120.00, 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tp1.jpg', 1),
(2, 'BMW 5 Series', 'Bon', 'Berline confort, chauffeur en costume', 90.00, 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tp2.jpg', 2),
(3, 'Toyota Land Cruiser', 'Excellent', '4x4 ideal pour le desert et les sites', 150.00, 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tp3.jpg', 3),
(4, 'Volvo XC90', 'Bon', 'SUV premium pour routes de montagne', 100.00, 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tp4.jpg', 4),
(5, 'Renault Trafic', 'Correct', 'Minibus 9 places pour groupes', 80.00, 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tp5.jpg', 5),
(6, 'Fiat 500', 'Excellent', 'Citadine parfaite pour les ruelles', 50.00, 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tp6.jpg', 6),
(7, 'Rolls-Royce Ghost', 'Excellent', 'Limousine de luxe transfert aeroport', 300.00, 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tp7.jpg', 7),
(8, 'Peugeot 3008', 'Bon', 'SUV confortable pour randonneurs', 75.00, 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tp8.jpg', 8);

-- --------------------------------------------------------

--
-- Table structure for table `transport_publique`
--

CREATE TABLE `transport_publique` (
  `id_transport_pub` int(11) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `tarif` decimal(10,2) DEFAULT NULL,
  `horraire` varchar(100) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `id_destination` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transport_publique`
--

INSERT INTO `transport_publique` (`id_transport_pub`, `type`, `tarif`, `horraire`, `image`, `id_destination`) VALUES
(1, 'Bus', 8.00, '08:00-22:00 / toutes les 30min', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub1.jpg', 1),
(2, 'Bateau', 15.00, '09:00-18:00 / toutes les heures', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub2.jpg', 1),
(3, 'Vaporetto', 12.00, '06:00-24:00 / toutes les 12min', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub3.jpg', 2),
(4, 'Avion', 180.00, 'Departs quotidiens 07:00 et 14:00', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub4.jpg', 3),
(5, 'Bus', 5.00, '07:00-21:00 / toutes les 45min', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub5.jpg', 3),
(6, 'Ferry', 35.00, 'Quotidien 10:00 / 3h de traversee', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub6.jpg', 4),
(7, 'Bus', 3.50, '06:30-23:00 / toutes les 20min', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub7.jpg', 5),
(8, 'Taxi partage', 6.00, 'Sur demande 24h/24', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub8.jpg', 6),
(9, 'Navette', 20.00, 'Aeroport-hotel 4 fois par jour', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub9.jpg', 7),
(10, 'Train', 25.00, 'TGV 3 allers/jour Paris-Chamonix', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/transport/tpub10.jpg', 8);

-- --------------------------------------------------------

--
-- Table structure for table `utilisateur`
--

CREATE TABLE `utilisateur` (
  `id_utilisateur` int(11) NOT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `prenom` varchar(100) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `mot_de_passe` varchar(255) DEFAULT NULL,
  `pays` varchar(50) DEFAULT NULL,
  `imageurl` varchar(255) DEFAULT NULL,
  `type_utilisateur` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `utilisateur`
--

INSERT INTO `utilisateur` (`id_utilisateur`, `nom`, `prenom`, `email`, `mot_de_passe`, `pays`, `imageurl`, `type_utilisateur`) VALUES
(1, 'Dupont', 'Alice', 'alice.dupont@email.com', '$2y$10$hpwd001', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u1.jpg', 'client'),
(2, 'Moreau', 'Baptiste', 'baptiste.moreau@email.com', '$2y$10$hpwd002', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u2.jpg', 'client'),
(3, 'Martin', 'Clara', 'clara.martin@email.com', '$2y$10$hpwd003', 'Belgique', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u3.jpg', 'client'),
(4, 'Bernard', 'David', 'david.bernard@email.com', '$2y$10$hpwd004', 'Suisse', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u4.jpg', 'client'),
(5, 'Petit', 'Emilie', 'emilie.petit@email.com', '$2y$10$hpwd005', 'Canada', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u5.jpg', 'client'),
(6, 'Robert', 'Francois', 'francois.robert@email.com', '$2y$10$hpwd006', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u6.jpg', 'client'),
(7, 'Simon', 'Gabrielle', 'gabrielle.simon@email.com', '$2y$10$hpwd007', 'Maroc', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u7.jpg', 'client'),
(8, 'Laurent', 'Hugo', 'hugo.laurent@email.com', '$2y$10$hpwd008', 'Tunisie', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u8.jpg', 'client'),
(9, 'Thomas', 'Ines', 'ines.thomas@email.com', '$2y$10$hpwd009', 'Algerie', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u9.jpg', 'client'),
(10, 'Richard', 'Julien', 'julien.richard@email.com', '$2y$10$hpwd010', 'Portugal', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/u10.jpg', 'client'),
(701, 'Fontaine', 'Patricia', 'patricia.fontaine@pidev.com', '$2y$10$emp001', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/e1.jpg', 'employee'),
(702, 'Chevalier', 'Quentin', 'quentin.chevalier@pidev.com', '$2y$10$emp002', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/e2.jpg', 'employee'),
(703, 'Rousseau', 'Rachel', 'rachel.rousseau@pidev.com', '$2y$10$emp003', 'Suisse', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/e3.jpg', 'employee'),
(704, 'Vincent', 'Samuel', 'samuel.vincent@pidev.com', '$2y$10$emp004', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/e4.jpg', 'employee'),
(705, 'Morel', 'Theodore', 'theodore.morel@pidev.com', '$2y$10$emp005', 'Belgique', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/e5.jpg', 'employee'),
(801, 'Girard', 'Ursula', 'ursula.girard@pidev.com', '$2y$10$gui001', 'Grece', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/g1.jpg', 'guide'),
(802, 'Boyer', 'Victor', 'victor.boyer@pidev.com', '$2y$10$gui002', 'Italie', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/g2.jpg', 'guide'),
(803, 'Leroy', 'Wendy', 'wendy.leroy@pidev.com', '$2y$10$gui003', 'Egypte', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/g3.jpg', 'guide'),
(804, 'Roux', 'Xavier', 'xavier.roux@pidev.com', '$2y$10$gui004', 'Norvege', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/g4.jpg', 'guide'),
(805, 'Blanc', 'Yasmine', 'yasmine.blanc@pidev.com', '$2y$10$gui005', 'Thailande', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/g5.jpg', 'guide'),
(901, 'Leclerc', 'Marc', 'marc.leclerc@pidev.com', '$2y$10$adm001', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/a1.jpg', 'admin'),
(902, 'Garnier', 'Nathalie', 'nathalie.garnier@pidev.com', '$2y$10$adm002', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/a2.jpg', 'admin'),
(903, 'Renard', 'Olivier', 'olivier.renard@pidev.com', '$2y$10$adm003', 'Belgique', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/a3.jpg', 'admin'),
(904, 'Mercier', 'Pauline', 'pauline.mercier@pidev.com', '$2y$10$adm004', 'Suisse', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/a4.jpg', 'admin'),
(905, 'Faure', 'Quentin', 'quentin.faure@pidev.com', '$2y$10$adm005', 'France', 'file:/home/wacel/pidev/PlaNova/src/main/resources/images/users/a5.jpg', 'admin');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activite`
--
ALTER TABLE `activite`
  ADD PRIMARY KEY (`id_activite`),
  ADD KEY `id_excursion` (`id_excursion`),
  ADD KEY `id_destination` (`id_destination`);

--
-- Indexes for table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`id_admin`);

--
-- Indexes for table `billet`
--
ALTER TABLE `billet`
  ADD PRIMARY KEY (`id_billet`),
  ADD KEY `id_destination` (`id_destination`),
  ADD KEY `id_transport_pub` (`id_transport_pub`),
  ADD KEY `id_transport_priv` (`id_transport_priv`);

--
-- Indexes for table `chambre`
--
ALTER TABLE `chambre`
  ADD PRIMARY KEY (`id_chambre`),
  ADD KEY `id_hotel` (`id_hotel`);

--
-- Indexes for table `client`
--
ALTER TABLE `client`
  ADD PRIMARY KEY (`id_client`);

--
-- Indexes for table `destination`
--
ALTER TABLE `destination`
  ADD PRIMARY KEY (`id_destination`),
  ADD UNIQUE KEY `uq_nom_destination` (`nom_destination`);

--
-- Indexes for table `employee`
--
ALTER TABLE `employee`
  ADD PRIMARY KEY (`id_employee`),
  ADD KEY `id_admin` (`id_admin`);

--
-- Indexes for table `excursion`
--
ALTER TABLE `excursion`
  ADD PRIMARY KEY (`id_excursion`);

--
-- Indexes for table `forum`
--
ALTER TABLE `forum`
  ADD PRIMARY KEY (`id_forum`),
  ADD KEY `idposte` (`idposte`);

--
-- Indexes for table `guide`
--
ALTER TABLE `guide`
  ADD PRIMARY KEY (`id_guide`);

--
-- Indexes for table `hotel`
--
ALTER TABLE `hotel`
  ADD PRIMARY KEY (`id_hotel`),
  ADD KEY `id_destination` (`id_destination`);

--
-- Indexes for table `post`
--
ALTER TABLE `post`
  ADD PRIMARY KEY (`idPost`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Indexes for table `reservation`
--
ALTER TABLE `reservation`
  ADD PRIMARY KEY (`id_reservation`),
  ADD KEY `id_utilisateur` (`id_utilisateur`),
  ADD KEY `id_destination` (`id_destination`),
  ADD KEY `fk_res_chambre` (`id_chambre`);

--
-- Indexes for table `transport_prive`
--
ALTER TABLE `transport_prive`
  ADD PRIMARY KEY (`id_transport_priv`),
  ADD KEY `id_destination` (`id_destination`);

--
-- Indexes for table `transport_publique`
--
ALTER TABLE `transport_publique`
  ADD PRIMARY KEY (`id_transport_pub`),
  ADD KEY `id_destination` (`id_destination`);

--
-- Indexes for table `utilisateur`
--
ALTER TABLE `utilisateur`
  ADD PRIMARY KEY (`id_utilisateur`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activite`
--
ALTER TABLE `activite`
  MODIFY `id_activite` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `billet`
--
ALTER TABLE `billet`
  MODIFY `id_billet` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `chambre`
--
ALTER TABLE `chambre`
  MODIFY `id_chambre` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `destination`
--
ALTER TABLE `destination`
  MODIFY `id_destination` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `excursion`
--
ALTER TABLE `excursion`
  MODIFY `id_excursion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `forum`
--
ALTER TABLE `forum`
  MODIFY `id_forum` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `hotel`
--
ALTER TABLE `hotel`
  MODIFY `id_hotel` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `post`
--
ALTER TABLE `post`
  MODIFY `idPost` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `reservation`
--
ALTER TABLE `reservation`
  MODIFY `id_reservation` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT for table `transport_prive`
--
ALTER TABLE `transport_prive`
  MODIFY `id_transport_priv` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `transport_publique`
--
ALTER TABLE `transport_publique`
  MODIFY `id_transport_pub` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `utilisateur`
--
ALTER TABLE `utilisateur`
  MODIFY `id_utilisateur` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=906;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activite`
--
ALTER TABLE `activite`
  ADD CONSTRAINT `activite_ibfk_1` FOREIGN KEY (`id_excursion`) REFERENCES `excursion` (`id_excursion`),
  ADD CONSTRAINT `activite_ibfk_2` FOREIGN KEY (`id_destination`) REFERENCES `destination` (`id_destination`);

--
-- Constraints for table `admin`
--
ALTER TABLE `admin`
  ADD CONSTRAINT `admin_ibfk_1` FOREIGN KEY (`id_admin`) REFERENCES `utilisateur` (`id_utilisateur`);

--
-- Constraints for table `billet`
--
ALTER TABLE `billet`
  ADD CONSTRAINT `billet_ibfk_1` FOREIGN KEY (`id_destination`) REFERENCES `destination` (`id_destination`),
  ADD CONSTRAINT `billet_ibfk_2` FOREIGN KEY (`id_transport_pub`) REFERENCES `transport_publique` (`id_transport_pub`),
  ADD CONSTRAINT `billet_ibfk_3` FOREIGN KEY (`id_transport_priv`) REFERENCES `transport_prive` (`id_transport_priv`);

--
-- Constraints for table `chambre`
--
ALTER TABLE `chambre`
  ADD CONSTRAINT `chambre_ibfk_1` FOREIGN KEY (`id_hotel`) REFERENCES `hotel` (`id_hotel`);

--
-- Constraints for table `client`
--
ALTER TABLE `client`
  ADD CONSTRAINT `client_ibfk_1` FOREIGN KEY (`id_client`) REFERENCES `utilisateur` (`id_utilisateur`);

--
-- Constraints for table `employee`
--
ALTER TABLE `employee`
  ADD CONSTRAINT `employee_ibfk_1` FOREIGN KEY (`id_employee`) REFERENCES `utilisateur` (`id_utilisateur`),
  ADD CONSTRAINT `employee_ibfk_2` FOREIGN KEY (`id_admin`) REFERENCES `admin` (`id_admin`);

--
-- Constraints for table `forum`
--
ALTER TABLE `forum`
  ADD CONSTRAINT `forum_ibfk_1` FOREIGN KEY (`idposte`) REFERENCES `post` (`idPost`);

--
-- Constraints for table `guide`
--
ALTER TABLE `guide`
  ADD CONSTRAINT `guide_ibfk_1` FOREIGN KEY (`id_guide`) REFERENCES `utilisateur` (`id_utilisateur`);

--
-- Constraints for table `hotel`
--
ALTER TABLE `hotel`
  ADD CONSTRAINT `hotel_ibfk_1` FOREIGN KEY (`id_destination`) REFERENCES `destination` (`id_destination`);

--
-- Constraints for table `post`
--
ALTER TABLE `post`
  ADD CONSTRAINT `post_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id_utilisateur`);

--
-- Constraints for table `reservation`
--
ALTER TABLE `reservation`
  ADD CONSTRAINT `fk_res_chambre` FOREIGN KEY (`id_chambre`) REFERENCES `chambre` (`id_chambre`),
  ADD CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id_utilisateur`),
  ADD CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`id_destination`) REFERENCES `destination` (`id_destination`);

--
-- Constraints for table `transport_prive`
--
ALTER TABLE `transport_prive`
  ADD CONSTRAINT `transport_prive_ibfk_1` FOREIGN KEY (`id_destination`) REFERENCES `destination` (`id_destination`);

--
-- Constraints for table `transport_publique`
--
ALTER TABLE `transport_publique`
  ADD CONSTRAINT `transport_publique_ibfk_1` FOREIGN KEY (`id_destination`) REFERENCES `destination` (`id_destination`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
