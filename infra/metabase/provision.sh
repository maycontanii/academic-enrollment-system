#!/bin/sh
# One-shot Metabase provisioning (Metabase has no file-based provisioning like Grafana).
# On first boot it drives the setup API: creates the admin, connects academicdb, and builds a
# starter business dashboard. Idempotent — if Metabase is already set up, it exits without changes.
MB="http://metabase:3000"

echo "provision: waiting for Metabase..."
i=0
until curl -sf "$MB/api/health" | grep -q '"status":"ok"'; do
  i=$((i + 1)); [ "$i" -gt 60 ] && { echo "provision: Metabase not healthy in time"; exit 1; }
  sleep 3
done

PROPS=$(curl -s "$MB/api/session/properties")
if [ "$(echo "$PROPS" | jq -r '.["has-user-setup"]')" = "true" ]; then
  echo "provision: Metabase already set up — nothing to do."
  exit 0
fi
TOKEN=$(echo "$PROPS" | jq -r '.["setup-token"]')
if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then echo "provision: no setup token — assuming already set up."; exit 0; fi

echo "provision: creating admin..."
SETUP_RESP=$(curl -s -X POST "$MB/api/setup" -H 'Content-Type: application/json' -d "$(jq -n --arg t "$TOKEN" '{
  token: $t,
  user: {first_name:"Admin", last_name:"User", email:"admin@example.com", password:"metabase123"},
  prefs: {site_name:"Academic Enrollment", allow_tracking:false}
}')")
SESSION=$(echo "$SETUP_RESP" | jq -r '.id')
if [ -z "$SESSION" ] || [ "$SESSION" = "null" ]; then echo "provision: setup failed: $SETUP_RESP"; exit 1; fi
AUTH="X-Metabase-Session: $SESSION"
echo "provision: setup ok."

echo "provision: connecting academicdb..."
DBRESP=$(curl -s -X POST "$MB/api/database" -H "$AUTH" -H 'Content-Type: application/json' -d "$(jq -n '{
  engine:"postgres", name:"Academic DB",
  details:{host:"postgres", port:5432, dbname:"academicdb", user:"app", password:"app", ssl:false}
}')")
DBID=$(echo "$DBRESP" | jq -r '.id')
if [ -z "$DBID" ] || [ "$DBID" = "null" ]; then echo "provision: db add failed: $DBRESP"; exit 1; fi
echo "provision: academicdb id=$DBID"

make_card() { # make_card <name> <sql> <display>  -> prints card id
  curl -s -X POST "$MB/api/card" -H "$AUTH" -H 'Content-Type: application/json' \
    -d "$(jq -n --arg n "$1" --arg q "$2" --arg d "$3" --argjson db "$DBID" '{
      name: $n, display: $d, visualization_settings: {},
      dataset_query: {type:"native", database:$db, native:{query:$q}}
    }')" | jq -r '.id'
}

C1=$(make_card "Enrollments by status" "SELECT status, count(*) AS total FROM enrollment GROUP BY status ORDER BY total DESC" "bar")
C2=$(make_card "Seat fill by class" "SELECT label, seats_used, seat_limit FROM class ORDER BY label" "table")
echo "provision: cards c1=$C1 c2=$C2"

DASHID=$(curl -s -X POST "$MB/api/dashboard" -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"name":"Academic — Business Overview"}' | jq -r '.id')
if [ -n "$DASHID" ] && [ "$DASHID" != "null" ] && [ "$C1" != "null" ] && [ "$C2" != "null" ]; then
  curl -s -X PUT "$MB/api/dashboard/$DASHID" -H "$AUTH" -H 'Content-Type: application/json' \
    -d "$(jq -n --argjson c1 "$C1" --argjson c2 "$C2" '{dashcards:[
      {id:-1, card_id:$c1, row:0, col:0, size_x:12, size_y:8},
      {id:-2, card_id:$c2, row:0, col:12, size_x:12, size_y:8}
    ]}')" >/dev/null
  echo "provision: dashboard id=$DASHID ready."
else
  echo "provision: dashboard step skipped — setup + DB are done."
fi
echo "provision: done."
