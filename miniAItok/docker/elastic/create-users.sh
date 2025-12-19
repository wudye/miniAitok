#!/bin/sh
set -eu

ES_URL="http://elasticsearch:9200"
AUTH="elastic:${ELASTIC_PASSWORD}"

echo "Waiting for Elasticsearch..."
until curl -sS -u "${AUTH}" "${ES_URL}/_cluster/health?wait_for_status=yellow&timeout=1s" >/dev/null 2>&1; do
  sleep 2
done
echo "Elasticsearch is up."

# create admin1 (幂等)
if ! curl -sS -u "${AUTH}" -f "${ES_URL}/_security/user/admin1" >/dev/null 2>&1; then
  echo "Creating admin1..."
  curl -sS -u "${AUTH}" -H "Content-Type: application/json" -X POST "${ES_URL}/_security/user/admin1" \
    -d '{"password":"123456789","roles":["superuser"],"full_name":"admin1"}'
fi

# create kibana_user for Kibana (kibana_system role)
if ! curl -sS -u "${AUTH}" -f "${ES_URL}/_security/user/kibana_user" >/dev/null 2>&1; then
  echo "Creating kibana_user..."
  curl -sS -u "${AUTH}" -H "Content-Type: application/json" -X POST "${ES_URL}/_security/user/kibana_user" \
    -d '{"password":"123456789","roles":["kibana_system"],"full_name":"Kibana system user"}'
fi

echo "es-init finished."