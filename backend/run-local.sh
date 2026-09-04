#!/bin/sh
set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
env_file="$script_dir/.env"

if [ ! -f "$env_file" ]; then
  echo "Missing backend/.env. Copy backend/.env.example to backend/.env first." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
. "$env_file"
set +a

cd "$script_dir"
exec ./mvnw spring-boot:run
