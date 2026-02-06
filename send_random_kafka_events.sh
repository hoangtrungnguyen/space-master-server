#!/bin/bash

TOPIC="ideaspace.document.event"
BROKER_CONTAINER="broker"
BOOTSTRAP_SERVER="localhost:9092"

get_uuid() {
    cat /proc/sys/kernel/random/uuid
}

get_random_int() {
    shuf -i 1-1000 -n 1
}

get_random_long() {
    shuf -i 1-100000 -n 1
}

generate_init_sync() {
    cat <<EOF
{
  "syncOp": "INIT_SYNC",
  "docId": $(get_random_long),
  "processId": $(get_random_long),
  "userId": $(get_random_long),
  "windowId": $(get_random_long),
  "peerUuid": "$(get_uuid)"
}
EOF
}

generate_add_element() {
    cat <<EOF
{
  "syncOp": "ADD_ELEMENT",
  "docId": $(get_random_long),
  "processId": $(get_random_long),
  "userId": $(get_random_long),
  "windowId": $(get_random_long),
  "uuid": "$(get_uuid)",
  "parentUuid": null,
  "type": "text",
  "value": { "content": "Random text $(get_random_int)" }
}
EOF
}

generate_edit_element() {
    cat <<EOF
{
  "syncOp": "EDIT_ELEMENT",
  "docId": $(get_random_long),
  "processId": $(get_random_long),
  "userId": $(get_random_long),
  "windowId": $(get_random_long),
  "revision": $(get_random_int),
  "uuid": "$(get_uuid)",
  "type": "text",
  "value": { "content": "Edited text $(get_random_int)" }
}
EOF
}

generate_move_element() {
    cat <<EOF
{
  "syncOp": "MOVE_ELEMENT",
  "docId": $(get_random_long),
  "processId": $(get_random_long),
  "userId": $(get_random_long),
  "windowId": $(get_random_long),
  "uuid": "$(get_uuid)",
  "parentUuid": "$(get_uuid)"
}
EOF
}

generate_remove_element() {
    cat <<EOF
{
  "syncOp": "REMOVE_ELEMENT",
  "docId": $(get_random_long),
  "processId": $(get_random_long),
  "userId": $(get_random_long),
  "windowId": $(get_random_long),
  "uuid": "$(get_uuid)"
}
EOF
}

generate_save_doc() {
    cat <<EOF
{
  "syncOp": "SAVE_DOC",
  "docId": $(get_random_long),
  "processId": $(get_random_long),
  "userId": $(get_random_long),
  "windowId": $(get_random_long)
}
EOF
}

# Main loop
count=${1:-1} # Default to 1 message if not specified

echo "Sending $count random messages to $TOPIC..."

for ((i=1; i<=count; i++)); do
    type=$(shuf -e INIT_SYNC ADD_ELEMENT EDIT_ELEMENT MOVE_ELEMENT REMOVE_ELEMENT SAVE_DOC -n 1)
    
    case $type in
        INIT_SYNC) json=$(generate_init_sync) ;;
        ADD_ELEMENT) json=$(generate_add_element) ;;
        EDIT_ELEMENT) json=$(generate_edit_element) ;;
        MOVE_ELEMENT) json=$(generate_move_element) ;;
        REMOVE_ELEMENT) json=$(generate_remove_element) ;;
        SAVE_DOC) json=$(generate_save_doc) ;;
    esac

    # Minify JSON by removing newlines (simple sed)
    minified_json=$(echo "$json" | tr -d '\n' | sed 's/  //g')
    
    echo "Sending [$i/$count]: $type"
    echo "$minified_json" | docker exec -i $BROKER_CONTAINER kafka-console-producer --bootstrap-server $BOOTSTRAP_SERVER --topic $TOPIC
done

echo "Done."
