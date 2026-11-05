-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 29-10-2026 a las 00:36:49
-- Versión del servidor: 13.0.2-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `mihospital_db`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auth_group`
--

CREATE TABLE `auth_group` (
  `id` int(11) NOT NULL,
  `name` varchar(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auth_group_permissions`
--

CREATE TABLE `auth_group_permissions` (
  `id` bigint(20) NOT NULL,
  `group_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auth_permission`
--

CREATE TABLE `auth_permission` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `content_type_id` int(11) NOT NULL,
  `codename` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `auth_permission`
--

INSERT INTO `auth_permission` (`id`, `name`, `content_type_id`, `codename`) VALUES
(1, 'Can add log entry', 1, 'add_logentry'),
(2, 'Can change log entry', 1, 'change_logentry'),
(3, 'Can delete log entry', 1, 'delete_logentry'),
(4, 'Can view log entry', 1, 'view_logentry'),
(5, 'Can add permission', 3, 'add_permission'),
(6, 'Can change permission', 3, 'change_permission'),
(7, 'Can delete permission', 3, 'delete_permission'),
(8, 'Can view permission', 3, 'view_permission'),
(9, 'Can add group', 2, 'add_group'),
(10, 'Can change group', 2, 'change_group'),
(11, 'Can delete group', 2, 'delete_group'),
(12, 'Can view group', 2, 'view_group'),
(13, 'Can add user', 4, 'add_user'),
(14, 'Can change user', 4, 'change_user'),
(15, 'Can delete user', 4, 'delete_user'),
(16, 'Can view user', 4, 'view_user'),
(17, 'Can add content type', 5, 'add_contenttype'),
(18, 'Can change content type', 5, 'change_contenttype'),
(19, 'Can delete content type', 5, 'delete_contenttype'),
(20, 'Can view content type', 5, 'view_contenttype'),
(21, 'Can add session', 6, 'add_session'),
(22, 'Can change session', 6, 'change_session'),
(23, 'Can delete session', 6, 'delete_session'),
(24, 'Can view session', 6, 'view_session'),
(25, 'Can add usuario', 7, 'add_usuario'),
(26, 'Can change usuario', 7, 'change_usuario'),
(27, 'Can delete usuario', 7, 'delete_usuario'),
(28, 'Can view usuario', 7, 'view_usuario'),
(29, 'Can add medico', 8, 'add_medico'),
(30, 'Can change medico', 8, 'change_medico'),
(31, 'Can delete medico', 8, 'delete_medico'),
(32, 'Can view medico', 8, 'view_medico'),
(33, 'Can add paciente', 9, 'add_paciente'),
(34, 'Can change paciente', 9, 'change_paciente'),
(35, 'Can delete paciente', 9, 'delete_paciente'),
(36, 'Can view paciente', 9, 'view_paciente');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auth_user`
--

CREATE TABLE `auth_user` (
  `id` int(11) NOT NULL,
  `password` varchar(128) NOT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `is_superuser` tinyint(1) NOT NULL,
  `username` varchar(150) NOT NULL,
  `first_name` varchar(150) NOT NULL,
  `last_name` varchar(150) NOT NULL,
  `email` varchar(254) NOT NULL,
  `is_staff` tinyint(1) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `date_joined` datetime(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `auth_user`
--

-- Cuentas ficticias de demostración; contraseña documentada en backend/README.md.
INSERT INTO `auth_user` (`id`, `password`, `last_login`, `is_superuser`, `username`, `first_name`, `last_name`, `email`, `is_staff`, `is_active`, `date_joined`) VALUES
(5, 'pbkdf2_sha256$1500000$Ve5Du8ZXfejzcKT4Z1iFsa$+E+J9buIIerr27pWcBvoTRrAfAqGVp2V3262p81N9KU=', '2026-10-28 23:33:09.345327', 1, 'gonzalo', '', '', 'gonzalodavila2304@gmail.com', 1, 1, '2026-10-24 21:26:42.419500'),
(6, 'pbkdf2_sha256$1500000$NSzFcELQ2hpoRxLHHKzT0z$OkZEY7vxrdSVuYy0FSalanRQDn2F5wXgTIPhz126rMM=', NULL, 0, 'lucasdavila1710@gmail.com', 'Lucas Dario', 'Davila Ochochoque', 'lucasdavila1710@gmail.com', 0, 1, '2026-10-24 21:41:43.599583'),
(7, 'pbkdf2_sha256$1500000$Xv3vZziokRiFU68ik3sTdr$ayfHWi+RcgMF+ZXKea0N4k9qgdAVNk7tCtrRFXgUm5M=', NULL, 0, 'velasquezana2708@gmail.com', 'Ana', 'Ortiz Velasquez', 'velasquezana2708@gmail.com', 0, 0, '2026-10-27 19:33:09.575137'),
(8, 'pbkdf2_sha256$1500000$zP9sGWIEq57u6eZXqO9lr8$D8pLz/YZcuXG8GT7SVwlJxZAzWH+BRLZR1h/GCQ0t+Y=', NULL, 0, 'davilagonzalo2304@gmail.com', 'Gonzalo', 'Davila Ochochoque', 'davilagonzalo2304@gmail.com', 0, 1, '2026-10-27 20:27:12.948298'),
(9, 'pbkdf2_sha256$1500000$yYtI7pWUtrNDCRksAlMz71$0DWNmcedUQ0pPTDyVhIiqi1nkdTYWI+Gn7KzXDqMrW4=', NULL, 0, 'suarezpedro1205@tecsup.edu.pe', 'Pedro', 'Suarez Lavado', 'suarezpedro1205@tecsup.edu.pe', 0, 0, '2026-10-27 22:59:49.308515'),
(10, 'pbkdf2_sha256$1000000$EyYTugMiBLXe9CrJ5uGQxc$xyWAtGzzuRVaP/hkhde0292FM90g9wL2XwoJq13WzC0=', NULL, 0, 'medico.prueba@tecsup.edu.pe', 'Valeria', 'Rojas Medina', 'medico.prueba@tecsup.edu.pe', 0, 1, '2026-10-30 11:00:00.000000'),
(11, 'pbkdf2_sha256$1000000$EyYTugMiBLXe9CrJ5uGQxc$xyWAtGzzuRVaP/hkhde0292FM90g9wL2XwoJq13WzC0=', NULL, 0, 'paciente.prueba@tecsup.edu.pe', 'Juan Carlos', 'Pérez Gómez', 'paciente.prueba@tecsup.edu.pe', 0, 1, '2026-10-30 11:00:00.000000');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auth_user_groups`
--

CREATE TABLE `auth_user_groups` (
  `id` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auth_user_user_permissions`
--

CREATE TABLE `auth_user_user_permissions` (
  `id` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `django_admin_log`
--

CREATE TABLE `django_admin_log` (
  `id` int(11) NOT NULL,
  `action_time` datetime(6) NOT NULL,
  `object_id` longtext DEFAULT NULL,
  `object_repr` varchar(200) NOT NULL,
  `action_flag` smallint(5) UNSIGNED NOT NULL CHECK (`action_flag` >= 0),
  `change_message` longtext NOT NULL,
  `content_type_id` int(11) DEFAULT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `django_admin_log`
--

INSERT INTO `django_admin_log` (`id`, `action_time`, `object_id`, `object_repr`, `action_flag`, `change_message`, `content_type_id`, `user_id`) VALUES
(1, '2026-10-24 21:28:19.827472', '3', 'florespedro@gmail.com.xd', 3, '', 4, 5),
(2, '2026-10-24 21:28:25.791141', '4', 'juandiego@tecsup.edu.pe', 3, '', 4, 5),
(3, '2026-10-24 21:28:39.454327', '1', 'rojasana@gmail.com', 3, '', 4, 5);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `django_content_type`
--

CREATE TABLE `django_content_type` (
  `id` int(11) NOT NULL,
  `app_label` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `django_content_type`
--

INSERT INTO `django_content_type` (`id`, `app_label`, `model`) VALUES
(1, 'admin', 'logentry'),
(2, 'auth', 'group'),
(3, 'auth', 'permission'),
(4, 'auth', 'user'),
(5, 'contenttypes', 'contenttype'),
(6, 'sessions', 'session'),
(8, 'usuarios', 'medico'),
(9, 'usuarios', 'paciente'),
(7, 'usuarios', 'usuario');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `django_migrations`
--

CREATE TABLE `django_migrations` (
  `id` bigint(20) NOT NULL,
  `app` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `applied` datetime(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `django_migrations`
--

INSERT INTO `django_migrations` (`id`, `app`, `name`, `applied`) VALUES
(1, 'contenttypes', '0001_initial', '2026-10-20 21:01:35.062509'),
(2, 'auth', '0001_initial', '2026-10-20 21:01:35.230639'),
(3, 'admin', '0001_initial', '2026-10-20 21:01:35.264420'),
(4, 'admin', '0002_logentry_remove_auto_add', '2026-10-20 21:01:35.268942'),
(5, 'admin', '0003_logentry_add_action_flag_choices', '2026-10-20 21:01:35.275228'),
(6, 'contenttypes', '0002_remove_content_type_name', '2026-10-20 21:01:35.308888'),
(7, 'auth', '0002_alter_permission_name_max_length', '2026-10-20 21:01:35.327586'),
(8, 'auth', '0003_alter_user_email_max_length', '2026-10-20 21:01:35.358682'),
(9, 'auth', '0004_alter_user_username_opts', '2026-10-20 21:01:35.363365'),
(10, 'auth', '0005_alter_user_last_login_null', '2026-10-20 21:01:35.383211'),
(11, 'auth', '0006_require_contenttypes_0002', '2026-10-20 21:01:35.385537'),
(12, 'auth', '0007_alter_validators_add_error_messages', '2026-10-20 21:01:35.392118'),
(13, 'auth', '0008_alter_user_username_max_length', '2026-10-20 21:01:35.404057'),
(14, 'auth', '0009_alter_user_last_name_max_length', '2026-10-20 21:01:35.416864'),
(15, 'auth', '0010_alter_group_name_max_length', '2026-10-20 21:01:35.429388'),
(16, 'auth', '0011_update_proxy_permissions', '2026-10-20 21:01:35.438068'),
(17, 'auth', '0012_alter_user_first_name_max_length', '2026-10-20 21:01:35.452804'),
(18, 'sessions', '0001_initial', '2026-10-20 21:01:35.465459'),
(19, 'usuarios', '0001_initial', '2026-10-20 21:01:35.472238'),
(20, 'usuarios', '0002_cuenta_y_dni_opcional', '2026-10-21 19:17:13.944397'),
(21, 'usuarios', '0003_usuario_rol', '2026-10-27 21:11:42.166503'),
(22, 'usuarios', '0004_alter_usuario_rol', '2026-10-27 21:21:39.424038'),
(23, 'usuarios', '0005_medico_paciente', '2026-10-27 23:45:03.475172'),
(24, 'usuarios', '0006_alter_usuario_rol', '2026-10-28 00:54:43.328932'),
(25, 'usuarios', '0007_medico_cmp_medico_direccion_medico_especialidad_and_more', '2026-10-28 00:59:27.901459'),
(26, 'usuarios', '0008_usuario_permisos', '2026-10-28 20:26:39.858241'),
(27, 'api', '0001_initial', '2026-10-30 10:55:00.000000'),
(28, 'usuarios', '0002_paciente_alergias_paciente_condiciones_cronicas_and_more', '2026-10-30 11:14:00.000000');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `django_session`
--

CREATE TABLE `django_session` (
  `session_key` varchar(40) NOT NULL,
  `session_data` longtext NOT NULL,
  `expire_date` datetime(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `django_session`
--

INSERT INTO `django_session` (`session_key`, `session_data`, `expire_date`) VALUES
('3meonttwec56hvg77j4n7dc73dco6pmg', '.eJxVjMsOwiAQRf-FtSHD8Ci4dO83kIGhUjU0Ke3K-O_apAvd3nPOfYlI21rj1ssSJxZnYcXpd0uUH6XtgO_UbrPMc1uXKcldkQft8jpzeV4O9--gUq_fOngyxmVgD8UoYzW6hKCLthYV6tEPwYJWzIAZFAEEQx4cDjxiVgHE-wOiFTYD:1xM9rt:iDfo8xhhzhF1vtKtc_-f3kfIY9IG97LM__duhqFJNSk', '2026-11-11 20:03:29.644671'),
('grqdi55pa1559lynupx4z0bjydsmdbvp', '.eJxVjMsOwiAQRf-FtSHD8Ci4dO83kIGhUjU0Ke3K-O_apAvd3nPOfYlI21rj1ssSJxZnYcXpd0uUH6XtgO_UbrPMc1uXKcldkQft8jpzeV4O9--gUq_fOngyxmVgD8UoYzW6hKCLthYV6tEPwYJWzIAZFAEEQx4cDjxiVgHE-wOiFTYD:1xKjxJ:jjOafdjGxKxgbQkgEOz6GlVY6vfWGRiwRotWBIluSNg', '2026-11-07 22:11:13.825226'),
('kyemsv8ycp4k2udtgh8acqzvjbf7esrf', '.eJxVjMsOwiAQRf-FtSHD8Ci4dO83kIGhUjU0Ke3K-O_apAvd3nPOfYlI21rj1ssSJxZnYcXpd0uUH6XtgO_UbrPMc1uXKcldkQft8jpzeV4O9--gUq_fOngyxmVgD8UoYzW6hKCLthYV6tEPwYJWzIAZFAEEQx4cDjxiVgHE-wOiFTYD:1xMD8n:1G-qAfb-00veyzVxaz6picU7F03hmFPjxNIILyjsRic', '2026-11-11 23:33:09.364817');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios_medico`
--

CREATE TABLE `usuarios_medico` (
  `id` bigint(20) NOT NULL,
  `usuario_id` bigint(20) NOT NULL,
  `cmp` varchar(20) DEFAULT NULL,
  `direccion` longtext NOT NULL,
  `especialidad` varchar(100) NOT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `identificacion_complementaria` varchar(100) NOT NULL,
  `sexo` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios_medico`
--

INSERT INTO `usuarios_medico` (`id`, `usuario_id`, `cmp`, `direccion`, `especialidad`, `fecha_nacimiento`, `identificacion_complementaria`, `sexo`) VALUES
(1, 7, '1514', 'Av. Republica 2625', 'Cardiologia', '2004-08-27', '', 'femenino'),
(2, 8, '1234', '', 'Pedriatria', '1999-05-12', '', 'masculino'),
(3, 9, 'CMP-DEMO-001', '', 'Medicina general', NULL, '', 'no especificado');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios_paciente`
--

CREATE TABLE `usuarios_paciente` (
  `id` bigint(20) NOT NULL,
  `usuario_id` bigint(20) NOT NULL,
  `contacto_emergencia` varchar(120) NOT NULL,
  `direccion` longtext NOT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `informacion_contacto` longtext NOT NULL,
  `sexo` varchar(20) NOT NULL,
  `telefono_emergencia` varchar(20) NOT NULL,
  `tipo_sangre` varchar(3) NOT NULL,
  `alergias` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`alergias`)),
  `condiciones_cronicas` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`condiciones_cronicas`)),
  `medicamentos_actuales` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`medicamentos_actuales`)),
  `observaciones_medicas` longtext NOT NULL,
  `informacion_medica_actualizada` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios_paciente`
--

INSERT INTO `usuarios_paciente` (`id`, `usuario_id`, `contacto_emergencia`, `direccion`, `fecha_nacimiento`, `informacion_contacto`, `sexo`, `telefono_emergencia`, `tipo_sangre`, `alergias`, `condiciones_cronicas`, `medicamentos_actuales`, `observaciones_medicas`, `informacion_medica_actualizada`) VALUES
(2, 1, 'Ana Davila', 'Av. Los Incas 1718, Cusco', '2006-04-23', '951987984', 'masculino', '951987984', 'O+', '[\"Sin alergias declaradas\"]', '[\"Ninguna registrada\"]', '[\"Ninguno\"]', 'Datos sintéticos; confirmar la información directamente con la persona atendida.', '2026-03-01'),
(3, 10, 'Marisol Pérez (Madre)', 'Jr. Los Álamos 245, Lima', '2000-05-15', '987654321', 'masculino', '912345678', 'O+', '[\"Penicilina (dato ficticio)\"]', '[\"Ninguna registrada\"]', '[\"Ninguno\"]', 'Nota ficticia de demostración: dato de alergia pendiente de confirmación directa con el paciente.', '2026-03-01'),
(4, 11, 'Elena Cárdenas (Madre)', 'Av. El Sol 520, Cusco', '1992-04-15', '987654321', 'femenino', '976543210', 'O+', '[\"Látex (dato ficticio)\"]', '[\"Ninguna registrada\"]', '[\"Ninguno\"]', 'Nota ficticia de demostración: confirmar alergias y antecedentes directamente con el paciente antes de usarlos.', '2026-03-01'),
(5, 12, 'Carlos Salazar (Padre)', 'Jr. Lima 380, Arequipa', '1987-09-22', '986123450', 'masculino', '965432108', 'A+', '[\"Ninguna registrada (dato ficticio)\"]', '[\"Ninguna registrada\"]', '[\"Ninguno\"]', 'Nota ficticia de demostración: registro inicial; completar la información en una entrevista real.', '2026-03-01'),
(6, 13, 'Patricia Medina (Madre)', 'Av. Larco 1420, Trujillo', '1998-12-03', '985234561', 'femenino', '954321087', 'B+', '[\"Polvo doméstico (dato ficticio)\"]', '[\"Ninguna registrada\"]', '[\"Ninguno\"]', 'Nota ficticia de demostración: alergia de ejemplo pendiente de confirmación con el paciente.', '2026-03-01'),
(7, 14, 'Miguel Flores (Hermano)', 'Calle Lima 640, Puno', '1979-06-28', '984345672', 'masculino', '943210876', 'O-', '[\"Ninguna registrada (dato ficticio)\"]', '[\"Ninguna registrada\"]', '[\"Ninguno\"]', 'Nota ficticia de demostración: sin observaciones clínicas verificadas; confirmar datos en consulta.', '2026-03-01'),
(8, 15, 'Rosa Paredes (Madre)', 'Jr. Ayacucho 315, Piura', '2001-02-11', '983456783', 'femenino', '983456783', 'AB+', '[\"Fragancias (dato ficticio)\"]', '[\"Ninguna registrada\"]', '[\"Ninguno\"]', 'Nota ficticia de demostración: dato de sensibilidad de ejemplo; confirmar directamente con el paciente.', '2026-03-01');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios_usuario`
--

CREATE TABLE `usuarios_usuario` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `correo` varchar(254) DEFAULT NULL,
  `telefono` varchar(20) NOT NULL,
  `cuenta_id` int(11) DEFAULT NULL,
  `dni` varchar(20) DEFAULT NULL,
  `rol` varchar(20) NOT NULL,
  `permisos` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`permisos`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Tablas médicas añadidas por api.0001_initial.
CREATE TABLE `usuarios_antecedente` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tipo` varchar(20) NOT NULL,
  `descripcion` longtext NOT NULL,
  `fecha_aproximada` date DEFAULT NULL,
  `activo` tinyint(1) NOT NULL,
  `creado_en` datetime(6) NOT NULL,
  `paciente_id` bigint(20) NOT NULL,
  `registrado_por_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ant_paciente_tipo_idx` (`paciente_id`,`tipo`),
  KEY `usuarios_antecedente_paciente_id_7bbf0d26` (`paciente_id`),
  KEY `usuarios_antecedente_registrado_por_id_7e0b48fb` (`registrado_por_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `usuarios_atencion` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_atencion` datetime(6) NOT NULL,
  `motivo_consulta` longtext NOT NULL,
  `presion_arterial` varchar(15) NOT NULL,
  `frecuencia_cardiaca` smallint(5) unsigned DEFAULT NULL,
  `temperatura` decimal(4,1) DEFAULT NULL,
  `saturacion_o2` smallint(5) unsigned DEFAULT NULL,
  `peso_kg` decimal(6,2) DEFAULT NULL,
  `talla_cm` decimal(5,2) DEFAULT NULL,
  `diagnostico` longtext NOT NULL,
  `codigo_cie10` varchar(10) NOT NULL,
  `tratamiento` longtext NOT NULL,
  `notas` longtext NOT NULL,
  `creado_en` datetime(6) NOT NULL,
  `medico_id` bigint(20) NOT NULL,
  `paciente_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ate_paciente_fecha_idx` (`paciente_id`,`fecha_atencion` DESC),
  KEY `usuarios_atencion_medico_id_c71a157a` (`medico_id`),
  KEY `usuarios_atencion_paciente_id_0e5c2905` (`paciente_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios_usuario`
--

-- Los roles `medico` y `paciente`, permisos JSON y perfiles separados
-- ya existían en el dump; no se modifican ni se activan cuentas aquí.
-- Los dos perfiles de prueba añadidos debajo son completamente ficticios.
INSERT INTO `usuarios_usuario` (`id`, `nombre`, `apellido`, `correo`, `telefono`, `cuenta_id`, `dni`, `rol`, `permisos`) VALUES
(1, 'Gonzalo', 'Davila Ochochoque', 'davilagonzalo2304@gmail.com', '961967135', 8, '74807428', 'paciente', '[\"paciente.leer_propio\", \"paciente.editar_propio\", \"medico.leer\"]'),
(6, 'Lucas Dario', 'Davila Ochochoque', 'lucasdavila1710@gmail.com', '951987984', 6, '78451289', '', '[]'),
(7, 'Ana', 'Ortiz Velasquez', 'velasquezana2708@gmail.com', '987847594', 7, '78984565', 'medico', '[\"paciente.leer\", \"paciente.editar\", \"medico.leer_propio\", \"medico.editar_propio\"]'),
(8, 'Pedro', 'Suarez Lavado', 'suarezpedro1205@tecsup.edu.pe', '951958986', 9, '54857865', 'medico', '[\"paciente.leer\", \"paciente.editar\", \"medico.leer_propio\", \"medico.editar_propio\"]'),
(9, 'Valeria', 'Rojas Medina', 'medico.prueba@tecsup.edu.pe', '900000001', 10, '99999991', 'medico', '[\"paciente.leer\", \"paciente.editar\", \"medico.leer_propio\", \"medico.editar_propio\"]'),
(10, 'Juan Carlos', 'Pérez Gómez', 'paciente.prueba@tecsup.edu.pe', '987654321', 11, '88000000', 'paciente', '[\"paciente.leer_propio\", \"paciente.editar_propio\"]'),
(11, 'Rosa Elena', 'Quispe Cárdenas', 'rosa.quispe@tecsup.edu.pe', '987654321', NULL, '88000001', 'paciente', '[\"paciente.leer_propio\", \"paciente.editar_propio\"]'),
(12, 'Luis Miguel', 'Huamán Salazar', 'luis.huaman@tecsup.edu.pe', '986123450', NULL, '88000002', 'paciente', '[\"paciente.leer_propio\", \"paciente.editar_propio\"]'),
(13, 'Andrea Paola', 'Rojas Medina', 'andrea.rojas@tecsup.edu.pe', '985234561', NULL, '88000003', 'paciente', '[\"paciente.leer_propio\", \"paciente.editar_propio\"]'),
(14, 'José Antonio', 'Mamani Flores', 'jose.mamani@tecsup.edu.pe', '984345672', NULL, '88000004', 'paciente', '[\"paciente.leer_propio\", \"paciente.editar_propio\"]'),
(15, 'Fiorella Milagros', 'Chávez Paredes', 'fiorella.chavez@tecsup.edu.pe', '983456783', NULL, '88000005', 'paciente', '[\"paciente.leer_propio\", \"paciente.editar_propio\"]');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `auth_group`
--
ALTER TABLE `auth_group`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indices de la tabla `auth_group_permissions`
--
ALTER TABLE `auth_group_permissions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `auth_group_permissions_group_id_permission_id_0cd325b0_uniq` (`group_id`,`permission_id`),
  ADD KEY `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` (`permission_id`);

--
-- Indices de la tabla `auth_permission`
--
ALTER TABLE `auth_permission`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `auth_permission_content_type_id_codename_01ab375a_uniq` (`content_type_id`,`codename`);

--
-- Indices de la tabla `auth_user`
--
ALTER TABLE `auth_user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indices de la tabla `auth_user_groups`
--
ALTER TABLE `auth_user_groups`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `auth_user_groups_user_id_group_id_94350c0c_uniq` (`user_id`,`group_id`),
  ADD KEY `auth_user_groups_group_id_97559544_fk_auth_group_id` (`group_id`);

--
-- Indices de la tabla `auth_user_user_permissions`
--
ALTER TABLE `auth_user_user_permissions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `auth_user_user_permissions_user_id_permission_id_14a6b632_uniq` (`user_id`,`permission_id`),
  ADD KEY `auth_user_user_permi_permission_id_1fbb5f2c_fk_auth_perm` (`permission_id`);

--
-- Indices de la tabla `django_admin_log`
--
ALTER TABLE `django_admin_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `django_admin_log_content_type_id_c4bce8eb_fk_django_co` (`content_type_id`),
  ADD KEY `django_admin_log_user_id_c564eba6_fk_auth_user_id` (`user_id`);

--
-- Indices de la tabla `django_content_type`
--
ALTER TABLE `django_content_type`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `django_content_type_app_label_model_76bd3d3b_uniq` (`app_label`,`model`);

--
-- Indices de la tabla `django_migrations`
--
ALTER TABLE `django_migrations`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `django_session`
--
ALTER TABLE `django_session`
  ADD PRIMARY KEY (`session_key`),
  ADD KEY `django_session_expire_date_a5c62663` (`expire_date`);

--
-- Indices de la tabla `usuarios_medico`
--
ALTER TABLE `usuarios_medico`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `usuario_id` (`usuario_id`),
  ADD UNIQUE KEY `cmp` (`cmp`);

--
-- Indices de la tabla `usuarios_paciente`
--
ALTER TABLE `usuarios_paciente`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `usuarios_usuario`
--
ALTER TABLE `usuarios_usuario`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cuenta_id` (`cuenta_id`),
  ADD UNIQUE KEY `dni` (`dni`),
  ADD UNIQUE KEY `usuarios_usuario_correo_80ace29b_uniq` (`correo`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `auth_group`
--
ALTER TABLE `auth_group`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `auth_group_permissions`
--
ALTER TABLE `auth_group_permissions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `auth_permission`
--
ALTER TABLE `auth_permission`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT de la tabla `auth_user`
--
ALTER TABLE `auth_user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT de la tabla `auth_user_groups`
--
ALTER TABLE `auth_user_groups`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `auth_user_user_permissions`
--
ALTER TABLE `auth_user_user_permissions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `django_admin_log`
--
ALTER TABLE `django_admin_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `django_content_type`
--
ALTER TABLE `django_content_type`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `django_migrations`
--
ALTER TABLE `django_migrations`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT de la tabla `usuarios_medico`
--
ALTER TABLE `usuarios_medico`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `usuarios_paciente`
--
ALTER TABLE `usuarios_paciente`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `usuarios_usuario`
--
ALTER TABLE `usuarios_usuario`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `auth_group_permissions`
--
ALTER TABLE `auth_group_permissions`
  ADD CONSTRAINT `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  ADD CONSTRAINT `auth_group_permissions_group_id_b120cbf9_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`);

--
-- Filtros para la tabla `auth_permission`
--
ALTER TABLE `auth_permission`
  ADD CONSTRAINT `auth_permission_content_type_id_2f476e4b_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`);

--
-- Filtros para la tabla `auth_user_groups`
--
ALTER TABLE `auth_user_groups`
  ADD CONSTRAINT `auth_user_groups_group_id_97559544_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`),
  ADD CONSTRAINT `auth_user_groups_user_id_6a12ed8b_fk_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`);

--
-- Filtros para la tabla `auth_user_user_permissions`
--
ALTER TABLE `auth_user_user_permissions`
  ADD CONSTRAINT `auth_user_user_permi_permission_id_1fbb5f2c_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  ADD CONSTRAINT `auth_user_user_permissions_user_id_a95ead1b_fk_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`);

--
-- Filtros para la tabla `django_admin_log`
--
ALTER TABLE `django_admin_log`
  ADD CONSTRAINT `django_admin_log_content_type_id_c4bce8eb_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`),
  ADD CONSTRAINT `django_admin_log_user_id_c564eba6_fk_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`);

--
-- Filtros para la tabla `usuarios_medico`
--
ALTER TABLE `usuarios_medico`
  ADD CONSTRAINT `usuarios_medico_usuario_id_43910484_fk_usuarios_usuario_id` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios_usuario` (`id`);

--
-- Filtros para la tabla `usuarios_paciente`
--
ALTER TABLE `usuarios_paciente`
  ADD CONSTRAINT `usuarios_paciente_usuario_id_9a1be9db_fk_usuarios_usuario_id` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios_usuario` (`id`);

--
-- Filtros para la tabla `usuarios_usuario`
--
ALTER TABLE `usuarios_usuario`
  ADD CONSTRAINT `usuarios_usuario_cuenta_id_da6e415b_fk_auth_user_id` FOREIGN KEY (`cuenta_id`) REFERENCES `auth_user` (`id`);

--
-- Filtros para las nuevas tablas médicas
--
ALTER TABLE `usuarios_antecedente`
  ADD CONSTRAINT `ant_paciente_fk` FOREIGN KEY (`paciente_id`) REFERENCES `usuarios_paciente` (`id`),
  ADD CONSTRAINT `ant_medico_fk` FOREIGN KEY (`registrado_por_id`) REFERENCES `usuarios_medico` (`id`);

ALTER TABLE `usuarios_atencion`
  ADD CONSTRAINT `ate_medico_fk` FOREIGN KEY (`medico_id`) REFERENCES `usuarios_medico` (`id`),
  ADD CONSTRAINT `ate_paciente_fk` FOREIGN KEY (`paciente_id`) REFERENCES `usuarios_paciente` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
