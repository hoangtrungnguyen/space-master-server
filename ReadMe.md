# Collaborative Whiteboard

# API-GATEWAY server

Design, coding, testing collaborative servers
API-GateWay for routing, adminstration site, authentication. Here, I use KTOR as backend framework. Socket flows to
receive changes from FE, end changes to Sync Server. Then, in the down flow, listen to changes from REDIS.

# SYNC-SERVER

Sync Server handle complicated algorithms and data manipulate
Ensure data consistency using various algorithms: 2 phrase commits, Operational Algorithm,

# Architect Stream Processing

## KAFKA

Kafka for publish changes from front-end (UP flow)

## REDIS

REDIS works as Down flow

# Front-end

Architect front-end using React
Design pattern Combination of various lib (mobx, jsx) to ensure code readability, scalability, maintainability

# DOCUMENTS for A.I agents

- 3 types of documents
    - Requirements: What we want to do ()
    - Architecture: Overall strategy and plan (Modules, components interactions, )
    - Implementation: Detail implementation (Tech, algorithms, classes, interfaces)

# REFERENCES

Techstack
WebRTC
Redis
Postgres
Kotlin - Ktor
React
Kafka
