# Project Overview

This project is a collaborative whiteboard backend composed of two main modules:

## 1. space-sync-server
- **Purpose**: Interacting with the database.
- **Functionality**: Handles complex algorithms, data manipulation, and ensures data consistency (e.g., using 2-phase commits, operational algorithms).

## 2. space-api-gateway
- **Purpose**: Manage connection and authentication / authorization.
- **Functionality**: Acts as the entry point for routing, administration, and handling socket flows between the Front-End and the Sync Server.
