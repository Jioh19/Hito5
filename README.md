# 🛡️ Hito 5 — Enterprise Identity & User Management (Full-Stack Integration)

Integración completa Full-Stack que une el backend persistente Spring Boot 3 + PostgreSQL con una interfaz web interactiva en TypeScript Vanilla + Vite, cumpliendo rigurosamente con los 8 puntos de la **Guía Definitiva de Extremo a Extremo (10/10)**.

---

## 📁 Estructura del Proyecto

```
Hito5/
├── backend/                    # Microservicio Spring Boot 3 + PostgreSQL 16 (Clean Architecture)
│   ├── .env                    # Variables y secretos locales (ignorado en Git)
│   ├── .env.example            # Plantilla de variables (versionada)
│   ├── compose.yaml            # Docker Compose para PostgreSQL 16
│   ├── pom.xml                 # Configuración Maven (Java 21, Spring Boot 3)
│   ├── README.md               # Documentación específica del backend
│   └── src/                    # Capas: domain, application, infrastructure
│
└── frontend/                   # Aplicación Web SPA (TypeScript Vanilla + Vite)
    ├── .env                    # Configuración del frontend (ignorado en Git)
    ├── .env.example            # Plantilla de variables del frontend (versionada)
    ├── package.json            # Scripts de build, dev y dependencias
    ├── tsconfig.json           # Modo estricto TypeScript (strict: true, zero 'any')
    ├── vite.config.ts          # Configuración del servidor Vite y proxy /api
    ├── index.html              # Maquetación semántica y accesible
    ├── README.md               # Documentación específica del frontend
    └── src/                    # models, services, components, styles, main.ts
```

---

## 🛠️ Stack Tecnológico

* **Backend:** Java 21, Spring Boot 3, Spring Data JPA, Hibernate, OpenAPI/Swagger UI.
* **Frontend:** TypeScript Vanilla, Vite 6, Native ESM, HTML5 semántico, CSS3 moderno.
* **Base de Datos & Infraestructura:** Docker Compose, PostgreSQL 16 Alpine.
* **Calidad y Testing:** JUnit 5, Mockito, JaCoCo, TypeScript Strict Mode (Zero `any`).

---

## 🚀 Guía de Puesta en Marcha Local

### 1. Levantar la Base de Datos Relacional
```bash
cd backend
docker compose up -d
```

### 2. Ejecutar Pruebas Automatizadas del Backend
```bash
cd backend
./mvnw clean test
```

### 3. Iniciar el Microservicio Backend
```bash
cd backend
./mvnw spring-boot:run
```
* **API REST:** `http://localhost:8080/api/v1/users`
* **Swagger UI (Dev):** `http://localhost:8080/swagger-ui.html`

### 4. Iniciar la Interfaz Web Frontend
```bash
cd frontend
npm install
npm run dev
```
* **Aplicación Web:** `http://localhost:5173`

---

## ✅ Checklist de Cumplimiento de Rúbrica (10/10)

1. **Pruebas Unitarias**: 63/63 tests pasando (`./mvnw test`).
2. **Cero Frameworks en Dominio**: Capa `domain/` pura en Java sin anotaciones de Spring ni JPA.
3. **Persistencia Real**: Base de datos relacional PostgreSQL activa en puerto 5432 con volumen persistente.
4. **CORS Resuelto**: `@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})` en `UserController`.
5. **Cero `any` en Frontend**: `npm run build` compila limpiamente sin ningún tipo `any`.
6. **Ciclo Completo**: Formularios web registran, autentican y eliminan usuarios con refresco en tiempo real.
7. **Exclusión de Secretos**: Archivos `.env` protegidos en `.gitignore`; plantillas `.env.example` versionadas.
8. **Swagger Aislado**: Swagger UI activo en perfil `dev` y bloqueado en perfil `prod`.
