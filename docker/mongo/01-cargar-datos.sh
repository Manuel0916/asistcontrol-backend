#!/bin/bash
# Se ejecuta solo la PRIMERA vez que se crea el contenedor de MongoDB:
# carga la base "asistcontrol" (usuarios, roles, login_config y asistencias).
set -e
echo "AsistControl: cargando datos iniciales..."
mongorestore --gzip --archive=/docker-entrypoint-initdb.d/asistcontrol.archive.gz
echo "AsistControl: datos cargados."
