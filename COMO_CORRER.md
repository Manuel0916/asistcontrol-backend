# Cómo correr el proyecto web (AsistControl) en el PC

Hay **dos formas**. Usa **una a la vez** (las dos usan el puerto 8080).

| Forma | Cuándo usarla |
|---|---|
| **A. IntelliJ (botón Run)** | Día a día y para presentar. Usa tu MongoDB del PC. |
| **B. Docker** | Para mostrar que todo corre en contenedores (lo que pidió el profe). |

Las dos traen el modelo de IA en Python (Random Forest) funcionando solo.

---

## A. Con IntelliJ (un clic)

1. Abre **IntelliJ IDEA** y el proyecto `F:\proyecto aula\control_asistencias-main`
   (*File → Open...* si no está abierto).
2. Arriba a la derecha, en la lista de configuraciones, elige **AsistControl Web**.
3. Dale al **botón verde ▶ (Run)**.
4. Espera en la consola de abajo la línea
   `Started ControlAsistenciaApplication` (unos 10 segundos).
   Antes verás `IA: prendiendo el servicio de Python...`: **la IA se prende sola**.
5. Abre en el navegador: **http://localhost:8080** (página principal).
6. Para apagar: **botón rojo ■ (Stop)**. La IA se apaga con él.

> Lo que necesita (ya está listo en este PC): el servicio **MongoDB** de Windows prendido
> (arranca solo con el PC) y la carpeta `control_asistencias_ia` al lado de esta, con su `.venv`.

---

## B. Con Docker

1. Abre **Docker Desktop** y espera a que abajo a la izquierda diga **Engine running** (verde).
2. Abre **PowerShell** y pega:
   ```powershell
   cd "F:\proyecto aula\control_asistencias-main"; docker compose up --build
   ```
   La primera vez tarda unos minutos (descarga e instala todo). Las siguientes, segundos.
3. Cuando veas `Started ControlAsistenciaApplication`, abre **http://localhost:8080**.
4. Para apagar: en esa ventana **Ctrl + C**, y luego:
   ```powershell
   docker compose down
   ```

Levanta 3 contenedores: **asistcontrol-web** (Spring Boot), **asistcontrol-ia** (Python) y
**asistcontrol-mongo** (MongoDB, que la primera vez se llena con los datos del proyecto).

> Docker tiene **su propia copia** de los datos: lo que registres ahí no aparece en el
> MongoDB del PC (y al revés). Para empezar de cero con los datos originales:
> `docker compose down -v` y luego otra vez `docker compose up --build`.

---

## Si algo falla

| Problema | Solución |
|---|---|
| `Port 8080 was already in use` | Ya hay uno corriendo: detén IntelliJ (■) o Docker (`docker compose down`). |
| La predicción dice "El servicio de IA está despertando" | Espera 10 segundos y dale *Reintentar* (la IA se está entrenando al arrancar). |
| En IntelliJ sale `IA: no se encontró ...\.venv` | Prepara la IA una vez (ver `control_asistencias_ia/README.md`, "Ejecutar en tu PC"). |
| Docker: `docker: command not found` o error de conexión | Abre Docker Desktop y espera a que diga *Engine running*. |
| ControlBot (chat) no responde | Necesita **Ollama** prendido en el PC con el modelo `llama3.2`. No afecta lo demás. |
