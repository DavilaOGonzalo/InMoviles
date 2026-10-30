# EssAid backend

Backend Django REST Framework for the medical mobile workflows. It provides
JWT authentication for mobile clients and keeps Django session authentication
enabled for browser/admin workflows.

## Local setup

Python 3.11 or newer is recommended. From this directory:

```powershell
py -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
python manage.py migrate
python manage.py test
python manage.py runserver
```

By default, local development and tests use SQLite. For MariaDB, copy
`.env.example` values into the process environment and set `DB_ENGINE=mysql`,
`DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_HOST`, and `DB_PORT`. Install/import
the SQL dump into that database before applying the new medical-record
migration. Never use the example secret or `DEBUG=true` in production.
The deployment check also recommends HSTS for subdomains/preload; set those
options to `true` only when every affected hostname is served exclusively over
HTTPS.

The updated `../mihospital_db.sql` includes both existing medical-profile
tables and the `api.0001_initial` tables/migration record. For a database
created from that dump, confirm the `django_migrations` entries and table
columns match this revision before starting the service. When applying this
backend to the original dump from before these additions, back it up and run:

```powershell
python manage.py migrate --fake-initial
```

The initial `usuarios` migration is a reconstructed baseline matching the
three existing profile tables. `--fake-initial` records it without recreating
those tables when they already exist; the API migration creates
`usuarios_antecedente` and `usuarios_atencion`. Back up the database and verify
table columns before applying migrations to any non-disposable database.
The SQL dump contains real-looking personal data; it is not copied into test
fixtures or used by the local test suite.

## Fictitious accounts in the SQL dump

`../mihospital_db.sql` includes two clearly marked, active demonstration
accounts so both role-specific app sections can be exercised:

| Role | Email / username | Test password |
| --- | --- | --- |
| Médico ficticio | `medico.prueba@tecsup.edu.pe` | `EssAidTest!2026` |
| Paciente ficticio | `paciente.prueba@tecsup.edu.pe` | `EssAidTest!2026` |

These accounts use fictional `@tecsup.edu.pe` addresses and dummy contact
details; their reserved DNIs are `99999991` (doctor) and `88000000` (patient),
and the doctor has a test-only CMP. The credentials are for local testing only. Do not deploy the dump or
reuse the password in production; replace/remove the demo accounts before
deployment.

The dump also contains five synthetic patient profiles with DNIs
`88000001`–`88000005` for exact-DNI and partial-name search tests. They have no
login accounts. Names, contact details, and medical fields on these profiles
are fictitious display/search data, not medical facts; confirm all health
information with the person receiving care. The Android mock search mirrors
these same five profiles.

## Authentication and authorization

- `POST /api/auth/token/` accepts an email and password and returns access and
  refresh JWTs.
- `POST /api/auth/token/refresh/` exchanges a refresh token for a new access
  token.
- Mobile API requests use `Authorization: Bearer <access>`.
- Django session authentication remains enabled.
- Patient reads require an active Django account, linked `rol="medico"` profile,
  linked `Medico` row, and `paciente.leer`.
- Creating an attention requires the same active medical profile and
  `paciente.editar`. The authenticated medical profile is assigned server-side.

See [API_CONTRACT.md](API_CONTRACT.md) for request/response details.
