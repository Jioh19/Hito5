# 🛡️ Enterprise Identity & User Management System - Full-Stack Integration

## 🛠️ Stack Tecnológico
* **Backend:** Java 21, Spring Boot 3 / 4, Spring Data JPA, Hibernate, OpenAPI/Swagger.
* **Frontend:** TypeScript Vanilla, Vite, Native ESM, HTML5/CSS3 Semántico.
* **Infraestructura:** Docker Compose, PostgreSQL 16 Alpine.
* **Calidad y Testing:** JUnit 5, Mockito, JaCoCo, TDD & Clean Architecture, TypeScript Strict Mode.

---

## 🔗 Repositorios de Referencia
* Core de Dominio / Hito 1: [Hito1 / neonpulse-ticketera](https://github.com/sebavidal10/neonpulse-ticketera)
* Frontend Vite + TS / Hito 2: [Hito2 / neonpulse-frontend](https://github.com/sebavidal10/neonpulse-frontend)
* Backend Spring Boot / Hito 4: [hito4 / neonpulse-api-springboot](https://github.com/sebavidal10/neonpulse-api-springboot)

---

## 🚀 Guía de Puesta en Marcha Local

### 1. Levantar la Base de Datos Relacional
```bash
cd backend
docker compose up -d
```

### 2. Ejecutar Pruebas Automatizadas
```bash
./mvnw clean test
```

### 3. Iniciar el Microservicio Backend
```bash
./mvnw spring-boot:run
```
* **API REST:** http://localhost:8080/api/v1/users
* **Swagger UI (Perfil Dev):** http://localhost:8080/swagger-ui.html

### 4. Iniciar la Interfaz Web Frontend
```bash
cd ../frontend
npm install
npm run dev
```
* **App Web:** http://localhost:5173

---

## 📋 Arquitectura de la Solución

### Frontend (`frontend`)
* **`src/models/`**: Contratos de datos tipados (`User`, `RegisterUserDTO`, `LoginUserDTO`, `ApiErrorResponse`) con cero uso de `any` y tipado estricto.
* **`src/services/`**: Servicio de red asíncrono (`fetchUsers`, `registerUser`, `loginUser`, `deleteUser`) con gestión y sanitización centralizada de errores HTTP.
* **`src/components/`**: Componentes modulares y reutilizables (`UserCard`, `Alert`) con generación semántica y sanitización contra inyección XSS.
* **`src/style.css`**: Hoja de estilos moderna y responsiva con tokens CSS, accesibilidad WCAG (tap targets ≥ 48px, contraste, focus visible) y feedback visual reactivo.
* **`src/main.ts`**: Controlador reactivo del DOM con validación en cliente (reglas de dominio para usuario y correo), consumo asíncrono con `try/catch` y actualización en tiempo real de la base de datos PostgreSQL.

### Backend (`backend`)
* **Clean Architecture**: Capa de Dominio pura (cero dependencias de frameworks en `domain/`), Capa de Aplicación con Casos de Uso aislados, e Infraestructura con persistencia relacional JPA sobre PostgreSQL.
* **CORS Configurado**: `@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})` en `UserController`.
* **Seguridad y Perfiles**: Swagger UI habilitado en perfil `dev` y bloqueado en `prod`.
