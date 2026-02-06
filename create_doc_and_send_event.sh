#!/bin/bash

# Configuration
API_URL="http://localhost:8080/api"
TOPIC="ideaspace.document.event"
BROKER_CONTAINER="broker"
BOOTSTRAP_SERVER="localhost:9092"
COOKIE_FILE="cookies.txt"

# --- 1. User Registration / Login ---
echo "1. Registering/Logging in user..."
# Randomize username to avoid conflicts on repeat runs (or just use a fixed one)
USERNAME="user_$(date +%s)"
FULLNAME="User $(date +%s)"

REGISTER_RESPONSE=$(curl -s -c $COOKIE_FILE -X POST "$API_URL/users/sign-up" \
  -H "Content-Type: application/json" \
  -d "{
    \"loginName\": \"$USERNAME\",
    \"fullName\": \"$FULLNAME\" 
  }")

# Note: The actual registration request body depends on RegisterNameRequest in Kotlin.
# Looking at the code: val request = call.receive<RegisterNameRequest>()
# I'll assume RegisterNameRequest has loginName and fullName based on usage, 
# but I don't see the password field used in the snippet provided. 
# Adjusting to likely fields.

USER_ID=$(echo $REGISTER_RESPONSE | grep -o '"id":[0-9]*' | head -1 | awk -F: '{print $2}')

if [ -z "$USER_ID" ]; then
    echo "Error: Failed to register/login."
    echo "Response: $REGISTER_RESPONSE"
    exit 1
fi

echo "User registered/logged in. ID: $USER_ID"

# --- 2. Create Document ---
echo "2. Creating a new document..."
DOC_TITLE="Auto Doc $(date +%s)"
DOC_TYPE="CANVAS" # Assuming enum value based on probable types

CREATE_DOC_RESPONSE=$(curl -s -b $COOKIE_FILE -X POST "$API_URL/documents/create" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"$DOC_TITLE\",
    \"documentType\": \"$DOC_TYPE\"
  }")

DOC_ID=$(echo $CREATE_DOC_RESPONSE | grep -o '"id":[0-9]*' | head -1 | awk -F: '{print $2}')

if [ -z "$DOC_ID" ]; then
    echo "Error: Failed to create document."
    echo "Response: $CREATE_DOC_RESPONSE"
    exit 1
fi

echo "Document created. ID: $DOC_ID"

# --- 3. Publish Event to Kafka ---
echo "3. Publishing event to Kafka for Doc ID: $DOC_ID..."

# Helper IDs
PROCESS_ID=999
WINDOW_ID=888
PEER_UUID=$(cat /proc/sys/kernel/random/uuid)

get_uuid() {
    cat /proc/sys/kernel/random/uuid
}

get_random_int() {
    shuf -i 1-1000 -n 1
}

send_kafka_event() {
    local json="$1"
    local minified_json=$(echo "$json" | tr -d '\n' | sed 's/  //g')
    echo "Sending payload: $minified_json"
    echo "$minified_json" | docker exec -i $BROKER_CONTAINER kafka-console-producer --bootstrap-server $BOOTSTRAP_SERVER --topic $TOPIC
}

# --- 3.1 INIT_SYNC ---
echo "3.1 Sending INIT_SYNC..."
INIT_SYNC_JSON=$(cat <<EOF
{
  "syncOp": "INIT_SYNC",
  "docId": $DOC_ID,
  "processId": $PROCESS_ID,
  "userId": $USER_ID,
  "windowId": $WINDOW_ID,
  "peerUuid": "$PEER_UUID"
}
EOF
)
send_kafka_event "$INIT_SYNC_JSON"

# --- 3.2 ADD_ELEMENT ---
echo "3.2 Sending ADD_ELEMENT..."
ELEMENT_UUID=$(get_uuid)
ADD_ELEMENT_JSON=$(cat <<EOF
{
  "syncOp": "ADD_ELEMENT",
  "docId": $DOC_ID,
  "processId": $PROCESS_ID,
  "userId": $USER_ID,
  "windowId": $WINDOW_ID,
  "uuid": "$ELEMENT_UUID",
  "parentUuid": null,
  "type": "text",
  "value": { "content": "Hello World", "x": 100, "y": 200 }
}
EOF
)
send_kafka_event "$ADD_ELEMENT_JSON"

# --- 3.3 EDIT_ELEMENT ---
echo "3.3 Sending EDIT_ELEMENT..."
EDIT_ELEMENT_JSON=$(cat <<EOF
{
  "syncOp": "EDIT_ELEMENT",
  "docId": $DOC_ID,
  "processId": $PROCESS_ID,
  "userId": $USER_ID,
  "windowId": $WINDOW_ID,
  "revision": 1,
  "uuid": "$ELEMENT_UUID",
  "type": "text",
  "value": { "content": "Hello World Updated", "x": 150, "y": 250 }
}
EOF
)
send_kafka_event "$EDIT_ELEMENT_JSON"

# --- 3.4 MOVE_ELEMENT ---
echo "3.4 Sending MOVE_ELEMENT..."
PARENT_UUID=$(get_uuid)
MOVE_ELEMENT_JSON=$(cat <<EOF
{
  "syncOp": "MOVE_ELEMENT",
  "docId": $DOC_ID,
  "processId": $PROCESS_ID,
  "userId": $USER_ID,
  "windowId": $WINDOW_ID,
  "uuid": "$ELEMENT_UUID",
  "parentUuid": "$PARENT_UUID"
}
EOF
)
send_kafka_event "$MOVE_ELEMENT_JSON"

# --- 3.5 REMOVE_ELEMENT ---
echo "3.5 Sending REMOVE_ELEMENT..."
REMOVE_ELEMENT_JSON=$(cat <<EOF
{
  "syncOp": "REMOVE_ELEMENT",
  "docId": $DOC_ID,
  "processId": $PROCESS_ID,
  "userId": $USER_ID,
  "windowId": $WINDOW_ID,
  "uuid": "$ELEMENT_UUID"
}
EOF
)
send_kafka_event "$REMOVE_ELEMENT_JSON"

# --- 3.6 SAVE_DOC ---
echo "3.6 Sending SAVE_DOC..."
SAVE_DOC_JSON=$(cat <<EOF
{
  "syncOp": "SAVE_DOC",
  "docId": $DOC_ID,
  "processId": $PROCESS_ID,
  "userId": $USER_ID,
  "windowId": $WINDOW_ID
}
EOF
)
send_kafka_event "$SAVE_DOC_JSON"

echo "Done."
